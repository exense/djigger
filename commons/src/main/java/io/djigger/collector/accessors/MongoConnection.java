package io.djigger.collector.accessors;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;


public class MongoConnection {
	
    private static final Logger logger = LoggerFactory.getLogger(MongoConnection.class);

    private MongoClient mongoClient;

    private MongoDatabase db;
    
    private static int MAX_RETRIES = 60;

    public MongoDatabase connect(String host, int port, String user, String password) {
        Builder o = MongoClientOptions.builder().serverSelectionTimeout(3000);

        String databaseName = "djigger";

        List<MongoCredential> credentials = new ArrayList<>();
        if (user != null && password != null && !user.trim().isEmpty() && !password.trim().isEmpty()) {
            credentials.add(MongoCredential.createCredential(user, databaseName, password.toCharArray()));
        }

        mongoClient = new MongoClient(new ServerAddress(host, port), credentials, o.build());

        // call this method to check if the connection succeeded as the mongo client lazy loads the connection
        boolean isConnected = false;
        int tries = 0;
        while (!isConnected && tries < MAX_RETRIES) {
	        try {
	        	tries++;
	        	mongoClient.getAddress();
	        	isConnected = true;
	        } catch (MongoSocketOpenException | MongoTimeoutException e) {
	        	logger.warn("Unable to establish a connection to the mongo DB, retrying in 10 seconds...");
	        }
        }
        db = mongoClient.getDatabase(databaseName);
        return db;
    }

    public MongoDatabase getDb() {
        return db;
    }

    public void close() {
        mongoClient.close();
    }
}
