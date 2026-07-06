package io.djigger.collector.accessors;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;


public class MongoConnection {

    private static final Logger logger = LoggerFactory.getLogger(MongoConnection.class);

    private MongoClient mongoClient;

    private MongoDatabase db;

    private static int MAX_RETRIES = 60;

    public MongoDatabase connect(String host, int port, String user, String password) {
        String databaseName = "djigger";

        com.mongodb.MongoClientSettings.Builder settingsBuilder = com.mongodb.MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder
                        .hosts(List.of(new ServerAddress(host, port)))
                        .serverSelectionTimeout(3000, TimeUnit.MILLISECONDS));

        if (user != null && password != null && !user.trim().isEmpty() && !password.trim().isEmpty()) {
            settingsBuilder.credential(MongoCredential.createCredential(user, databaseName, password.toCharArray()));
        }

        mongoClient = MongoClients.create(settingsBuilder.build());
        db = mongoClient.getDatabase(databaseName);

        // the mongo client lazy loads the connection, ping the server to check that the connection succeeded
        boolean isConnected = false;
        int tries = 0;
        while (!isConnected && tries < MAX_RETRIES) {
	        try {
	        	tries++;
	        	db.runCommand(new Document("ping", 1));
	        	isConnected = true;
	        } catch (MongoException e) {
	        	logger.warn("Unable to establish a connection to the mongo DB, retrying in 10 seconds...");
	        }
        }
        return db;
    }

    public MongoDatabase getDb() {
        return db;
    }

    public void close() {
        mongoClient.close();
    }
}
