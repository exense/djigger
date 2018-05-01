/*******************************************************************************
 * (C) Copyright 2016 Jérôme Comte and Dorian Cransac
 *
 *  This file is part of djigger
 *
 *  djigger is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  djigger is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with djigger.  If not, see <http://www.gnu.org/licenses/>.
 *
 *******************************************************************************/


/*******************************************************************************
 * Configurator : practical static methods used to load various parts of the collector's configuration  
 /*******************************************************************************/

package io.djigger.collector.server.conf;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.XStream;
import io.djigger.client.Facade;
import io.djigger.client.JMXClientFacade;
import io.djigger.collector.server.conf.mixin.InstrumentSubscriptionMixin;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class Configurator {

    private static final Logger logger = LoggerFactory.getLogger(Configurator.class);

    public static CollectorConfig parseCollectorConfiguration(String collConfigFilename) throws Exception {

        if (collConfigFilename == null || collConfigFilename.trim().isEmpty())
            throw new Exception("Invalid collector config file : " + collConfigFilename + ". Check your -DcollectorConfig option.");

        try {
            XStream xstream = new XStream();
            xstream.alias("Collector", CollectorConfig.class);
            xstream.processAnnotations(MongoDBParameters.class);
            return (CollectorConfig) xstream.fromXML(new File(collConfigFilename));
        } catch (Exception e) {
            String errorMessage = "XStream could not load the collector's config file located at " + collConfigFilename + ". Exception message was : " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    public static ConnectionsConfig parseConnectionsConfiguration(List<String> connectionsConfigFiles) throws Exception {

        if (connectionsConfigFiles == null || connectionsConfigFiles.size() < 1)
            throw new Exception("The connections config file is either empty or null. Please define a proper file location in in the <collectionFiles> block of your Collector.xml (see template in conf/ folder).");

        List<ConnectionsConfig> ccList = new ArrayList<ConnectionsConfig>();

        for (String connectionsFile : connectionsConfigFiles) {
            if (connectionsFile == null || connectionsFile.trim().isEmpty())
                throw new Exception("Invalid connections config file : " + connectionsConfigFiles + ". Check your -DconnectionsConfig option.");


            if (connectionsFile.trim().toLowerCase().endsWith(".xml"))
                ccList.add(parseConnectionsXML(connectionsFile));
            else if (connectionsFile.trim().toLowerCase().endsWith(".json"))
                ccList.add(parseConnectionsJson(connectionsFile));
            else {
                if (connectionsFile.trim().toLowerCase().endsWith(".csv"))
                    ccList.add(parseConnectionsCSV(connectionsFile));
                else {
                    File folderCase = new File(connectionsFile);
                    if (folderCase.isDirectory()) {
                        List<String> subFiles = qualify(folderCase.list(), folderCase.getAbsolutePath());
                        ccList.add(parseConnectionsConfiguration(subFiles));
                    }
                }
            }
        }
        ConnectionsConfig result = mergeConnectionsConfigs(ccList);

        logger.info("Parsed the following files:" + connectionsConfigFiles);
        logger.info("Which loaded the following connection definitions:" + result);

        return result;
    }

    private static List<String> qualify(String[] list, String path) {

        List<String> qualified = new ArrayList<String>();

        for (int i = 0; i < list.length; i++)
            qualified.add(path + File.separator + list[i]);

        return qualified;
    }

    private static ConnectionsConfig mergeConnectionsConfigs(
        List<ConnectionsConfig> ccList) {

        ConnectionGroup topContainer = new ConnectionGroup();

        for (ConnectionsConfig cc : ccList)
            topContainer.addGroup(cc.getConnectionGroup());

        ConnectionsConfig result = new ConnectionsConfig();
        result.setConnectionGroup(topContainer);

        return result;
    }

    private static ConnectionsConfig parseConnectionsCSV(String connectionsConfigFilename) {

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(connectionsConfigFilename));
        } catch (FileNotFoundException e1) {
            logger.error("Connection definitions file not found: " + connectionsConfigFilename, e1);
            // not the ideal way of signaling an exception, but at least it will throw another exception later on
            return null;
        }

        // Build a single ConnectionGroup to hold all Connections with flat, potentially redundant attributes
        List<ConnectionGroupNode> cgnList = new ArrayList<ConnectionGroupNode>();

        try {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                // ignore completely empty lines, and comment lines (starting with a "#")
                // the "#" character is not a legal character for an IP or hostname anyway, so we're on the safe side here.
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                try {
                    cgnList.add(parseAndBuildConnection(line));
                } catch (Exception e) {
                    logger.error("Error while parsing connection definition line in " + connectionsConfigFilename + ": " + line, e);
                    // fatal exception will be thrown later
                    return null;
                }
            }
        } catch (IOException e1) {
            logger.error("Error while reading connection definitions from file " + connectionsConfigFilename, e1);
            // fatal exception will be thrown later
            return null;
        }

        ConnectionsConfig cc = new ConnectionsConfig();
        ConnectionGroup cg = new ConnectionGroup();
        cg.setGroups(cgnList);
        cc.setConnectionGroup(cg);

        return cc;

    }

    private static ConnectionsConfig parseConnectionsXML(String connectionsConfigFilename) {

        ConnectionsConfig cc = new ConnectionsConfig();

        try {
            XStream xstream = new XStream();
            // [dcransac] Only Connections / Groups
            xstream.alias("Group", ConnectionGroup.class);
            xstream.alias("Connection", Connection.class);
            xstream.processAnnotations(Connection.class);
            xstream.processAnnotations(SamplingParameters.class);

            ConnectionGroup cg = (ConnectionGroup) xstream.fromXML(new File(connectionsConfigFilename));
            cc.setConnectionGroup(cg);

            return cc;

        } catch (Exception e) {
            logger.error("Unable to load " + connectionsConfigFilename, e);
            throw new RuntimeException("Unable to load " + connectionsConfigFilename, e);
        }
    }

    private static ConnectionsConfig parseConnectionsJson(String connectionsConfigFilename) {

        ConnectionsConfig cc = new ConnectionsConfig();

        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(InstrumentSubscription.class, InstrumentSubscriptionMixin.class);

        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, Connection.class);

        List<ConnectionGroupNode> connections;
        try {
            connections = mapper.readValue(new File(connectionsConfigFilename), type);
            ConnectionGroup group = new ConnectionGroup();
            group.groups = connections;
            cc.connectionGroup = group;
            return cc;
        } catch (IOException e) {
            String errorMsg = "Error while reading connection file: " + connectionsConfigFilename;
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }

    }

    private static ConnectionGroupNode parseAndBuildConnection(String csvLine) throws Exception {
        Connection connection = new io.djigger.collector.server.conf.Connection();

        String[] splitLine = csvLine.split(";");

        if (splitLine.length < 5)
            throw new Exception("The following JMX Config line is missing mandatory arguments:\n" + csvLine);

        // JMX Connection Properties
        Properties connectionProperties = new Properties();
        connectionProperties.setProperty(Facade.Parameters.HOST, splitLine[0]);
        connectionProperties.setProperty(Facade.Parameters.PORT, splitLine[1]);
        connectionProperties.setProperty(Facade.Parameters.USERNAME, splitLine[2]);
        connectionProperties.setProperty(Facade.Parameters.PASSWORD, resolvePasswordFromPath(splitLine[3]));

        //connectionProperties.setProperty(arg0, arg1)

        connection.setConnectionProperties(connectionProperties);

        // Sampling Rate Object
        SamplingParameters sp = new SamplingParameters();
        sp.setSamplingRate(Integer.parseInt(splitLine[4]));

        if (((splitLine.length - 5) % 2) != 0)
            throw new Exception("Mismatch in the attributes of the following JMX Config line (mod%2 !=0):\n" + csvLine);

        // Optional Attributes

        Map<String, String> attributes = new TreeMap<String, String>();
        for (int i = 5; i < splitLine.length; i += 2)
            attributes.put(splitLine[i], splitLine[i + 1]);

        connection.setConnectionProperties(connectionProperties);
        connection.setSamplingParameters(sp);
        connection.setAttributes(attributes);
        connection.setConnectionClass(JMXClientFacade.class.getCanonicalName());

        return connection;
    }

    private static String resolvePasswordFromPath(String pathToPassword) {

        // Default password = empty
        if (pathToPassword == null || pathToPassword.trim().isEmpty())
            return "";

        String password = null;
        String line = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(pathToPassword));
        } catch (FileNotFoundException e) {
            logger.error("Error while resolving password from file " + pathToPassword, e);
        }

        try {
            line = br.readLine();
        } catch (IOException e1) {
            logger.error("Error while resolving password from file " + pathToPassword, e1);
        }

        while (line != null) {
            if (line != null && line.length() > 0)
                password = line.trim();
            try {
                line = br.readLine();
            } catch (IOException e) {
                logger.error("Error while resolving password from file " + pathToPassword, e);
            }
        }
        return password;
    }

}
