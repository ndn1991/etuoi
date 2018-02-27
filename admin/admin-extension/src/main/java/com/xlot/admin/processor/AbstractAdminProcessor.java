package com.xlot.admin.processor;

import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.gaia.acs.api.GaiaAcsClient;
import com.gaia.acs.api.cache.BundleImmutableConfigCache;
import com.gaia.ams.client.AMSClient;
import com.gaia.ams.client.AMSRPCFuture;
import com.gaia.ams.client.vo.AssetVO;
import com.gaia.ams.message.AMSMessageResponse;
import com.gaia.ams.message.impl.FetchAssetMessage;
import com.gaia.ams.message.impl.FetchMultiAssetMessage;
import com.gaia.ams.message.impl.resp.FetchAssetMessageResponse;
import com.gaia.ams.message.impl.resp.FetchMultiAssetMessageResponse;
import com.gaia.cashin.api.CashInClient;
import com.nhb.common.BaseLoggable;
import com.nhb.common.async.BaseRPCFuture;
import com.nhb.common.async.RPCFuture;
import com.nhb.common.async.translator.RPCFutureTranslator;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.common.db.models.ModelFactory;
import com.nhb.common.utils.Converter;
import com.nhb.common.vo.ByteArrayWrapper;
import com.puppet.figures.client.FiguresClient;
import com.xlot.admin.ProducerManager;
import com.xlot.admin.UserCache;
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
	private AMSClient amsClient;
	private FiguresClient figuresClient;
	private BundleImmutableConfigCache acsConfigCache;
	private GaiaAcsClient acsClient;
	private CashInClient cashInClient;

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
					return Converter.uuidToBytes(s);
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
				return Converter.uuidToBytes(s);
			} catch (Exception e) {
				return Base64.getDecoder().decode(s);
			}
		}
		if (type == PuDataType.NULL) {
			return null;
		}
		throw new RuntimeException("can not get byte array for type: " + type);
	}

	protected RPCFuture<Map<ByteArrayWrapper, Long>> getAssets(Collection<byte[]> userIds, int assetType) {
		FetchMultiAssetMessage amsReq = new FetchMultiAssetMessage();
		for (byte[] userId : userIds) {
			amsReq.getAssets().add(new AssetVO(userId, assetType));
		}
		getLogger().debug("amsReq: {}", amsReq);
		AMSRPCFuture future = amsClient.send(amsReq);
		return new RPCFutureTranslator<AMSMessageResponse, Map<ByteArrayWrapper, Long>>(future) {
			@Override
			protected Map<ByteArrayWrapper, Long> translate(AMSMessageResponse sourceResult) throws Exception {
				if (sourceResult == null) {
					return null;
				}
				FetchMultiAssetMessageResponse rsp = (FetchMultiAssetMessageResponse) sourceResult;
				Map<ByteArrayWrapper, Long> rs = new HashMap<>();
				for (AssetVO asset : rsp.getAssets()) {
					rs.put(new ByteArrayWrapper(asset.getAccountId()), asset.getBalance());
				}
				return rs;
			}
		};
	}

	protected RPCFuture<Map<Integer, Long>> getAssets(byte[] userId) {
		FetchAssetMessage amsReq = new FetchAssetMessage();
		amsReq.setAccountId(userId);
		AMSRPCFuture future = amsClient.send(amsReq);
		return new RPCFutureTranslator<AMSMessageResponse, Map<Integer, Long>>(future) {

			@Override
			protected Map<Integer, Long> translate(AMSMessageResponse sourceResult) throws Exception {
				if (sourceResult == null) {
					return null;
				}
				FetchAssetMessageResponse rsp = (FetchAssetMessageResponse) sourceResult;
				Map<Integer, Long> rs = new HashMap<>();
				for (AssetVO asset : rsp.getAssets()) {
					rs.put(asset.getAssetType(), asset.getBalance());
				}
				return rs;
			}
		};
	}
}
