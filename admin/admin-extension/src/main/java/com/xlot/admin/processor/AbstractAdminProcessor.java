package com.xlot.admin.processor;

import java.util.Base64;

import com.nhb.common.BaseLoggable;
import com.nhb.common.async.BaseRPCFuture;
import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.common.db.models.ModelFactory;
import com.nhb.common.utils.UUIDUtils;
import com.xlot.admin.ProducerManager;
import com.xlot.admin.UserCache;
import com.xlot.admin.sms.SmsSender;
import com.xlot.admin.statics.Status;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter(value = AccessLevel.PACKAGE)
@Getter(value = AccessLevel.PROTECTED)
public abstract class AbstractAdminProcessor extends BaseLoggable implements AdminProcessor {
	private ModelFactory modelFactory;
	private ProducerManager producerManager;
	private UserCache userCache;
	private SmsSender smsSender;

	@Getter()
	@Setter()
	private boolean requireLoggedIn = true;

	@Getter
	@Setter
	private String permission;

	@Override
	public void init(PuObjectRO params) {

	}

	@Override
	public RPCFuture<PuElement> process(PuObjectRO params) {
		return _process(params);
	}

	protected abstract RPCFuture<PuElement> _process(PuObjectRO params);

	protected PuObject baseResponse(Status status) {
		PuObject puo = new PuObject();
		puo.setInteger("status", status.getId());
		puo.setString("message", status.getMessage());
		return puo;
	}

	protected RPCFuture<PuElement> futureResponse(PuElement response) {
		BaseRPCFuture<PuElement> rs = new BaseRPCFuture<>();
		rs.setAndDone(response);
		return rs;
	}

	protected byte[] getRaw(PuObjectRO puo, String field) {
		if (puo.variableExists(field)) {
			PuDataType type = puo.typeOf(field);
			if (type == PuDataType.RAW) {
				return puo.getRaw(field);
			}
			if (type == PuDataType.STRING) {
				String s = puo.getString(field);
				try {
					return UUIDUtils.uuidToBytes(s);
				} catch (Exception e) {
					return Base64.getDecoder().decode(s);
				}
			}
			if (type == PuDataType.NULL) {
				return null;
			}
			throw new RuntimeException("can not get byte array for type: " + type);
		}
		return null;
	}

	protected byte[] getRaw(PuValue value) {
		PuDataType type = value.getType();
		if (type == PuDataType.RAW) {
			return value.getRaw();
		}
		if (type == PuDataType.STRING) {
			String s = value.getString();
			try {
				return UUIDUtils.uuidToBytes(s);
			} catch (Exception e) {
				return Base64.getDecoder().decode(s);
			}
		}
		if (type == PuDataType.NULL) {
			return null;
		}
		throw new RuntimeException("can not get byte array for type: " + type);
	}
}
