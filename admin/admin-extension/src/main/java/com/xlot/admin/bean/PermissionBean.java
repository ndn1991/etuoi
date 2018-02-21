package com.xlot.admin.bean;

import com.nhb.common.data.PuObject;
import com.nhb.common.utils.UUIDUtils;

import lombok.Data;
import ndn.GeneratePuMethod;

@Data
public class PermissionBean {
	private byte[] id;
	private String name;
	private String description;
	private long createdTime;

	public static void main(String[] args) {
		System.out.println(GeneratePuMethod.generateToPuObject(PermissionBean.class));
	}

	public PuObject toObject() {
		PuObject puo = new PuObject();
		puo.setLong("createdTime", createdTime);
		puo.setString("description", description);
		puo.setString("name", name);
		puo.setString("id", UUIDUtils.bytesToUUIDString(id));
		return puo;
	}
}
