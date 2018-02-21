package com.xlot.admin.processor.push;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;
import com.xlot.admin.clientwrapper.Hermes2Client;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.processor.HasHermesClient;

import lombok.Setter;

public class ResetServiceProcessor extends AbstractAdminProcessor implements HasHermesClient {
	@Setter
	private Hermes2Client hermesClient;

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		String id = params.getString("id");
		return hermesClient.resetService(id);
	}

}
