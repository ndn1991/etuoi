package com.xlot.admin.processor.user;

import java.util.Map;
import java.util.Map.Entry;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.async.translator.RPCFutureTranslator;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.xlot.admin.processor.AbstractAdminProcessor;

public class GetUserAssetProcessor extends AbstractAdminProcessor {

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		byte[] userId = getRaw(params, "userId");
		RPCFuture<Map<Integer, Long>> future = getAssets(userId);
		return new RPCFutureTranslator<Map<Integer, Long>, PuElement>(future) {
			@Override
			protected PuElement translate(Map<Integer, Long> sourceResult) throws Exception {
				if (sourceResult != null) {
					PuObject rs = new PuObject();
					for (Entry<Integer, Long> e : sourceResult.entrySet()) {
						rs.setLong(String.valueOf(e.getKey()), e.getValue());
					}
					return rs;
				}
				return null;
			}
		};
	}

}
