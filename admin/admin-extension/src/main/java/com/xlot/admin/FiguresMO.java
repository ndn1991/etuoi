package com.xlot.admin;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import com.mario.entity.ManagedObject;
import com.mario.entity.impl.BaseLifeCycle;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.utils.FileSystemUtils;
import com.puppet.figures.client.FiguresClient;

public class FiguresMO extends BaseLifeCycle implements ManagedObject {
	private FiguresClient client;

	@Override
	public void init(PuObjectRO initParams) {
		UUID appId = UUID.fromString(initParams.getString("appId"));
		String topic = initParams.getString("topic");
		String configFile = initParams.getString("config");
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(
					FileSystemUtils.createAbsolutePathFrom("extensions", getExtensionName(), configFile)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		this.client = new FiguresClient(appId, topic, props);
	}

	@Override
	public Object acquire(PuObject requestParams) {
		return client;
	}

	@Override
	public void release(Object object) {

	}

}
