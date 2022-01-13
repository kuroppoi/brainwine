package brainwine.gameserver.server.pipeline;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectReader;

import brainwine.gameserver.server.NetworkRegistry;
import brainwine.gameserver.server.Request;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class RequestDecoder extends MessageToMessageDecoder<ByteBuf> {
    
    private final ObjectReader reader;
    
    public RequestDecoder(ObjectReader reader) {
        this.reader = reader;
    }
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        int id = buf.readByte() & 0xFF;
        int length = buf.readIntLE();
        
        if(length > 1024) {
            throw new IOException("Request exceeds max length of 1024 bytes");
        }
        
        Class<? extends Request> type = NetworkRegistry.getRequestClass(id);
        
        if(type == null) {
            throw new IOException("Client sent invalid request: " + id);
        }
        
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        Request request = reader.readValue(bytes, type);
        out.add(request);
    }
}
