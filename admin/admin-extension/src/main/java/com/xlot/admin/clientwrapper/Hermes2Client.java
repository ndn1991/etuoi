package com.xlot.admin.clientwrapper;

import java.io.Closeable;
import java.io.IOException;

import org.apache.http.HttpResponse;

import com.nhb.common.BaseLoggable;
import com.nhb.common.async.RPCFuture;
import com.nhb.common.async.translator.RPCFutureTranslator;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.messaging.http.HttpAsyncFuture;
import com.nhb.messaging.http.HttpClientHelper;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Hermes2Client extends BaseLoggable implements Closeable {
	private static final String TOTAL = "total";
	private static final String ITEMS = "items";
	private static final String DATA = "data";
	private static final String AUTHENTICATOR = "authenticator";
	private static final String STATUS = "status";
	private static final String ID = "id";
	private static final String COMMAND = "command";
	private static final String APP_ID = "appId";
	private static final String BUNDLE_ID = "bundleId";
	private static final String SERVICE_TYPE = "serviceType";

	private String appId;
	private HttpClientHelper httpClient;
	private String url;
	@Setter
	private long timeout;

	public static final String PUSH = "push";
	public static final String ADMIN = "admin";
	public static final String REGISTER = "register";

	public Hermes2Client(String appId, String url) {
		this.appId = appId;
		this.url = url;
		this.httpClient = new HttpClientHelper();
		httpClient.setUsingMultipath(false);
	}

	public RPCFuture<PuElement> send(PuObject data, String path) {
		data.set(APP_ID, appId);
		HttpAsyncFuture future = httpClient.executeAsyncPost(getUrl() + "/" + path, data);
		return new RPCFutureTranslator<HttpResponse, PuElement>(future) {
			@Override
			protected PuElement translate(HttpResponse sourceResult) throws Exception {
				if (sourceResult != null) {
					return HttpClientHelper.handleResponse(sourceResult);
				}
				return null;
			}
		};
	}

	public RPCFuture<PuElement> pushNotification(Notification notification) {
		PuObject puo = notification.toPuObject();
		puo.set(APP_ID, this.appId);
		puo.set(COMMAND, "push");
		return this.send(puo, Hermes2Client.PUSH);
	}

	public RPCFuture<PuElement> resetService(String serviceId) {
		PuObject data = new PuObject();
		data.set(COMMAND, "resetService");
		data.set(ID, serviceId);
		return this.send(data, Hermes2Client.PUSH);
	}

	public RPCFuture<PuElement> addAuthenticator(PuObject data) {
		data.set(COMMAND, "addAuthenticator");
		if (!data.variableExists(AUTHENTICATOR) || !data.variableExists(BUNDLE_ID)
				|| !data.variableExists(SERVICE_TYPE)) {
			return null;
		}
		System.out.println("add authen " + data);
		return this.send(data, Hermes2Client.REGISTER);
	}

	public RPCFuture<PuElement> getAuthenticator() {
		PuObject data = new PuObject();
		data.set(COMMAND, "getAuthenticators");
		data.set(APP_ID, this.appId);
		RPCFuture<PuElement> future = this.send(data, Hermes2Client.ADMIN);
		return new RPCFutureTranslator<PuElement, PuElement>(future) {
			@Override
			protected PuElement translate(PuElement sourceResult) throws Exception {
				if (sourceResult != null) {
					PuObject puo = (PuObject) sourceResult;
					try {
						if (puo.getInteger(STATUS) == 0) {
							PuArray arr = puo.getPuObject(DATA).getPuArray(AUTHENTICATOR);
							PuObject pu = new PuObject();
							pu.set(ITEMS, arr);
							pu.set(TOTAL, arr.size());
							puo.set(DATA, pu);
						}
					} catch (Exception e) {
						getLogger().error("error", e);
						return null;
					}
				}
				return null;
			}
		};
	}

	public RPCFuture<PuElement> removeAuthenticator(String id) {
		PuObject data = new PuObject();
		data.set(COMMAND, "removeAuthenticator");
		data.set(ID, id);
		return this.send(data, Hermes2Client.ADMIN);
	}

	public PuElement updateAuthenticator() {
		return null;
	}

	public RPCFuture<PuElement> checkPushTask(String taskId) {
		PuObject data = new PuObject();
		data.set(COMMAND, "checkTask");
		data.set(ID, taskId);
		return this.send(data, Hermes2Client.PUSH);
	}

	public RPCFuture<PuElement> getPushTask(long from, long to, int skip, int limit) {
		PuObject data = new PuObject();
		data.set(COMMAND, "getTasks");
		data.setLong("from", from);
		data.setLong("to", to);
		data.set("skip", skip);
		data.set("limit", limit);
		return this.send(data, Hermes2Client.PUSH);
	}

	@Override
	public void close() throws IOException {
		this.httpClient.close();
	}
}
