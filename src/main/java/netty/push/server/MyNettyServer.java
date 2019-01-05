package netty.push.server;

import io.netty.channel.ChannelHandler;
import netty.example.test02.handler.FirstServerHandler;

/**
 * Created by sundingding on 2019/1/2.
 */
public class MyNettyServer extends AbstractNettyServer {

	public MyNettyServer(int port) {
		super(port);
	}

	public MyNettyServer(String host, int port) {
		super(host, port);
	}

	@Override
	public ChannelHandler getChannelHandler() {
		return new FirstServerHandler();
	}

	public static void main(String[] args) {
		MyNettyServer server = new MyNettyServer(8800);

		server.init();
		server.start(null);
	}
}
