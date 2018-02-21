package com.xlot.sms.sender.vo;

public class RemainTime {
	private static final String SECOND_UNIT = "giây";
	private static final String MINUTE_UNIT = "phút";

	private static final int MINUTE = 60;

	private final long value;
	private final String unit;

	public RemainTime(long nanoSeconds) {
		long seconds = nanoSeconds / 1000000000l;
		if (seconds > MINUTE) {
			value = (long) Math.ceil((double) seconds / MINUTE);
			unit = MINUTE_UNIT;
		} else {
			value = seconds;
			unit = SECOND_UNIT;
		}
	}

	public String toString() {
		return value + " " + unit;
	}
}
