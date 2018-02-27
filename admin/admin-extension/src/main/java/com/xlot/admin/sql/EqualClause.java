package com.xlot.admin.sql;

import lombok.Data;

@Data
public class EqualClause implements Clause {
	private final String field;
	private final Object value;

	@Override
	public String sqlString() {
		return String.format("%s=%s", field, RepresentUtils.represent(value));
	}

}
