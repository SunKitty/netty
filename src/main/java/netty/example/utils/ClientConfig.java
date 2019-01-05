package netty.example.utils;

import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;

import java.util.LinkedList;

/**
 * @author sunding
 */
public class ClientConfig {

	/**
	 * IP地址
	 */
	private String host;

	/**
	 * 端口号
	 */
	private int port;

	/**
	 * 最多重试次数
	 */
	private int maxRetries;

	/**
	 * 事件处理器线程组
	 */
	private EventLoopGroup workerGroup;

	/**
	 * 处理器
	 */
	private LinkedList<ChannelHandler> channelHandlers;

	public ClientConfig(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public ClientConfig(String host, int port, int maxRetries) {
		this.host = host;
		this.port = port;
		this.maxRetries = maxRetries;
	}

	public ClientConfig(String host, int port, EventLoopGroup workerGroup) {
		this.host = host;
		this.port = port;
		this.workerGroup = workerGroup;
	}

	public ClientConfig(String host, int port, int maxRetries, EventLoopGroup workerGroup) {
		this.host = host;
		this.port = port;
		this.maxRetries = maxRetries;
		this.workerGroup = workerGroup;
	}

	public void addChannelHandler(ChannelHandler handler) {
		if (channelHandlers == null) {
			channelHandlers = Lists.newLinkedList();
		}
		channelHandlers.add(handler);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public EventLoopGroup getWorkerGroup() {
		return workerGroup;
	}

	public void setWorkerGroup(EventLoopGroup workerGroup) {
		this.workerGroup = workerGroup;
	}

	public LinkedList<ChannelHandler> getChannelHandlers() {
		return channelHandlers;
	}

	public void setChannelHandlers(LinkedList<ChannelHandler> channelHandlers) {
		this.channelHandlers = channelHandlers;
	}
}
