package com.xlot.sms.sender.model;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.nhb.common.db.models.AbstractModel;
import com.xlot.sms.sender.bean.SmsLogBean;
import com.xlot.sms.sender.statics.F;
import com.xlot.sms.sender.statics.Utils;

public class MongoSmsLogModel extends AbstractModel implements SmsLogModel {
	private MongoCollection<Document> logCollection;

	@Override
	protected void init() {
		if (getMongoClient() == null) {
			throw new RuntimeException("mongo missing");
		}
		String database = (String) getEnvironmentVariables().get(F.DATABASE);
		if (database == null) {
			throw new RuntimeException("database name missing");
		}
		MongoDatabase mongoDatabase = getMongoClient().getDatabase(database);
		logCollection = mongoDatabase.getCollection(F.LOG);
	}

	@Override
	public void log(SmsLogBean bean) {
		logCollection.insertOne(Utils.toDocument(bean));
	}

}
