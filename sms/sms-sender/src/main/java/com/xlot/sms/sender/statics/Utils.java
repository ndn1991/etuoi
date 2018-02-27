package com.xlot.sms.sender.statics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.bson.Document;

import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuValue;
import com.xlot.sms.sender.bean.SmsLogBean;

public interface Utils {
	static Document toDocument(SmsLogBean bean) {
		Document doc = new Document() //
				.append(F.ID, bean.getId()) //
				.append(F.LOG_ID, bean.getLogId()) //
				.append(F.TIMEOUT, bean.isTimeout()) //
				.append(F.STATUS, bean.getStatus()) //
				.append(F.SEND_PHONES, bean.getSendPhones()) //
				.append(F.INVALID_PHONES, bean.getInvalidPhones()) //
				.append(F.RESULT, bean.getResult() == null ? null : toDocument(bean.getResult().toPuObject()));
		return doc;
	}

	static Document toDocument(PuObject puo) {
		Document doc = new Document();
		for (Entry<String, PuValue> e : puo) {
			if (e.getValue().getType() != PuDataType.NULL) {
				if (e.getValue().getType() == PuDataType.PUOBJECT) {
					Document subDoc = toDocument(e.getValue().getPuObject());
					doc.append(e.getKey(), subDoc);
				} else if (e.getValue().getType() == PuDataType.PUARRAY) {
					doc.append(e.getKey(), toDocuments(e.getValue().getPuArray()));
				} else {
					doc.append(e.getKey(), e.getValue().getData());
				}
			}
		}
		return doc;
	}

	static Collection<Object> toDocuments(PuArray pua) {
		if (pua == null) {
			return Collections.emptyList();
		}
		List<Object> rs = new ArrayList<>(pua.size());
		for (PuValue e : pua) {
			if (e.getType() != PuDataType.NULL) {
				if (e.getType() == PuDataType.PUOBJECT) {
					Document subDoc = toDocument(e.getPuObject());
					rs.add(subDoc);
				} else if (e.getType() == PuDataType.PUARRAY) {
					rs.add(toDocuments(e.getPuArray()));
				} else {
					rs.add(e.getData());
				}
			}
		}
		return rs;
	}
}
