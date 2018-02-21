package com.xlot.admin.statics;

import org.bson.Document;
import org.bson.types.Binary;

import com.xlot.admin.bean.CreditBean;

public interface CreditUtils {
	default Document toDocument(CreditBean c) {
		return new Document() //
				.append("id", c.getId()) //
				.append("createdTime", c.getCreatedTime()) //
				.append("creditedTime", c.getCreditedTime()) //
				.append("name", c.getName()) //
				.append("nameNonUTF8", c.getNameNonUTF8()) //
				.append("introducer", c.getIntroducer()) //
				.append("introducerNonUTF8", c.getIntroducerNonUTF8()) //
				.append("description", c.getDescription()) //
				.append("descriptionNonUTF8", c.getDescriptionNonUTF8()) //
				.append("phone", c.getPhone()) //
				.append("value", c.getValue()) //
				.append("interestRate", c.getInterestRate()) //
				.append("status", c.getStatus());
	}

	default CreditBean fromDocument(Document d) {
		CreditBean c = new CreditBean();
		c.setId(d.get("id", Binary.class).getData());
		c.setCreatedTime(d.getLong("createdTime"));
		c.setCreditedTime(d.getLong("creditedTime"));
		c.setName(d.getString("name"));
		c.setNameNonUTF8(d.getString("nameNonUTF8"));
		c.setIntroducer(d.getString("introducer"));
		c.setIntroducerNonUTF8(d.getString("introducerNonUTF8"));
		c.setDescription(d.getString("description"));
		c.setDescriptionNonUTF8(d.getString("descriptionNonUTF8"));
		c.setPhone(d.getString("phone"));
		c.setValue(d.getLong("value"));
		c.setInterestRate(d.getDouble("interestRate"));
		c.setStatus(d.getInteger("status"));
		return c;
	}
}
