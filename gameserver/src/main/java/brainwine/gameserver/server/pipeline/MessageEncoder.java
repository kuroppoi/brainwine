package brainwine.gameserver.server.pipeline;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectWriter;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;
import brainwine.gameserver.server.NetworkRegistry;
import brainwine.gameserver.util.ZipUtils;
import brainwine.shared.JsonHelper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<Message> {
    
    private final ObjectWriter writer;
    private final Connection connection;
    
    public MessageEncoder(ObjectWriter writer, Connection connection) {
        this.writer = writer;
        this.connection = connection;
    }
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Message in, ByteBuf out) throws Exception {
        int id = NetworkRegistry.getMessageId(in);
        
        if(id == 0) {
            throw new IOException("Attempted to encode unregistered message " + in.getClass());
        }
        
        MessageInfo info = in.getClass().getAnnotation(MessageInfo.class);
        out.writeByte(id);
        out.writeIntLE(0); // Length field is set at the end.
        byte[] bytes = null;
        
        if(info.json() && connection.isV3()) {
            List<Object> data = new ArrayList<>();
            
            for(Field field : in.getClass().getFields()) {
                data.add(field.get(in));
            }
            
            bytes = JsonHelper.writeValueAsBytes(data);
        } else {
            bytes = writer.writeValueAsBytes(in);
        }
        
        if(info.compressed()) {
            bytes = ZipUtils.deflateBytes(bytes);
        }
        
        out.writeBytes(bytes);
        out.setIntLE(1, bytes.length);
    }
}
