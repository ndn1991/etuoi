package com.xlot.admin.processor.push;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.xlot.admin.clientwrapper.Hermes2Client;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.processor.HasHermesClient;

import lombok.Setter;

public class AddAuthenticatorProcessor extends AbstractAdminProcessor implements HasHermesClient {
	@Setter
	private Hermes2Client hermesClient;

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO _params) {
		PuObject params = new PuObject();
		params.addAll(_params);
		return hermesClient.addAuthenticator(params);
	}

}
