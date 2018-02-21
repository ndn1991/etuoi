package com.xlot.admin.processor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gaia.acs.api.GaiaAcsClient;
import com.gaia.acs.api.cache.BundleImmutableConfigCache;
import com.gaia.ams.client.AMSClient;
import com.gaia.cashin.api.CashInClient;
import com.google.common.base.Joiner;
import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuValue;
import com.nhb.common.db.models.ModelFactory;
import com.puppet.figures.client.FiguresClient;
import com.xlot.admin.ProducerManager;
import com.xlot.admin.UserCache;
import com.xlot.admin.clientwrapper.Hermes2Client;

import lombok.Builder;

@Builder
public class AdminProcessorFactory extends BaseLoggable {
	private final ModelFactory modelFactory;
	private final ProducerManager producerManager;
	private final UserCache userCache;
	private final AMSClient amsClient;
	private final FiguresClient figuresClient;
	private final Hermes2Client hermesClient;
	private final BundleImmutableConfigCache acsConfigCache;
	private final  GaiaAcsClient acsClient;
	private final CashInClient cashInClient;

	private final Map<String, AdminProcessor> pMap = new HashMap<>();

	@SuppressWarnings("unchecked")
	public AdminProcessorFactory init(String path) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(path));
		String content = Joiner.on('\n').join(lines);
		getLogger().info("content of commands: \n{}", content);
		PuArray processorConfigs = path.endsWith(".xml") ? PuArrayList.fromXML(content) //
				: PuArrayList.fromJSON(content);
		for (PuValue processorConfig : processorConfigs) {
			PuObject config = processorConfig.getPuObject();
			String command = config.getString("command");
			String className = config.getString("class");
			boolean requireLoggedIn = config.getBoolean("requireLoggedIn", true);
			String permission = config.getString("permission", null);
			permission = permission == null ? null : permission.toLowerCase();
			PuObject params = config.getPuObject("params", new PuObject());

			try {
				Class<? extends AdminProcessor> clazz = (Class<? extends AdminProcessor>) Class.forName(className);
				AdminProcessor p = clazz.newInstance();
				if (p instanceof AbstractAdminProcessor) {
					((AbstractAdminProcessor) p).setRequireLoggedIn(requireLoggedIn);
					((AbstractAdminProcessor) p).setPermission(permission);
					((AbstractAdminProcessor) p).setModelFactory(modelFactory);
					((AbstractAdminProcessor) p).setProducerManager(producerManager);
					((AbstractAdminProcessor) p).setUserCache(userCache);
					((AbstractAdminProcessor) p).setAmsClient(amsClient);
					((AbstractAdminProcessor) p).setFiguresClient(figuresClient);
					((AbstractAdminProcessor) p).setAcsConfigCache(acsConfigCache);
					((AbstractAdminProcessor) p).setAcsClient(acsClient);
					((AbstractAdminProcessor) p).setCashInClient(cashInClient);
				}
				
				if (p instanceof HasHermesClient) {
					((HasHermesClient) p).setHermesClient(hermesClient);
				}
				p.init(params);
				pMap.put(command, p);
			} catch (Exception e) {
				getLogger().error("error create processor: {} - {} - {}", command, className, params, e);
			}
		}

		return this;
	}

	public AdminProcessor getProcessor(String command) {
		return pMap.get(command);
	}
}
