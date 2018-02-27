package com.tuoi.credit.model;

import java.util.Collections;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.nhb.common.db.models.AbstractModel;
import com.tuoi.credit.bean.CreditBean;
import com.tuoi.credit.statics.F;

public class CreditModel extends AbstractModel {
	private MongoCollection<Document> collection;

	@Override
	protected void init() {
		if (getMongoClient() == null) {
			throw new RuntimeException("mongo missing");
		}

		MongoDatabase database = getMongoClient().getDatabase((String) getEnvironmentVariables().get(F.DATABASE));
		collection = database.getCollection("credit");
	}
	
	public void insert(CreditBean c) {
		Document d = new Document() //
				.append("id", c.getCustomerId()) //
				.append("createdTime", System.currentTimeMillis()) //
				.append("creditedTime", c.getCreditedTime()) //
				.append("customerId", c.getCustomerId()) //
				.append("value", c.getValue()) //
				.append("rate", c.getRate()) //
				.append("status", c.getStatus()) //
				.append("customerName", c.getCustomerName()) //
				.append("referer", c.getReferer()) //
				.append("phone", c.getPhone()) //
				.append("payments", Collections.emptyList());
		collection.insertOne(d);
	}
}
