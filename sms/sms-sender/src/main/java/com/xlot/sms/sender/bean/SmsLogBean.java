package com.xlot.sms.sender.bean;

import java.util.Collection;
import java.util.HashSet;

import com.xlot.sms.sender.SendSMSResult;

import lombok.Data;

@Data
public class SmsLogBean {
	private byte[] id;
	private byte[] logId;
	private boolean timeout;
	private int status;
	private SendSMSResult result;
	private final Collection<String> sendPhones = new HashSet<>();
	private final Collection<String> invalidPhones = new HashSet<>();
}
