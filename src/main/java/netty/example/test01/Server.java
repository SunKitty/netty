package netty.example.test01;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

/**
 * @author sundingding
 */
public class Server {

	public static void main(String[] args) {
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		//接受新连接线程，主要负责创建新连接
		NioEventLoopGroup boss = new NioEventLoopGroup();
		//负责读取数据的线程，主要用于读取数据以及业务逻辑处理
		NioEventLoopGroup worker = new NioEventLoopGroup();
		serverBootstrap
				.group(boss, worker)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<NioSocketChannel>() {
					@Override
					protected void initChannel(NioSocketChannel ch) {
						ch.pipeline().addLast(new StringDecoder());
						ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
							@Override
							protected void channelRead0(ChannelHandlerContext ctx, String msg) {
								System.out.println(msg);
							}
						});
					}
				})
				.bind(8000);
	}
}
