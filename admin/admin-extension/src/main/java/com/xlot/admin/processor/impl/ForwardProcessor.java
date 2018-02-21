package com.xlot.admin.processor.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.gaia.acs.api.GaiaAcsRPCFuture;
import com.gaia.acs.api.message.ACSMessage;
import com.gaia.acs.api.message.request.bundle.FetchAllBundleOfCpsRequest;
import com.gaia.acs.api.message.request.bundle.GetImmutableConfigRequest;
import com.gaia.acs.api.message.request.cp.FetchAllCpOfProductsRequest;
import com.gaia.acs.api.message.response.bundle.FetchAllBundleOfCpsResponse;
import com.gaia.acs.api.message.response.bundle.GetImmutableConfigResponse;
import com.gaia.acs.api.message.response.cp.FetchAllCpOfProductsResponse;
import com.gaia.acs.api.vo.Bundle;
import com.gaia.acs.api.vo.ContentProvider;
import com.google.common.base.Strings;
import com.nhb.common.async.BaseRPCFuture;
import com.nhb.common.async.Callback;
import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.xlot.admin.bean.PermissionBean;
import com.xlot.admin.bean.UserBean;
import com.xlot.admin.model.RbacModel;
import com.xlot.admin.model.UserModel;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.statics.Permissions;
import com.xlot.admin.statics.Status;
import com.xlot.admin.statics.UserType;

import lombok.Data;

