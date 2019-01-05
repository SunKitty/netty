package netty.example.test02.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.nio.charset.Charset;

/**
 * @author sunding
 * 	客户端处理器
 */
public class FirstClientHandler extends ChannelInboundHandlerAdapter {

	/**
	 * 发送数据
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(System.currentTimeMillis() + " : 客户端开始写入数据。");

		//1. 获取数据
		ByteBuf buffer = getByteBuf(ctx);

		//2. 写入数据
		ctx.channel().writeAndFlush(buffer);

	}

	/**
	 * 处理服务商响应数据
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buffer = (ByteBuf) msg;
		System.out.println(System.currentTimeMillis() + " : 客户端读取到服务端返回的数据 -> "
				+ buffer.toString(Charset.forName("utf-8")));
	}

	private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
		//1. 获取二进制抽象 ByteBuf
		ByteBuf buffer = ctx.alloc().buffer();

		//2. 准备数据，指定字符串的字符集为 utf-8
		byte[] bytes = "hello netty。".getBytes(Charset.forName("utf-8"));

		//3. 填充数据到buffer
		buffer.writeBytes(bytes);

		return buffer;
	}
}
