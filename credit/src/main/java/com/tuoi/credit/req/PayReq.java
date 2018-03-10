package com.tuoi.credit.req;

import com.ndn.common.request.Request;

import lombok.Data;

@Data
public class PayReq implements Request {
	private byte[] id;
	private long value;
	private int type;
}
