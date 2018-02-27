package com.xlot.admin.id;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.nhb.common.data.PuObject;
import com.nhb.common.encrypt.utils.EncryptionUtils;

public final class JWTUtils {
	private static final Logger log = LoggerFactory.getLogger(JwtLoginUtils.class);

	public static final String DATA = "d";
	public static final String EXPIRE = "e";

	public static String createJWT(Object data, String key, long timeout) throws GeneralSecurityException {
		long currentTime = System.currentTimeMillis();
		long expire = currentTime + timeout;
		log.debug("currentTime: {}", currentTime);
		log.debug("expire: {}", expire);
		log.debug("timeout: {}", timeout);
		PuObject puo = new PuObject();
		puo.setLong(EXPIRE, expire);
		puo.set(DATA, data);

		String encodedPayload = Base64.getEncoder().encodeToString(puo.toJSON().getBytes());
		String sign = Base64.getEncoder()
				.encodeToString(EncryptionUtils.hashHmacSHA256(encodedPayload.getBytes(), key.getBytes()));
		return String.join(".", encodedPayload, sign);
	}

	public static Object get(String jwt) {
		if (Strings.isNullOrEmpty(jwt)) {
			return null;
		}
		String[] arr = jwt.split("\\.");
		if (arr.length != 2) {
			return null;
		}
		String encodedPayload = arr[0];
		String _payload = new String(Base64.getDecoder().decode(encodedPayload));
		try {
			PuObject payload = PuObject.fromJSON(_payload);
			return payload.get(DATA);
		} catch (Exception e) {
			return null;
		}
	}

	public static Object verifyAndGet(String jwt, String key) {
		if (Strings.isNullOrEmpty(jwt)) {
			return null;
		}
		String[] arr = jwt.split("\\.");
		if (arr.length != 2) {
			return null;
		}
		String encodedPayload = arr[0];
		String sign;
		try {
			sign = Base64.getEncoder()
					.encodeToString(EncryptionUtils.hashHmacSHA256(encodedPayload.getBytes(), key.getBytes()));
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return null;
		}
		if (!sign.equals(arr[1])) {
			return null;
		}
		String _payload = new String(Base64.getDecoder().decode(encodedPayload));
		try {
			PuObject payload = PuObject.fromJSON(_payload);
			long currentTime = System.currentTimeMillis();
			long expire = payload.getLong(EXPIRE);
			if (expire <= currentTime) {
				log.debug("token expired: {}", jwt);
				return null;
			}
			return payload.get(DATA);
		} catch (Exception e) {
			return null;
		}
	}
}
