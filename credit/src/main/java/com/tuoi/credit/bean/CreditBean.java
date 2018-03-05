package com.tuoi.credit.bean;

import java.util.List;

import lombok.Data;

@Data
public class CreditBean {
	private byte[] id;
	
	private long createdTime;
	private long creditedTime;
	private byte[] customerId;
	private long value;
	private double rate;
	private int status;

	private String customerName;
	private String referer;
	private String phone;

	private List<Payment> payments;

	@Data
	public static class Payment {
		private byte[] id;
		private long timestamp;
		private int type;
		private long oriValue;
		private long value;
	}
}
