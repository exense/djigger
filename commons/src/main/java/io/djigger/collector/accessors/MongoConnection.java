package io.djigger.collector.accessors;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {

	private MongoClient mongoClient;
	
	private MongoDatabase db;
	
	public MongoDatabase connect(String host) {
		Builder o = MongoClientOptions.builder().serverSelectionTimeout(3000);  
		mongoClient = new MongoClient(host, o.build());
		
		// call this method to check if the connection succeeded as the mongo client lazy loads the connection 
		mongoClient.getAddress();
		
		db = mongoClient.getDatabase("djigger");
		return db;
	}
	
	public MongoDatabase getDb() {
		return db;
	}

	public void close() {
		mongoClient.close();
	}
}
