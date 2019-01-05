package netty.push.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;
import java.util.List;

import static netty.push.codec.Packet.decodePacket;

/**
 * Created by sundingding on 2019/1/2.
 */
public final class PacketDecoder extends ByteToMessageDecoder {
	private static final int maxPacketSize = 1024;

	@Override protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		decodeHeartbeat(in, out);
		decodeFrames(in, out);
	}

	private void decodeHeartbeat(ByteBuf in, List<Object> out) {
		while (in.isReadable()) {
			if (in.readByte() == Packet.HB_PACKET_BYTE) {
				out.add(Packet.HB_PACKET);
			} else {
				in.readerIndex(in.readerIndex() - 1);
				break;
			}
		}
	}

	private void decodeFrames(ByteBuf in, List<Object> out) {
		if (in.readableBytes() >= Packet.HEADER_LEN) {
			//1.记录当前读取位置位置.如果读取到非完整的frame,要恢复到该位置,便于下次读取
			in.markReaderIndex();

			Packet packet = decodeFrame(in);
			if (packet != null) {
				out.add(packet);
			} else {
				//2.读取到不完整的frame,恢复到最近一次正常读取的位置,便于下次读取
				in.resetReaderIndex();
			}
		}
	}

	private Packet decodeFrame(ByteBuf in) {
		int readableBytes = in.readableBytes();
		int bodyLength = in.readInt();
		if (readableBytes < (bodyLength + Packet.HEADER_LEN)) {
			return null;
		}
		if (bodyLength > maxPacketSize) {
			throw new TooLongFrameException("packet body length over limit:" + bodyLength);
		}
		return decodePacket(new Packet(in.readByte()), in, bodyLength);
	}

	public static Packet decodeFrame(DatagramPacket frame) {
		ByteBuf in = frame.content();
		int readableBytes = in.readableBytes();
		int bodyLength = in.readInt();
		if (readableBytes < (bodyLength + Packet.HEADER_LEN)) {
			return null;
		}

		return decodePacket(new UDPPacket(in.readByte(), frame.sender()), in, bodyLength);
	}

	public static Packet decodeFrame(String frame) throws Exception {
		if (frame == null)
			return null;
		return Jsons.fromJson(frame, JsonPacket.class);
	}
}
