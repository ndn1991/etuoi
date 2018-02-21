package com.xlot.sms.sender.vo;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Data;

@Data
public class RequestCount {
	private final AtomicInteger reqCount;
	private final AtomicLong fromTime;
}
