package com.xlot.admin.processor.user;

import com.gaia.ams.client.AMSRPCFuture;
import com.gaia.ams.client.vo.AssetVO;
import com.gaia.ams.message.AMSMessageResponse;
import com.gaia.ams.message.impl.ChangeAssetMessage;
import com.gaia.ams.message.impl.resp.ChangeAssetMessageResponse;
import com.gaia.ams.utils.Status;
import com.gaia.cashin.api.CashInRPCFuture;
import com.gaia.cashin.api.message.CashInBaseMessageResponse;
import com.gaia.cashin.api.message.impl.LogGoldTransferCashInMessage;
import com.gaia.cashin.api.statics.TransferType;
import com.nhb.common.async.Callback;
import com.nhb.common.async.RPCFuture;
import com.nhb.common.async.translator.RPCFutureTranslator;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.utils.Converter;
import com.puppet.figures.ActivityType;
import com.puppet.figures.msg.impl.UserActivityLogMessage;
import com.xlot.admin.processor.AbstractAdminProcessor;

public class ChangeAssetProcessor extends AbstractAdminProcessor {
	private int moneyAssetType = 1;

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		byte[] userId = getRaw(params, "userId");
		int assetType = params.getInteger("assetType");
		long value = params.getLong("value");
		String desc = params.getString("description", "");

		ChangeAssetMessage amsReq = new ChangeAssetMessage();
		amsReq.setDescription("change by admin " + params.getString(ADMIN_NAME, "") + " - " + desc);
		amsReq.getChangedAssets().put(new AssetVO(userId, assetType), value);
		AMSRPCFuture future = getAmsClient().send(amsReq);

		return new RPCFutureTranslator<AMSMessageResponse, PuElement>(future) {
			@Override
			protected PuElement translate(AMSMessageResponse sourceResult) throws Exception {
				if (sourceResult != null) {
					ChangeAssetMessageResponse rsp = (ChangeAssetMessageResponse) sourceResult;
					if (rsp.getStatus() == Status.OK) {
						PuObject rs = baseResponse(com.xlot.admin.statics.Status.SUCCESS);
						rs.set("data", rsp.getChangedAssets().get(0).getBalance());
						logCashIn(userId, value, assetType, desc);
						log(userId, value, rsp.getChangedAssets().get(0).getBalance(), desc);
						return rs;
					} else {
						getLogger().warn("error from ams: {}", rsp.getStatus());
						return baseResponse(com.xlot.admin.statics.Status.UNKNOWN_ERROR);
					}
				} else {
					return null;
				}
			}
		};
	}

	protected void logCashIn(byte[] userId, long value, int assetType, String desc) {
		if (assetType != moneyAssetType) {
			return;
		}
		if (desc != null && desc.trim().startsWith("-1")) {
			return;
		}
		LogGoldTransferCashInMessage cashInReq = new LogGoldTransferCashInMessage();
		cashInReq.setFromNickname(desc);
		cashInReq.setFromUsername(desc);
		cashInReq.setGold(value);
		cashInReq.setPrice(value);
		cashInReq.setUserId(userId);
		cashInReq.setTransferType(TransferType.ADMIN_TO_USER);
		CashInRPCFuture future = getCashInClient().send(cashInReq);
		future.setCallback(new Callback<CashInBaseMessageResponse>() {
			@Override
			public void apply(CashInBaseMessageResponse result) {
				if (result == null) {
					getLogger().error("error from cash in", future.getFailedCause());
				} else {
					getLogger().debug("result from cash in {}", result);
				}
			}
		});		
	}

	private void log(byte[] userId, long value, long money, String desc) {
		PuObject content = new PuObject();
		content.setLong("value", value);
		content.setString("description", desc);
		UserActivityLogMessage userLog = new UserActivityLogMessage();
		userLog.setActivityType(ActivityType.CHANGE_ASSET_BY_ADMIN);
		userLog.setContent(content.toJSON());
		userLog.setUserId(Converter.bytesToUUID(userId));
		userLog.setMoney(money);
		userLog.setDescription(desc);

		getFiguresClient().send(userLog);
	}

}
