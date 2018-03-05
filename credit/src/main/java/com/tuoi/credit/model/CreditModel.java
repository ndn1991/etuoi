package com.tuoi.credit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

import com.mongodb.Function;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.nhb.common.db.models.AbstractModel;
import com.tuoi.credit.bean.CreditBean;
import com.tuoi.credit.bean.CreditBean.Payment;
import com.tuoi.credit.statics.F;

import jersey.repackaged.com.google.common.collect.Lists;

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

	public List<CreditBean> search(String content, int status, int from, int size) {
		Bson q = makeQuery(content, status);
		MongoIterable<CreditBean> cs = collection.find(q).map(new Function<Document, CreditBean>() {
			@Override
			public CreditBean apply(Document t) {
				return map(t);
			}
		});
		return Lists.newArrayList(cs);
	}

	@SuppressWarnings("unchecked")
	private CreditBean map(Document t) {
		CreditBean c = new CreditBean();
		c.setId(t.get("id", Binary.class).getData());
		c.setCreatedTime(t.getLong("createdTime"));
		c.setCreditedTime(t.getLong("creditedTime"));
		c.setCustomerId(t.get("customerId", Binary.class).getData());
		c.setValue(t.getLong("value"));
		c.setRate(t.getDouble("rate"));
		c.setStatus(t.getInteger("status"));
		c.setCustomerName(t.getString("customerName"));
		c.setReferer(t.getString("referer"));
		c.setPhone(t.getString("phone"));

		List<Payment> payments = new ArrayList<>();
		List<Document> paymentDocs = (List<Document>) t.get("payments");
		for (Document paymentDoc : paymentDocs) {
			Payment payment = new Payment();
			payment.setId(paymentDoc.get("id", Binary.class).getData());
			payment.setTimestamp(paymentDoc.getLong("timestamp"));
			payment.setType(paymentDoc.getInteger("type"));
			payment.setOriValue(paymentDoc.getLong("oriValue"));
			payment.setValue(paymentDoc.getLong("value"));
			payments.add(payment);
		}
		c.setPayments(payments);

		return c;
	}

	private Bson makeQuery(String content, int status) {
		Bson q1 = Filters.or(
				Filters.regex("customerName", Pattern.compile(".*" + content + ".*", Pattern.CASE_INSENSITIVE)), //
				Filters.regex("referer", Pattern.compile(".*" + content + ".*", Pattern.CASE_INSENSITIVE)), //
				Filters.regex("phone", Pattern.compile(".*" + content + ".*", Pattern.CASE_INSENSITIVE)),
				Filters.eq("value", number(content)));
		return Filters.and(q1, Filters.eq("status", status));
	}

	private long number(String s) {
		try {
			return Long.valueOf(s);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public int count(String content, int status) {
		return (int) collection.count(makeQuery(content, status));
	}
}
