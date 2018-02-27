package com.tuoi.credit.bean;

import lombok.Data;

@Data
public class CustomerBean {
	private byte[] id;
	private String name;
	private String referer;
	private String phone;
	private int status;
}
