package com.xlot.admin.processor;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;

public interface AdminProcessor {
	static final String ADMIN_ID = "_adminId_";
	static final String ADMIN_NAME = "_adminName_";
	
	boolean isRequireLoggedIn();
	void setRequireLoggedIn(boolean b);
	
	String getPermission();
	void setPermission(String permission);
	
	void init(PuObjectRO params);

	RPCFuture<PuElement> process(PuObjectRO params);
}
