package com.xlot.admin.id;

import java.text.Normalizer;
import java.util.regex.Pattern;

public interface StringUtils {
	String USERNAME_REGEX = "^[a-zA-Z_$][a-zA-Z_$0-9]*$";

	Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

	static String unAccent(String s) {
		String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
		return pattern.matcher(temp).replaceAll("").replaceAll("Đ", "D").replaceAll("đ", "d")
				.replaceAll("[^a-zA-Z0-9_]", "");
	}

	static String unAccentWithoutSpace(String s) {
		String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
		return pattern.matcher(temp).replaceAll("").replaceAll("Đ", "D").replaceAll("đ", "d")
				.replaceAll("[^a-zA-Z0-9_ ]", "");
	}

	char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', //
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', //
			'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	static String base32(int n) {
		return toUnsignedString(n, 5);
	}

	static String toUnsignedString(int val, int shift) {
		// assert shift > 0 && shift <=5 : "Illegal shift value";
		int mag = Integer.SIZE - Integer.numberOfLeadingZeros(val);
		int chars = Math.max(((mag + (shift - 1)) / shift), 1);
		char[] buf = new char[chars];

		formatUnsignedInt(val, shift, buf, 0, chars);

		return new String(buf);
	}

	static int formatUnsignedInt(int val, int shift, char[] buf, int offset, int len) {
		int charPos = len;
		int radix = 1 << shift;
		int mask = radix - 1;
		do {
			buf[offset + --charPos] = digits[val & mask];
			val >>>= shift;
		} while (val != 0 && charPos > 0);

		return charPos;
	}

	static String getMaxName(String realLastName, int sizeLimit) {
		int index = realLastName.indexOf(' ');
		if (index == -1) {
			return realLastName;
		}
		String sub = realLastName.substring(index + 1);
		int length = StringUtils.unAccent(sub).length();
		if (length <= sizeLimit) {
			return sub;
		}
		return getMaxName(sub, sizeLimit);
	}
}
