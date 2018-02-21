package com.xlot.admin.bean;

import com.nhb.common.data.PuObject;
import com.nhb.common.utils.UUIDUtils;
import com.xlot.admin.id.UserStatus;
import com.xlot.admin.statics.UserType;

import lombok.Data;
import ndn.GeneratePuMethod;

@Data
public class UserBean {
	private byte[] userId;
	private String username;
	private String password;
	private String hashKey;
	private String salt;
	private int status = UserStatus.ACTIVE;
	private long timestamp;
	private int type = UserType.NORMAL;
	private byte[] refId = null;

	public PuObject toPuObject() {
		PuObject puo = new PuObject();
		puo.setString("userId", UUIDUtils.bytesToUUIDString(userId));
		puo.setString("username", username);
//		puo.setString("password", password);
//		puo.setString("hashKey", hashKey);
//		puo.setString("salt", salt);
		puo.setInteger("status", status);
		puo.setLong("timestamp", timestamp);
		puo.setInteger("type", type);
		puo.setString("refId",  refId == null ? null : UUIDUtils.bytesToUUIDString(refId));
		return puo;
	}
	
	public static void main(String[] args) {
		System.out.println(GeneratePuMethod.generateToPuObject(UserBean.class));
	}
}
