package com.xlot.admin;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.gaia.ams.client.AMSClient;
import com.mario.entity.ManagedObject;
import com.mario.entity.impl.BaseLifeCycle;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.utils.Converter;
import com.nhb.common.utils.FileSystemUtils;

public class AmsMO extends BaseLifeCycle implements ManagedObject {
	private AMSClient amsClient;

	@Override
	public void init(PuObjectRO initParams) {
		String configFile = initParams.getString("configFile");
		String path = FileSystemUtils.createAbsolutePathFrom("extensions", getExtensionName(), configFile);
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(path));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		byte[] appId = Converter.uuidToBytes(initParams.getString("appId"));
		this.amsClient = new AMSClient(appId, props);
	}

	@Override
	public Object acquire(PuObject requestParams) {
		return amsClient;
	}

	@Override
	public void release(Object object) {

	}

}
