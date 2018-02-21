package com.xlot.admin.sql;

import lombok.Data;

@Data
public class CompareClause implements Clause {
	private final String field;
	private final String comparator;
	private final Object value;

	@Override
	public String sqlString() {
		return String.format("%s %s %s", field, comparator, RepresentUtils.represent(value));
	}

}
