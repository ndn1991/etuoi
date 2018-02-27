package com.xlot.sms.sender;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.nhb.common.vo.ByteArrayWrapper;
import com.xlot.sms.sender.vo.RemainTime;
import com.xlot.sms.sender.vo.RequestCount;

public interface RequestCountChecker {
	static final long MINUTE = 60l * 1000000000l;

	default RemainTime getRemainTime(byte[] userId, long delayNanoSeconds) {
		long now = System.nanoTime();
		ByteArrayWrapper key = new ByteArrayWrapper(userId);
		long fromTime = getUser2ReqCount().get(key).getFromTime().get();
		long result = delayNanoSeconds - (now - fromTime);
		return new RemainTime(result);
	}

	default RemainTime getRemainTime(String phone, long delayNanoSeconds) {
		long now = System.nanoTime();
		long fromTime = getPhone2ReqCount().get(phone).getFromTime().get();
		long result = delayNanoSeconds - (now - fromTime);
		return new RemainTime(result);
	}

	ConcurrentHashMap<ByteArrayWrapper, RequestCount> getUser2ReqCount();

	ConcurrentHashMap<String, RequestCount> getPhone2ReqCount();

	default boolean checkReqCount(String phone, long delayNanoseconds, int maxPerUnit) {
		long now = System.nanoTime();
		RequestCount urc = getPhone2ReqCount().get(phone);
		if (urc == null) {
			urc = new RequestCount(new AtomicInteger(1), new AtomicLong(now));
			getPhone2ReqCount().putIfAbsent(phone, urc);
			return true;
		}
		long toTime = urc.getFromTime().get() + delayNanoseconds;
		if (now > toTime) {
			urc.getFromTime().set(now);
			urc.getReqCount().set(1);
			return true;
		} else {
			return urc.getReqCount().incrementAndGet() <= maxPerUnit;
		}
	}

	default boolean checkReqCount(byte[] userId, long delayNanoseconds, int maxPerUnit) {
		long now = System.nanoTime();
		ByteArrayWrapper key = new ByteArrayWrapper(userId);
		RequestCount urc = getUser2ReqCount().get(key);
		if (urc == null) {
			urc = new RequestCount(new AtomicInteger(1), new AtomicLong(now));
			getUser2ReqCount().putIfAbsent(key, urc);
			return true;
		}
		long toTime = urc.getFromTime().get() + delayNanoseconds;
		if (now > toTime) {
			urc.getFromTime().set(now);
			urc.getReqCount().set(1);
			return true;
		} else {
			return urc.getReqCount().incrementAndGet() <= maxPerUnit;
		}
	}
}
