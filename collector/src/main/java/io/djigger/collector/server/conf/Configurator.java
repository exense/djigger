/*******************************************************************************
 * (C) Copyright  2016 Dorian Cransac and others.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *    - Dorian Cransac (dcransac)
 *    - Jérôme Comte
 *******************************************************************************/


/*******************************************************************************
 * Configurator : practical static methods used to load various parts of the collector's configuration  
/*******************************************************************************/

package io.djigger.collector.server.conf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import io.djigger.client.JMXClientFacade;

public class Configurator {

	private static final Logger logger = LoggerFactory.getLogger(Configurator.class);

	public static CollectorConfig parseCollectorConfiguration(String collConfigFilename) throws Exception {
		
		if(collConfigFilename == null || collConfigFilename.trim().isEmpty())
			throw new Exception("Invalid collector config file : " + collConfigFilename + ". Check your -DcollectorConfig option.");
		
		try {
			XStream xstream = new XStream();
			xstream.alias("Collector", CollectorConfig.class);
			// [dcransac] Since connections and collector settings are now decoupled
			//xstream.alias("Group", ConnectionGroup.class);
			//xstream.alias("Connection", Connection.class);
			//xstream.processAnnotations(Connection.class);
			//xstream.processAnnotations(SamplingParameters.class);
			xstream.processAnnotations(MongoDBParameters.class);
			return (CollectorConfig) xstream.fromXML(new File(collConfigFilename));
		} catch (Exception e) {
			logger.error("Unable to load " + collConfigFilename + " from ClassLoader.", e);
			throw new RuntimeException("Unable to load " + collConfigFilename + " from ClassLoader.", e);
		}
	}

	public static ConnectionsConfig parseConnectionsConfiguration(String connectionsConfigFilename) throws Exception {

		if(connectionsConfigFilename == null || connectionsConfigFilename.trim().isEmpty())
			throw new Exception("Invalid connections config file : " + connectionsConfigFilename  + ". Check your -DconnectionsConfig option.");

		if(connectionsConfigFilename.trim().toLowerCase().endsWith(".xml"))
			return parseConnectionsXML(connectionsConfigFilename);
		if(connectionsConfigFilename.trim().toLowerCase().endsWith(".csv"))
			return parseConnectionsCSV(connectionsConfigFilename);

		throw new Exception("Unknown connections config file type for file : " + connectionsConfigFilename + ". Please use a .xml or .csv file.");
	}

	/*
	 * @author dcransac
	 * @since 16.03.2016
	 *
	 * Provide a simpler mechanism based on CSV Files to specify and load JMX Connection information
	 *
	 */
	private static ConnectionsConfig parseConnectionsCSV(String connectionsConfigFilename) {

		ConnectionsConfig cc = new ConnectionsConfig();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(connectionsConfigFilename));
		} catch (FileNotFoundException e1) {
			logger.error("Error while parsing connection definitions from file "+connectionsConfigFilename,e1);
		}
		String line = null;
		try {
			line = br.readLine();
		} catch (IOException e1) {
			logger.error("Error while parsing connection definitions from file "+connectionsConfigFilename,e1);
		}

		// Build a single ConnectionGroup to hold all Connections with flat, potentially redundant attributes
		ConnectionGroup cg = new ConnectionGroup();
		List<ConnectionGroupNode> cgnList = new ArrayList<ConnectionGroupNode>();

		// @bugfix (cosmetic, since exception gets caught anyway)
	    // empty line bug : test if line contains something
		while (line != null && !line.trim().isEmpty())
		{
			try {
				cgnList.add(parseAndBuildConnection(line));
			} catch (Exception e) {
				logger.error("Error while parsing line "+line,e);
			}
			try {
				line = br.readLine();
			} catch (IOException e) {
				logger.error("Error while reading line",e);
			}
		}

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

	private static ConnectionGroupNode parseAndBuildConnection(String csvLine) throws Exception {
		Connection connection = new io.djigger.collector.server.conf.Connection();

		String[] splitLine = csvLine.split(";");

		if(splitLine.length < 5)
			throw new Exception("The following JMX Config line is missing mendatory arguments:\n" + csvLine);

		// JMX Connection Properties
		Properties connectionProperties = new Properties();
		connectionProperties.setProperty("host", splitLine[0]);
		connectionProperties.setProperty("port", splitLine[1]);
		connectionProperties.setProperty("username", splitLine[2]);
		connectionProperties.setProperty("password", resolvePasswordFromPath(splitLine[3]));
		connection.setConnectionProperties(connectionProperties);

		// Sampling Rate Object
		SamplingParameters sp = new SamplingParameters();
		sp.setSamplingRate(Integer.parseInt(splitLine[4]));

		if(((splitLine.length -5) %2 ) != 0)
			throw new Exception("Mismatch in the attributes of the following JMX Config line (mod%2 !=0):\n" + csvLine);

		// Optional Attributes

		Map<String,String> attributes = new TreeMap<String,String>();
		for(int i = 5; i < splitLine.length; i+=2)
			attributes.put(splitLine[i], splitLine[i+1]);

		connection.setConnectionProperties(connectionProperties);
		connection.setSamplingParameters(sp);
		connection.setAttributes(attributes);
		connection.setConnectionClass(JMXClientFacade.class.getCanonicalName());

		return connection;
	}

	private static String resolvePasswordFromPath(String pathToPassword) {

		// Default password = empty
		if(pathToPassword == null || pathToPassword.trim().isEmpty())
			return "";
		
		String password = null;
		String line = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(pathToPassword));
		} catch (FileNotFoundException e)
		{ logger.error("Error while resolving password from file "+pathToPassword,e);}

		try {
			line = br.readLine();
		} catch (IOException e1)
		{ logger.error("Error while resolving password from file "+pathToPassword,e1);}

		while (line != null)
		{
			if(line != null && line.length() > 0)
				password = line.trim();
			try {
				line = br.readLine();
			} catch (IOException e)
			{  logger.error("Error while resolving password from file "+pathToPassword,e);}
		}
		return password;
	}

}
