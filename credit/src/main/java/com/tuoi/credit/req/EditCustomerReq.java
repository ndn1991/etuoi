package com.tuoi.credit.req;

import com.ndn.common.request.Request;

import lombok.Data;

@Data
public class EditCustomerReq implements Request {
	private byte[] id;
	private String name;
	private String referer;
	private String phone;
	private int status;
}
