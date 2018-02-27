package com.tuoi.credit.req;

import com.ndn.common.request.Request;

import lombok.Data;

@Data
public class CreateCreditReq implements Request {
	private long creditedTime;
	private byte[] customerId;
	private long value;
	private double rate;
	private int status;
}
