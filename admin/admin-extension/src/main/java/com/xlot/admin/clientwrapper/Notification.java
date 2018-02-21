package com.xlot.admin.clientwrapper;

import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Notification {
	private static final String SANDBOX = "sandbox";
	private static final String TOKEN = "token";
	private static final String BUNDLE_ID = "bundleId";
	private static final String SERVICE_TYPE = "serviceType";
	private static final String MESSAGE = "message";
	private static final String TITLE = "title";
	private String title;
	private String message;
	private String serviceType;
	private boolean sandbox = false;
	private String bundleId;
	private String token;

	public PuObject toPuObject() {
		PuObject puo = new PuObject();
		if (title != null) {
			puo.set(TITLE, title);
		}
		if (message != null) {
			puo.set(MESSAGE, message);
		}
		if (serviceType != null) {
			puo.set(SERVICE_TYPE, serviceType);
		}
		if (bundleId != null) {
			puo.set(BUNDLE_ID, bundleId);
		}
		if (token != null) {
			puo.set(TOKEN, token);
		}
		puo.set(SANDBOX, sandbox);
		return puo;
	}

	public static Notification fromPuObject(PuObjectRO data) {
		Notification noti = new Notification();
		noti.setTitle(data.getString(TITLE, null));
		noti.setMessage(data.getString(MESSAGE, null));
		noti.setSandbox(data.getBoolean(SANDBOX, false));
		noti.setServiceType(data.getString(SERVICE_TYPE, null));
		if (noti.getServiceType() != null && noti.getServiceType().length() < 3) {
			noti.setServiceType(null);
		}
		noti.setToken(data.getString(TOKEN, null));
		noti.setBundleId(data.getString(BUNDLE_ID, null));
		return noti;
	}
}