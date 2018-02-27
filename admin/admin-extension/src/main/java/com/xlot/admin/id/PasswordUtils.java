package com.xlot.admin.id;

import org.apache.commons.codec.digest.DigestUtils;

import com.xlot.admin.statics.Status;

import scala.Tuple2;

public interface PasswordUtils {

	/**
	 * 
	 * @param rawPassword
	 * @return (salt, password)
	 */
	static Tuple2<String, String> getStoredPassword(String rawPassword) {
		String salt = com.nhb.common.utils.StringUtils.randomString(10);
		String storedPassword = DigestUtils.sha512Hex(String.format("%s%s", rawPassword, salt));
		return new Tuple2<String, String>(salt, storedPassword);
	}

	static Status verifyPassword(String rawPassword) {
		if (rawPassword.length() < 6 || rawPassword.length() > 32) {
			return Status.INVALID_PASSWORD_LENGTH;
		}
		return Status.SUCCESS;
	}

	static boolean isPasswordValid(String salt, String enterPassword, String storePassword) {
		return DigestUtils.sha512Hex(String.format("%s%s", enterPassword, salt)).equalsIgnoreCase(storePassword);
	}

	static String getVerifyCode(String hashKey, String prefix, String username) {
		String s = String.join(".", prefix, hashKey, username);
		int hash = 7;
		for (int i = 0; i < s.length(); i++) {
			hash = hash * 31 + s.charAt(i);
		}
		hash = hash & 0b111111111111111111111111111111;
		return String.format("%3s", StringUtils.base32(hash)).replaceAll(" ", "0");
	}

	public static void main(String[] args) {
		System.out.println(String.format("%3s", "a").replaceAll(" ", "0"));
	}
}
