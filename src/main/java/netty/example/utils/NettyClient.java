package netty.example.utils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.concurrent.TimeUnit;

/**
 * @author sunding
 */
public class NettyClient {

	/**
	 * 最大重试次数
	 */
	private int maxRetries;

	private EventLoopGroup workerGroup;

	private Bootstrap bootstrap;

	private ClientConfig config;

	public NettyClient(ClientConfig config) {
		this.config = config;
		this.maxRetries = config.getMaxRetries();
		this.workerGroup = config.getWorkerGroup();
	}

	/**
	 * 启动客户端
	 */
	public void start() {

		this.bootstrap = new Bootstrap();

		if (workerGroup == null) {
			this.workerGroup = new NioEventLoopGroup();
		}

		bootstrap.group(workerGroup)
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<Channel>() {
					@Override
					protected void initChannel(Channel channel) throws Exception {
						ChannelPipeline pipeline = channel.pipeline();
						if (config.getChannelHandlers() != null) {
							config.getChannelHandlers().stream().forEach(handler -> {
								pipeline.addLast(handler);
							});
						}
					}
				});

		connect(bootstrap, config.getHost(), config.getPort(), config.getMaxRetries());
	}

	/**
	 *
	 * 客户端建立连接
	 *
	 * @param bootstrap 启动类
	 * @param host IP
	 * @param port 端口号
	 * @param retries 第n次连接
	 */
	private void connect(Bootstrap bootstrap, String host, int port, int retries) {
		bootstrap.connect(host, port).addListener(future -> {
			int order = maxRetries - retries + 1;
			if (future.isSuccess()) {
				System.out.println(host + ":" + port + ",连接成功。");
			} else {
				System.out.println(host + ":" + port + ",第" + order + "连接失败。");
				if (retries == 0) {
					System.out.println(host + ":" + port + ",最多重试" + maxRetries + "次。");
					return;
				}
				//设置重试间隔时间，随着请求次数递增
				int delay = 1 << order;
				bootstrap.config().group().schedule(() -> connect(bootstrap, host, port, retries - 1), delay, TimeUnit.SECONDS);
			}
		});
	}
}
