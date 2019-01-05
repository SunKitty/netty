package netty.push.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import static netty.push.codec.Packet.encodePacket;

/**
 * Created by sundingding on 2019/1/2.
 */
@ChannelHandler.Sharable
public final class PacketEncoder extends MessageToByteEncoder<Packet> {
	public static final PacketEncoder INSTANCE = new PacketEncoder();

	@Override
	protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
		encodePacket(packet, out);
	}
}

