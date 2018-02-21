package com.xlot.admin.sql;

import lombok.Data;

@Data
public class LikeBeforeClause implements Clause {
	private final String field;
	private final String value;

	@Override
	public String sqlString() {
		return String.format("%s LIKE '%s%%'", getField(), getValue());
	}
}
