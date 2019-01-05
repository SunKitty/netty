package netty.example.test02;

import netty.example.test02.handler.FirstClientHandler;
import netty.example.utils.ClientConfig;
import netty.example.utils.NettyClient;

/**
 * @author sundingding
 * 客户端,包括重试机制
 */
public class Client {

	public static void main(String[] args) throws InterruptedException {
		ClientConfig config = new ClientConfig("127.0.0.1", 8800, 5);
		config.addChannelHandler(new FirstClientHandler());

		NettyClient client = new NettyClient(config);

		client.start();

	}

}
