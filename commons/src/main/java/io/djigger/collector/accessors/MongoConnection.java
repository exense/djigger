package io.djigger.collector.accessors;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.List;

public class MongoConnection {

    private MongoClient mongoClient;

    private MongoDatabase db;

    public MongoDatabase connect(String host, int port, String user, String password) {
        Builder o = MongoClientOptions.builder().serverSelectionTimeout(3000);

        String databaseName = "djigger";

        List<MongoCredential> credentials = new ArrayList<>();
        if (user != null && password != null && !user.trim().isEmpty() && !password.trim().isEmpty()) {
            credentials.add(MongoCredential.createCredential(user, databaseName, password.toCharArray()));
        }

        mongoClient = new MongoClient(new ServerAddress(host, port), credentials, o.build());

        // call this method to check if the connection succeeded as the mongo client lazy loads the connection
        mongoClient.getAddress();

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
