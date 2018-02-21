package com.xlot.admin.vo;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CreditQuery {
	private String name;
	private String introducer;
	private String description;
	private double fromInterestRate;
	private double toInterestRate;
	private long fromValue;
	private long toValue;
	private long fromCreatedTime;
	private long toCreatedTime;
	private long fromCreditedTime;
	private long toCreditedTime;
}
