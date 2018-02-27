package com.xlot.admin.id;

import java.util.Base64;

import org.slf4j.LoggerFactory;

import com.nhb.common.data.PuObject;

import lombok.Data;

@Data
public class JwtLoginData {
	private final byte[] userId;
	private final String username;

	public PuObject toPuObject() {
		PuObject puo = new PuObject();
		puo.setString("uid", Base64.getEncoder().encodeToString(userId));
		puo.setString("un", username);
		return puo;
	}

	public static JwtLoginData fromPuObject(PuObject puo) {
		try {
			byte[] userId = Base64.getDecoder().decode(puo.getString("uid"));
			String username = puo.getString("un");
			JwtLoginData rs = new JwtLoginData(userId, username);
			return rs;
		} catch (Exception e) {
			LoggerFactory.getLogger(JwtLoginData.class).error("error", e);
			return null;
		}
	}

}
