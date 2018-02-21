package com.xlot.admin.bean;

import com.nhb.common.data.PuObject;
import com.nhb.common.utils.UUIDUtils;

import lombok.Data;

@Data
public class RoleBean {
	private byte[] id;
	private String name;
	private String description;
	private long createdTime;

	public PuObject toObject() {
		PuObject puo = new PuObject();
		puo.setLong("createdTime", createdTime);
		puo.setString("description", description);
		puo.setString("name", name);
		puo.setString("id", UUIDUtils.bytesToUUIDString(id));
		return puo;
	}
}
