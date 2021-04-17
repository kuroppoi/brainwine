package brainwine.gameserver.server.pipeline;

import java.io.IOException;
import java.util.List;

import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.msgpack.MessagePackHelper;
import brainwine.gameserver.server.Command;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class CommandDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        int id = buf.readByte() & 0xFF;
        buf.readIntLE(); // body length
        Command command = Command.instantiateCommand(id);
        
        if(command == null) {
            throw new IOException("Client sent invalid command: " + id);
        }
        
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        Unpacker unpacker = MessagePackHelper.createBufferUnpacker(bytes);
        command.unpack(unpacker);
        unpacker.close();
        out.add(command);
    }
}
