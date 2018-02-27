import java.io.IOException;

import com.gaia.ams.client.AMSClient;
import com.gaia.ams.client.vo.AssetVO;
import com.gaia.ams.message.impl.ChangeAssetMessage;
import com.nhb.common.vo.HostAndPort;
import com.nhb.common.vo.UserNameAndPassword;
import com.nhb.messaging.rabbit.RabbitMQQueueConfig;
import com.nhb.messaging.rabbit.connection.RabbitMQConnectionPool;
import com.nhb.messaging.rabbit.producer.RabbitMQRPCProducer;

public class ChangeAsset {
	public static void main(String[] args) throws IOException {
		byte[] appId = null;
		String queue = "";

		RabbitMQConnectionPool connectionPool = new RabbitMQConnectionPool();
		connectionPool.addEndpoints(HostAndPort.fromString("dev1:5672"));
		UserNameAndPassword credential = new UserNameAndPassword();
		credential.setUserName("root");
		credential.setPassword("Alert.show(1)");
		connectionPool.setCredential(credential);
		RabbitMQQueueConfig queueConfig = new RabbitMQQueueConfig();
		queueConfig.setQueueName(queue);
		connectionPool.init();

		RabbitMQRPCProducer producer = new RabbitMQRPCProducer(connectionPool, queueConfig);
		AMSClient amsClient = new AMSClient(appId, producer);
		ChangeAssetMessage amsReq = new ChangeAssetMessage();
		amsReq.getChangedAssets().put(new AssetVO(), 1l);
		amsClient.send(amsReq);
		amsClient.close();
	}
}
