package netty.push.server;

import com.google.common.base.Strings;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import netty.push.common.Listener;
import netty.push.exception.ServerException;
import netty.push.common.ThreadNames;
import netty.push.codec.PacketDecoder;
import netty.push.codec.PacketEncoder;
import java.net.InetSocketAddress;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author sunding
 */
public abstract class AbstractNettyServer {

	/**
	 * 服务器状态
	 * Created-已创建, Initialized-初始化, Starting-启动中, Started-已启动, Shutdown-已关闭
	 */
	public enum State {Created, Initialized, Starting, Started, Shutdown};

	protected final AtomicReference<State> states = new AtomicReference<>(State.Created);

	protected final String host;

	protected final int port;

	protected EventLoopGroup bossGroup;

	protected EventLoopGroup workerGroup;

	public AbstractNettyServer(int port) {
		this.host = null;
		this.port = port;
	}

	public AbstractNettyServer(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * 防止重复启动服务端
	 */
	public void init() {
		if (!states.compareAndSet(State.Created, State.Initialized)) {
			throw new ServerException("Server already init.");
		}
	}

	public void start(final Listener listener) {
		if (!states.compareAndSet(State.Initialized, State.Starting)) {
			throw new ServerException("Server already started or have not init");
		}
		createNioServer(listener);

	}

	private void  createNioServer(Listener listener) {
		EventLoopGroup bossGroup = getBossGroup();
		EventLoopGroup workerGroup = getWorkerGroup();
		if (bossGroup == null) {
			NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(getBossThreadNum(),
					getBossThreadFactory(), getSelectorProvider());
			nioEventLoopGroup.setIoRatio(100);
			bossGroup = nioEventLoopGroup;
		}

		if (workerGroup == null) {
			NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(getWorkThreadNum(),
					getWorkerGroup(), getSelectorProvider());
			workerGroup = nioEventLoopGroup;
		}

		createServer(listener, bossGroup, workerGroup, getChannelFactory());


	}

	private void createServer(Listener listener, EventLoopGroup boss, EventLoopGroup worker,
			ChannelFactory<? extends ServerChannel> channelFactory) {

		/**
		 * NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器，
		 * Netty提供了许多不同的EventLoopGroup的实现用来处理不同传输协议。
		 * 在一个服务端的应用会有2个NioEventLoopGroup会被使用。
		 * 第一个经常被叫做‘boss’，用来接收进来的连接。
		 * 第二个经常被叫做‘worker’，用来处理已经被接收的连接，
		 * 一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上。
		 * 如何知道多少个线程已经被使用，如何映射到已经创建的Channels上都需要依赖于EventLoopGroup的实现，
		 * 并且可以通过构造函数来配置他们的关系。
		 */
		this.bossGroup = boss;
		this.workerGroup = worker;

		try {
			/**
			 * ServerBootstrap 是一个启动NIO服务的辅助启动类
			 * 你可以在这个服务中直接使用Channel
			 */
			ServerBootstrap bootstrap = new ServerBootstrap();

			/**
			 * 这一步是必须的，如果没有设置group将会报java.lang.IllegalStateException: group not set异常
			 */
			bootstrap.group(bossGroup, workerGroup);

			/**
			 * ServerSocketChannel以NIO的selector为基础进行实现的，用来接收新的连接
			 * 这里告诉Channel如何获取新的连接.
			 */
			bootstrap.channelFactory(channelFactory);

			/**
			 * 这里的事件处理类经常会被用来处理一个最近的已经接收的Channel。
			 * ChannelInitializer是一个特殊的处理类，
			 * 他的目的是帮助使用者配置一个新的Channel。
			 * 也许你想通过增加一些处理类比如NettyServerHandler来配置一个新的Channel
			 * 或者其对应的ChannelPipeline来实现你的网络程序。
			 * 当你的程序变的复杂时，可能你会增加更多的处理类到pipeline上，
			 * 然后提取这些匿名类到最顶层的类上。
			 */
			bootstrap.childHandler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel channel) throws Exception {
					//每连上一个链接调用一次
					initPipeline(channel.pipeline());
				}
			});

