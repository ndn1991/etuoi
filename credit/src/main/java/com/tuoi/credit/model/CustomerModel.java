package com.tuoi.credit.model;

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
import com.mongodb.client.model.Updates;
import com.nhb.common.db.models.AbstractModel;
import com.tuoi.credit.bean.CustomerBean;
import com.tuoi.credit.statics.F;

import jersey.repackaged.com.google.common.collect.Lists;

public class CustomerModel extends AbstractModel {
	private MongoCollection<Document> collection;

	@Override
	protected void init() {
		if (getMongoClient() == null) {
			throw new RuntimeException("mongo missing");
		}

		MongoDatabase database = getMongoClient().getDatabase((String) getEnvironmentVariables().get(F.DATABASE));
		collection = database.getCollection("customer");
	}

	public int insert(CustomerBean c) {
		Document d = new Document().append("id", c.getId()) //
				.append("name", c.getName()) //
				.append("referer", c.getReferer()) //
				.append("phone", c.getPhone()) //
				.append("status", c.getStatus());
		try {
			collection.insertOne(d);
			return 1;
		} catch (Exception e) {
			getLogger().warn("exception when insert doc: {}", d, e);
			return 0;
		}
	}

	public int update(CustomerBean c) {
		Bson update = Updates.combine(Updates.set("name", c.getName()), //
				Updates.set("referer", c.getReferer()), //
				Updates.set("phone", c.getPhone()), //
				Updates.set("status", c.getStatus()));
		Document doc = collection.findOneAndUpdate(Filters.eq("id", c.getId()), update);
		return doc == null ? 0 : 1;
	}

	public CustomerBean fetch(byte[] customerId) {
		return collection.find(Filters.eq("id", customerId)).map(new Function<Document, CustomerBean>() {
			@Override
			public CustomerBean apply(Document t) {
				CustomerBean c = map(t);
				return c;
			}
		}).first();
	}

	public List<CustomerBean> search(String content) {
		Bson q = makeQuery(content);
		MongoIterable<CustomerBean> cs = collection.find(q).map(new Function<Document, CustomerBean>() {
			@Override
			public CustomerBean apply(Document t) {
				return map(t);
			}
		});
		return Lists.newArrayList(cs);
	}

	private Bson makeQuery(String content) {
		Bson q = Filters.or(Filters.regex("name", Pattern.compile(".*" + content + ".*", Pattern.CASE_INSENSITIVE)), //
				Filters.regex("referer", Pattern.compile(".*" + content + ".*", Pattern.CASE_INSENSITIVE)), //
				Filters.regex("phone", Pattern.compile(".*" + content + ".*", Pattern.CASE_INSENSITIVE)));
		return q;
	}

	private CustomerBean map(Document t) {
		CustomerBean c = new CustomerBean();
		c.setId(t.get("id", Binary.class).getData());
		c.setName(t.getString("name"));
		c.setReferer(t.getString("referer"));
		c.setPhone(t.getString("phone"));
		c.setStatus(t.getInteger("status"));
		return c;
	}

	public int count(String content, int skip, int limit) {
		Bson q = makeQuery(content);
		return (int) collection.count(q);
	}
}
