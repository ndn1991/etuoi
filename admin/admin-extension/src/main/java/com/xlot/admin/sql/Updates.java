package com.xlot.admin.sql;

import java.util.ArrayList;
import java.util.List;

public class Updates {
	private final List<String> updates = new ArrayList<>();

	public Updates update(String field, Object value) {
		this.updates.add(String.format("%s = %s", field, RepresentUtils.represent(value)));
		return this;
	}

	public boolean hasUpdate() {
		return !this.updates.isEmpty();
	}

	public String sqlString(String table, Clause clause) {
		return String.format("UPDATE %s SET %s WHERE %s", table, String.join(",", updates), clause.sqlString());
	}
}
