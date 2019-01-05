package netty.example.test01;

import io.netty.handler.codec.string.StringEncoder;
import netty.example.utils.ClientConfig;
import netty.example.utils.NettyClient;

/**
 * @author sundingding
 * 客户端,包括重试机制
 */
public class Client {
	/**
	 * 重试次数
	 */
	private static int MAX_RETRY = 5;

	public static void main(String[] args) throws InterruptedException {

		ClientConfig config = new ClientConfig("127.0.0.1", 8800, 5);
		config.addChannelHandler(new StringEncoder());

		NettyClient client = new NettyClient(config);

		client.start();
	}




}
