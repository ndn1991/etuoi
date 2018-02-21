package com.xlot.admin;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.mario.gateway.rabbitmq.RabbitMQServerWrapper;
import com.nhb.common.BaseLoggable;
import com.nhb.messaging.rabbit.RabbitMQQueueConfig;
import com.nhb.messaging.rabbit.connection.RabbitMQConnection;
import com.nhb.messaging.rabbit.producer.RabbitMQRPCProducer;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProducerManager extends BaseLoggable implements AutoCloseable {
	private final RabbitMQServerWrapper rabbitServer;
	private final Map<String, RabbitMQRPCProducer> rpcMap = new ConcurrentHashMap<>();

	private final Object rpcLock = new Object();

	public RabbitMQRPCProducer getRPCProducer(String queueName) {
		if (!rpcMap.containsKey(queueName)) {
			synchronized (rpcLock) {
				if (!rpcMap.containsKey(queueName)) {
					RabbitMQQueueConfig queueConfig = new RabbitMQQueueConfig();
					queueConfig.setQueueName(queueName);
					rpcMap.put(queueName, new RabbitMQRPCProducer(rabbitServer.getConnection(), queueConfig));
				}
			}
		}
		RabbitMQRPCProducer producer = rpcMap.get(queueName);
		if (!producer.isConnected()) {
			producer.start();
		}
		return producer;
	}

	public RabbitMQConnection getRabbitMQConnection() {
		return rabbitServer.getConnection();
	}

	@Override
	public void close() throws Exception {
		for (Entry<String, RabbitMQRPCProducer> e : rpcMap.entrySet()) {
			System.out.println("closing rpc rabbit producer " + e.getKey());
			getLogger().info("closing rabbit rpc producer {}", e.getKey());
			if (e.getValue() != null) {
				e.getValue().close();
			}
		}
	}
}
