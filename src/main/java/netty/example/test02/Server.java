package netty.example.test02;

import netty.example.utils.NettyServer;

/**
 * @author sundingding
 */
public class Server {

	public static void main(String[] args) {
//		ServerBootstrap serverBootstrap = new ServerBootstrap();
//		//接受新连接线程，主要负责创建新连接
//		NioEventLoopGroup boss = new NioEventLoopGroup();
//		//负责读取数据的线程，主要用于读取数据以及业务逻辑处理
//		NioEventLoopGroup worker = new NioEventLoopGroup();
//		serverBootstrap
//				.group(boss, worker)
//				.channel(NioServerSocketChannel.class)
//				.childHandler(new ChannelInitializer<NioSocketChannel>() {
//					@Override
//					protected void initChannel(NioSocketChannel ch) {
//						ch.pipeline().addLast(new FirstServerHandler());
//					}
//				})
//				.bind(8800);
		NettyServer server = new NettyServer(8800);
		server.init();
		server.start(null);
	}
}