public class ForwardProcessor extends AbstractAdminProcessor {
	private final Map<String, ForwardConfig> forwardConfigMap = new HashMap<>();
	private RbacModel rbacModel;
	private UserModel userModel;

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO _params) {
		String command = _params.getString("command");
		ForwardConfig forwardConfig = forwardConfigMap.get(command);
		if (forwardConfig == null) {
			return futureResponse(baseResponse(Status.COMMAND_NOT_FOUND));
		}
		if (forwardConfig.isRequireLoggedIn() && !_params.variableExists(ADMIN_ID)) {
			return futureResponse(baseResponse(Status.USER_NOT_LOGGED_IN));
		}
		byte[] adminId = _params.getRaw(ADMIN_ID);
		Status verifyPermissionStatus = verifyPermission(adminId, forwardConfig);
		if (verifyPermissionStatus != Status.SUCCESS) {
			return futureResponse(baseResponse(verifyPermissionStatus));
		}

		PuObject params = new PuObject();
		params.addAll(_params);
		if (!Strings.isNullOrEmpty(forwardConfig.getForwardCommand())) {
			params.setString("command", forwardConfig.getForwardCommand());
		}
		if (forwardConfig.isCommandShortcut()) {
			params.setString("c", params.getString("command", null));
		}

		UserBean admin = userModel.loadById(adminId);
		return checkProductCpBundleAndForward(forwardConfig, params, admin);
	}

	private RPCFuture<PuElement> checkProductCpBundleAndForward(ForwardConfig forwardConfig, PuObject params,
			UserBean admin) {
		BaseRPCFuture<PuElement> rs = new BaseRPCFuture<>();
		if (admin.getType() == UserType.NORMAL) {
			byte[] productId = getRaw(params, "productId");
			byte[] cpId = getRaw(params, "cpId");
			String bundle = params.getString("bundle", null);
			if (!Strings.isNullOrEmpty(bundle)) {
				params.setPuArray("bundles", PuArrayList.fromObject(Arrays.asList(bundle)));
				getPrefixAndForward(forwardConfig, params, rs, bundle);
			} else if (cpId != null) {
				FetchAllBundleOfCpsRequest fetchBundleReq = new FetchAllBundleOfCpsRequest();
				fetchBundleReq.getCpIds().add(cpId);
				getBundlesAndForward(forwardConfig, params, rs, fetchBundleReq);
			} else if (productId != null) {
				fetchCpAndForward(forwardConfig, params, productId, rs);
			} else {
				getLogger().debug("forward params: {}", params);
				return getProducerManager().getRPCProducer(forwardConfig.getQueue()).publish(params);
			}
		} else {
			if (admin.getType() == UserType.PRODUCT) {
				params.setRaw("productId", admin.getRefId());
				byte[] cpId = getRaw(params, "cpId");
				String bundle = params.getString("bundle", null);
				if (!Strings.isNullOrEmpty(bundle)) {
					params.setPuArray("bundles", PuArrayList.fromObject(Arrays.asList(bundle)));
					getPrefixAndForward(forwardConfig, params, rs, bundle);
				} else if (cpId != null) {
					FetchAllBundleOfCpsRequest fetchBundleReq = new FetchAllBundleOfCpsRequest();
					fetchBundleReq.getCpIds().add(cpId);
					getBundlesAndForward(forwardConfig, params, rs, fetchBundleReq);
				} else {
					fetchCpAndForward(forwardConfig, params, admin.getRefId(), rs);
				}
			} else {
				params.setRaw("cpId", admin.getRefId());
				params.setRaw("contentProviderId", admin.getRefId());
				String bundle = params.getString("bundle", null);
				if (!Strings.isNullOrEmpty(bundle)) {
					params.setPuArray("bundles", PuArrayList.fromObject(Arrays.asList(bundle)));
					getPrefixAndForward(forwardConfig, params, rs, bundle);
				} else {
					FetchAllBundleOfCpsRequest fetchBundleReq = new FetchAllBundleOfCpsRequest();
					fetchBundleReq.getCpIds().add(admin.getRefId());
					getBundlesAndForward(forwardConfig, params, rs, fetchBundleReq);
				}
			}
		}
		return rs;
	}

	private void fetchCpAndForward(ForwardConfig forwardConfig, PuObject params, byte[] productId,
			BaseRPCFuture<PuElement> rs) {
		FetchAllCpOfProductsRequest fetchCpReq = new FetchAllCpOfProductsRequest();
		fetchCpReq.getProductIds().add(productId);
		GaiaAcsRPCFuture fetchCpFuture = getAcsClient().send(fetchCpReq);
		fetchCpFuture.setTimeout(30, TimeUnit.SECONDS);
		fetchCpFuture.setCallback(new Callback<ACSMessage>() {
			@Override
			public void apply(ACSMessage result) {
				if (result == null) {
					getLogger().error("error", result);
					rs.setAndDone(baseResponse(Status.UNKNOWN_ERROR));
				} else {
					FetchAllCpOfProductsResponse response = (FetchAllCpOfProductsResponse) result;
					FetchAllBundleOfCpsRequest fetchBundleReq = new FetchAllBundleOfCpsRequest();
					for (ContentProvider cp : response.getCps()) {
						fetchBundleReq.getCpIds().add(cp.getId());
					}
					getBundlesAndForward(forwardConfig, params, rs, fetchBundleReq);
				}
			}
		});
	}

	private void getPrefixAndForward(ForwardConfig forwardConfig, PuObject params, BaseRPCFuture<PuElement> rs,
			String bundle) {
		GetImmutableConfigRequest getPrefixReq = new GetImmutableConfigRequest();
		getPrefixReq.setName(bundle);
		getPrefixReq.setKey("prefix");
		GaiaAcsRPCFuture getPrefixFuture = getAcsClient().send(getPrefixReq);
		getPrefixFuture.setTimeout(30, TimeUnit.SECONDS);
		getPrefixFuture.setCallback(new Callback<ACSMessage>() {
			@Override
			public void apply(ACSMessage result) {
				if (result == null) {
					getLogger().error("error", result);
					rs.setAndDone(baseResponse(Status.UNKNOWN_ERROR));
				} else {
					GetImmutableConfigResponse response = (GetImmutableConfigResponse) result;
					params.setString("prefix", response.getValue());
					getLogger().debug("forward params: {}", params);
					RPCFuture<PuElement> forwardFuture = getProducerManager().getRPCProducer(forwardConfig.getQueue())
							.publish(params);
					forwardFuture.setTimeout(30, TimeUnit.SECONDS);
					forwardFuture.setCallback(new Callback<PuElement>() {
						@Override
						public void apply(PuElement result) {
							rs.setAndDone(result);
						}
					});
				}
			}
		});
	}

	private void getBundlesAndForward(ForwardConfig forwardConfig, PuObject params, BaseRPCFuture<PuElement> rs,
			FetchAllBundleOfCpsRequest fetchBundleReq) {
		GaiaAcsRPCFuture fetchBundleFuture = getAcsClient().send(fetchBundleReq);
		fetchBundleFuture.setTimeout(30, TimeUnit.SECONDS);
		fetchBundleFuture.setCallback(new Callback<ACSMessage>() {
			@Override
			public void apply(ACSMessage result) {
				if (result == null) {
					getLogger().error("error", result);
					rs.setAndDone(baseResponse(Status.UNKNOWN_ERROR));
				} else {
					FetchAllBundleOfCpsResponse response = (FetchAllBundleOfCpsResponse) result;
					PuArrayList bundles = new PuArrayList();
					String bundle = "";
					for (Bundle b : response.getBundles()) {
						bundles.addFrom(b.getName());
						bundle = b.getName();
					}
					params.setPuArray("bundles", bundles);
					getPrefixAndForward(forwardConfig, params, rs, bundle);
				}
			}
		});
	}

	@Override
	public void init(PuObjectRO params) {
		rbacModel = getModelFactory().getModel(RbacModel.class.getName());
		userModel = getModelFactory().getModel(UserModel.class.getName());

		PuObject configs = params.getPuObject("config");
		for (Entry<String, PuValue> e : configs) {
			PuObject config = e.getValue().getPuObject();
			ForwardConfig forwardConfig = new ForwardConfig();
			forwardConfig.setForwardCommand(config.getString("forwardCommand", e.getKey()));
			forwardConfig.setQueue(config.getString("queue"));
			forwardConfig.setPermission(config.getString("permission", null));
			forwardConfig.setRequireLoggedIn(config.getBoolean("requireLoggedIn", true));
			forwardConfig.setCommandShortcut(config.getBoolean("commandShortcut", false));
			forwardConfigMap.put(e.getKey(), forwardConfig);
		}
	}

	private Status verifyPermission(byte[] adminId, ForwardConfig forwardConfig) {
		if (Strings.isNullOrEmpty(forwardConfig.getPermission())) {
			return Status.SUCCESS;
		}
		List<PermissionBean> permissions = rbacModel.permissionsOfUser(adminId);
		if (permissions != null && !permissions.isEmpty()) {
			for (PermissionBean p : permissions) {
				if (p.getName().equalsIgnoreCase(Permissions.SUPER_USER)) {
					return Status.SUCCESS;
				}
				if (p.getName().equalsIgnoreCase(forwardConfig.getPermission())) {
					return Status.SUCCESS;
				}
			}
		}
		return Status.NOT_HAVE_PERMISSION;
	}

	@Data
	class ForwardConfig {
		private String queue;
		private String forwardCommand;
		private String permission;
		private boolean requireLoggedIn;
		private boolean commandShortcut;
	}
}
