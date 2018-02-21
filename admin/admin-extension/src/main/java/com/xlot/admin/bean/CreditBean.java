package com.xlot.admin.bean;

import java.util.Base64;

import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.utils.UUIDUtils;
import com.xlot.admin.id.StringUtils;

import lombok.Data;

@Data
public class CreditBean {
	private byte[] id;
	private long createdTime;
	private long creditedTime;
	private String name;
	private String nameNonUTF8;
	private String introducer;
	private String introducerNonUTF8;
	private String description;
	private String descriptionNonUTF8;
	private String phone;
	private long value;
	private double interestRate;
	private int status;
	
	public CreditBean fromPuo(PuObject puo) {
		CreditBean c = new CreditBean();
		c.id = getRaw(puo, "id");
		c.createdTime = System.currentTimeMillis();
		c.cre;
		c.name = puo.getString("name");
		c.nameNonUTF8 = StringUtils.unAccent(c.name);
		c.introducer = puo.getString("introducer");
		c.introducerNonUTF8 = StringUtils.unAccent(c.introducer);
		c.description = puo.getString("introducer");
		c.descriptionNonUTF8 = StringUtils.unAccent(c.description);
		c.phone = puo.getString("phone");
		c.value = puo.getLong("phone");
		c.interestRate = puo.getDouble("interestRate");
		c.status = puo.getInteger("status");
		return c;
	}
	
	private byte[] getRaw(PuObjectRO puo, String field) {
		if (puo.variableExists(field)) {
			PuDataType type = puo.typeOf(field);
			if (type == PuDataType.RAW) {
				return puo.getRaw(field);
			}
			if (type == PuDataType.STRING) {
				String s = puo.getString(field);
				try {
					return UUIDUtils.uuidToBytes(s);
				} catch (Exception e) {
					return Base64.getDecoder().decode(s);
				}
			}
			if (type == PuDataType.NULL) {
				return null;
			}
			throw new RuntimeException("can not get byte array for type: " + type);
		}
		return null;
	}
}
