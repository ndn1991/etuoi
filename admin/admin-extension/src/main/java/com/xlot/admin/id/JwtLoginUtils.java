package com.xlot.admin.id;

import java.security.GeneralSecurityException;

public interface JwtLoginUtils {
	static String token(byte[] userId, String username, String key, long timeout) throws GeneralSecurityException {
		JwtLoginData data = new JwtLoginData(userId, username);
		return JWTUtils.createJWT(data.toPuObject(), key, timeout);
	}
}
