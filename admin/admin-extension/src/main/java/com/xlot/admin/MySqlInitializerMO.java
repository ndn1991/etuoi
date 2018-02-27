package com.xlot.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.skife.jdbi.v2.Handle;

import com.google.common.base.Joiner;
import com.mario.entity.ManagedObject;
import com.mario.entity.impl.BaseLifeCycle;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.db.sql.DBIAdapter;
import com.nhb.common.utils.FileSystemUtils;

public class MySqlInitializerMO extends BaseLifeCycle implements ManagedObject {

	@Override
	public void init(PuObjectRO initParams) {
		String prefix = FileSystemUtils.createAbsolutePathFrom("extensions", getExtensionName());
		String sqlPaths = initParams.getString("sqlPaths");
		DBIAdapter dbiAdapter = getApi().getDatabaseAdapter(initParams.getString("mysql"));
		try {
			init(dbiAdapter, prefix, sqlPaths.split(","));
		} catch (IOException e) {
			getLogger().error("error when init mysql {}", getName(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object acquire(PuObject arg0) {
		return null;
	}

	@Override
	public void release(Object arg0) {
	}

	private void init(DBIAdapter adapter, String prefix, String... paths) throws IOException {
		for (String path : paths) {
			List<String> lines = Files.readAllLines(Paths.get(FileSystemUtils.createPathFrom(prefix, path)));
			String sql = Joiner.on('\n').join(lines);
			getLogger().debug("content of file: {}\n{}", path, sql);
			try (Handle handle = adapter.newHandle()) {
				handle.execute(sql);
			}
		}
	}
}
