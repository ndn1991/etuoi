package com.xlot.sms.sender;

import java.util.Base64;

import com.nhb.common.Loggable;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.utils.UUIDUtils;

public interface Common extends Loggable {
	default byte[] getRaw(PuObjectRO puo, String field) {
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
					try {
						return Base64.getDecoder().decode(s);
					} catch (Exception ex) {
						getLogger().debug("error when get byte for field {} from {}", field, puo, ex);
					}
				}
			}
		}
		return null;
	}

	default PuObject message(int status, String message) {
		SendSMSResult smsResult = new SendSMSResult();
		smsResult.setStatus(status);
		smsResult.setMessage(message);
		return smsResult.toPuObject();
	}
}
