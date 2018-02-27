package com.xlot.admin.sql;

import org.apache.commons.codec.binary.Hex;

public class RepresentUtils {
	public static String represent(Object value) {
		if (value instanceof byte[]) {
			return String.format("0x%s", Hex.encodeHexString((byte[]) value));
		}
		if (value instanceof Integer || value instanceof Long || value instanceof Byte || value instanceof Short) {
			return String.valueOf(value);
		}
		if (value instanceof Boolean) {
			return String.valueOf(value);
		}
		return String.format("'%s'", String.valueOf(value));
	}
}
