package netty.example.test02.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;

/**
 * @author sunding
 */
public class FirstServerHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//1. 接受到客户端的数据
		ByteBuf buffer = (ByteBuf) msg;
		System.out.println(System.currentTimeMillis() + " : 服务端读取到客户端发送的数据 -> "
				+ buffer.toString(Charset.forName("utf-8")));

		//2. 回复客户端的数据
		ByteBuf out = getByteBuf(ctx);
		ctx.channel().writeAndFlush(out);

	}

	private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
		//1. 准备buffer
		ByteBuf buffer = ctx.alloc().buffer();

		//2. 数据准备
		String content = System.currentTimeMillis() + " : 你好，客户端，已经收到数据。";
		byte[] bytes = content.getBytes(Charset.forName("utf-8"));

		//3. 写buffer
		buffer.writeBytes(bytes);

		return buffer;

	}
}
