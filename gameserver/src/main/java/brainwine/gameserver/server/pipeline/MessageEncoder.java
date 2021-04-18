package brainwine.gameserver.server.pipeline;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.msgpack.packer.BufferPacker;

import com.fasterxml.jackson.databind.ObjectMapper;

import brainwine.gameserver.msgpack.MessagePackHelper;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageOptions;
import brainwine.gameserver.server.NetworkRegistry;
import brainwine.gameserver.util.ZipUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<Message> {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Message in, ByteBuf out) throws Exception {
        int id = NetworkRegistry.getMessageId(in);
        MessageOptions options = NetworkRegistry.getMessageOptions(in);
        
        if(id == 0) {
            throw new IOException("Attempted to encode unregistered message " + in.getClass());
        }
        
        out.writeByte(id);
        out.writeIntLE(0); // Length field is set at the end.
        byte[] bytes = null;
        
        if(options.isJson()) {
            List<Object> data = new ArrayList<>();
            
            for(Field field : in.getClass().getFields()) {
                data.add(field.get(in));
            }
            
            bytes = mapper.writer().writeValueAsString(data).getBytes();
        } else {
            BufferPacker packer = MessagePackHelper.createBufferPacker();
            in.pack(packer);
            packer.close();
            bytes = packer.toByteArray();
        }
        
        if(options.isCompressed()) {
            bytes = ZipUtils.deflateBytes(bytes);
        }
        
        out.writeBytes(bytes);
        out.setIntLE(1, bytes.length);
    }
}
