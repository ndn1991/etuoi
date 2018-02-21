package com.xlot.admin.processor.user;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.gaia.ams.client.AMSRPCFuture;
import com.gaia.ams.message.AMSMessageResponse;
import com.gaia.ams.message.impl.FetchAssetMessage;
import com.gaia.ams.message.impl.FetchLogAssetMessage;
import com.gaia.ams.message.impl.resp.FetchAssetMessageResponse;
import com.gaia.ams.message.impl.resp.FetchLogAssetMessageResponse;
import com.nhb.common.async.BaseRPCFuture;
import com.nhb.common.async.Callback;
import com.nhb.common.async.RPCFuture;
import com.nhb.common.async.translator.RPCFutureTranslator;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.utils.Converter;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.statics.Status;

public class FetchAssetLogProcessor extends AbstractAdminProcessor {

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		long fromTime = getTime(params, "fromTime", 0);
		long toTime = getTime(params, "toTime", 0);
		byte[] accountId = getRaw(params, "accountId");
		byte[] transactionId = getRaw(params, "transactionId");
		int type = params.getInteger("type", 1);

		if (transactionId != null) {
			return fetchLog(transactionId);
		}
		FetchAssetMessage fetchReq = new FetchAssetMessage();
		fetchReq.setAccountId(accountId);
		fetchReq.getAssetTypes().add(type);
		AMSRPCFuture fetchFuture = getAmsClient().send(fetchReq);
		BaseRPCFuture<PuElement> rs = new BaseRPCFuture<>();
		fetchFuture.setCallback(new Callback<AMSMessageResponse>() {
			@Override
			public void apply(AMSMessageResponse result) {
				if (result == null) {
					getLogger().error("error when fetch asset", fetchFuture.getFailedCause());
					rs.setAndDone(baseResponse(Status.UNKNOWN_ERROR));
				} else {
					FetchAssetMessageResponse fetchRsp = (FetchAssetMessageResponse) result;
					if (fetchRsp.getStatus() == com.gaia.ams.utils.Status.OK) {
						fetchLog(rs, fetchRsp.getAssets().get(0).getId(), fromTime, toTime);
					} else {
						getLogger().debug("fetch asset status: {}", fetchRsp.getStatus());
						rs.setAndDone(baseResponse(Status.ASSET_NOT_FOUND));
					}
				}
			}
		});

		return rs;
	}

	private RPCFuture<PuElement> fetchLog(byte[] transactionId) {
		FetchLogAssetMessage logReq = new FetchLogAssetMessage();
		logReq.setTransactionId(transactionId);
		getLogger().debug("fetching log for tid: {}", Converter.bytesToHex(transactionId));
		AMSRPCFuture logFuture = getAmsClient().send(logReq);
		return new RPCFutureTranslator<AMSMessageResponse, PuElement>(logFuture) {
			@Override
			protected PuElement translate(AMSMessageResponse sourceResult) throws Exception {
				if (sourceResult == null) {
					return null;
				}
				FetchLogAssetMessageResponse logRsp = (FetchLogAssetMessageResponse) sourceResult;
				if (logRsp.getStatus() == com.gaia.ams.utils.Status.OK) {
					PuObject rs = baseResponse(Status.SUCCESS);
					rs.setPuObject("data", logRsp.getData());
					return rs;
				} else {
					getLogger().debug("fetch log status: {}", logRsp.getStatus());
					return baseResponse(Status.AMS_LOG_NOT_FOUND);
				}
			}
		};
	}

	private void fetchLog(BaseRPCFuture<PuElement> rs, byte[] id, long fromTime, long toTime) {
		FetchLogAssetMessage logReq = new FetchLogAssetMessage();
		logReq.setAssetIds(Arrays.asList(id));
		logReq.setFrom(fromTime);
		logReq.setTo(toTime);
		getLogger().debug("fetching log for id: {}", Converter.bytesToHex(id));
		AMSRPCFuture logFuture = getAmsClient().send(logReq);
		logFuture.setTimeout(10, TimeUnit.SECONDS);
		logFuture.setCallback(new Callback<AMSMessageResponse>() {
			@Override
			public void apply(AMSMessageResponse result) {
				if (result == null) {
					getLogger().error("error when fetch log", logFuture.getFailedCause());
					rs.setAndDone(baseResponse(Status.UNKNOWN_ERROR));
				} else {
					FetchLogAssetMessageResponse logRsp = (FetchLogAssetMessageResponse) result;
					if (logRsp.getStatus() == com.gaia.ams.utils.Status.OK) {
						PuObject _rs = baseResponse(Status.SUCCESS);
						_rs.setPuObject("data", logRsp.getData());
						rs.setAndDone(_rs);
					} else {
						getLogger().debug("fetch log status: {}", logRsp.getStatus());
						rs.setAndDone(baseResponse(Status.AMS_LOG_NOT_FOUND));
					}
				}
			}
		});
	}

	private long getTime(PuObjectRO params, String field, long def) {
		if (!params.variableExists(field)) {
			return def;
		}
		if (params.typeOf(field) == PuDataType.LONG) {
			return params.getLong(field);
		} else {
			String s = params.getString(field);
			try {
				return Long.parseLong(s);
			} catch (Exception ex) {
				try {SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date date = formater.parse(s);
					return date.getTime();
				} catch (Exception e) {
					try {
						SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
						Date date = formater.parse(s);
						return date.getTime();
					} catch (Exception e2) {
						try {
							SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH");
							Date date = formater.parse(s);
							return date.getTime();
						} catch (Exception e3) {
							try {
								SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
								Date date = formater.parse(s);
								return date.getTime();
							} catch (Exception e4) {
								throw new RuntimeException("can not get time from param " + field);
							}
						}
					}
				}
			}
		}
	}

}