			initOptions(bootstrap);

			/***
			 * 绑定端口并启动去接收进来的连接
			 */

			InetSocketAddress address = Strings.isNullOrEmpty(host) ? new InetSocketAddress(port)
					: new InetSocketAddress(host, port);

			bootstrap.bind(address).addListener(future -> {
				if (future.isSuccess()) {
					states.set(State.Started);
					System.out.println("server start success on :  " + port);
					if (listener != null) {
						listener.onSuccess(port);
					}
				} else {
					System.out.println("server start failure on : " + port + future.cause());
					if (listener != null) {
						listener.onFailure(future.cause());
					}
				}
			});

		} catch (Exception e) {
			System.out.println("server start exception, " +  e);
			if (listener != null) {
				listener.onFailure(e);
			}
			throw new ServerException("server start exception, port=" + port, e);
		}

	}

	/**
	 * option()是提供给NioServerSocketChannel用来接收进来的连接。
	 * childOption()是提供给由父管道ServerChannel接收到的连接，
	 * 在这个例子中也是NioServerSocketChannel。
	 */
	protected void initOptions(ServerBootstrap bootstrap) {

		/**
		 * 在Netty 4中实现了一个新的ByteBuf内存池，它是一个纯Java版本的 jemalloc （Facebook也在用）。
		 * 现在，Netty不会再因为用零填充缓冲区而浪费内存带宽了。不过，由于它不依赖于GC，开发人员需要小心内存泄漏。
		 * 如果忘记在处理程序中释放缓冲区，那么内存使用率会无限地增长。
		 * Netty默认不使用内存池，需要在创建客户端或者服务端的时候进行指定
		 */
		bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
	}

	/**
	 * 每连上一个链接调用一次
	 * @param pipeline
	 */
	protected void initPipeline(ChannelPipeline pipeline) {
		pipeline.addLast("decoder", getDecoder());
		pipeline.addLast("encoder", getEncoder());
		pipeline.addLast("handler", getChannelHandler());
	}

	protected ChannelHandler getEncoder() {
		//每连上一个链接调用一次, 所有用单利
		return PacketEncoder.INSTANCE;
	}

	public ChannelHandler getChannelHandler() {
		return null;
	}

	protected ChannelHandler getDecoder() {
		return new PacketDecoder();
	}


	private int getBossThreadNum() {
		return 1;
	}

	private int getWorkThreadNum() {
		return 0;
	}

	/**
	 * netty 默认的Executor为ThreadPerTaskExecutor
	 * 线程池的使用在SingleThreadEventExecutor#doStartThread
	 * eventLoop.execute(runnable);
	 * 是比较重要的一个方法。在没有启动真正线程时，
	 * 它会启动线程并将待执行任务放入执行队列里面。
	 * 启动真正线程(startThread())会判断是否该线程已经启动，
	 * 如果已经启动则会直接跳过，达到线程复用的目的
	 *
	 * @return
	 */
	protected ThreadFactory getBossThreadFactory() {
		return new DefaultThreadFactory(getBossThreadName());
	}

	protected ThreadFactory getWorkThreadFactory() {
		return new DefaultThreadFactory(getWorkThreadName());
	}

	/**
	 * 判断服务器是否已经启动
	 * @return
	 */
	public boolean isRunning() {
		return states.get() == State.Started;
	}

	public EventLoopGroup getBossGroup() {
		return bossGroup;
	}

	public EventLoopGroup getWorkerGroup() {
		return workerGroup;
	}

	protected String getBossThreadName() {
		return ThreadNames.BOSS;
	}

	protected String getWorkThreadName() {
		return ThreadNames.WORKER;
	}

	public SelectorProvider getSelectorProvider() {
		return SelectorProvider.provider();
	}

	public ChannelFactory<? extends ServerChannel> getChannelFactory() {
		return NioServerSocketChannel::new;
	}

}
