package brainwine.gameserver.server;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.core.MessagePack;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import brainwine.gameserver.serialization.BlockSerializer;
import brainwine.gameserver.serialization.ItemCodeSerializer;
import brainwine.gameserver.serialization.MessageSerializer;
import brainwine.gameserver.serialization.NetworkChunkSerializer;
import brainwine.gameserver.serialization.RequestDeserializerModifier;
import brainwine.gameserver.server.pipeline.Connection;
import brainwine.gameserver.server.pipeline.MessageEncoder;
import brainwine.gameserver.server.pipeline.RequestDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * Simple class for managing endpoints.
 */
public class Server {
    
    private static final Logger logger = LogManager.getLogger();
    private static final ThreadFactory threadFactory = new DefaultThreadFactory("netty");
    private static final ObjectMapper mapper = new ObjectMapper(new MessagePackFactory(
            MessagePack.DEFAULT_PACKER_CONFIG.withStr8FormatSupport(false)))
            .configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true)
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .registerModule(new SimpleModule()
                    .setDeserializerModifier(RequestDeserializerModifier.INSTANCE)
                    .addSerializer(MessageSerializer.INSTANCE)
                    .addSerializer(BlockSerializer.INSTANCE)
                    .addSerializer(NetworkChunkSerializer.INSTANCE)
                    .addSerializer(ItemCodeSerializer.INSTANCE));
    private static final ObjectWriter writer = mapper.writer();
    private static final ObjectReader reader = mapper.reader();
    private final List<ChannelFuture> endpoints = new ArrayList<>();
    private final Class<? extends ServerChannel> channelType;
    private final EventLoopGroup eventLoopGroup;
    
    public Server() {
        if(Epoll.isAvailable()) {
            channelType = EpollServerSocketChannel.class;
            eventLoopGroup = new EpollEventLoopGroup(0, threadFactory);
        } else {
            channelType = NioServerSocketChannel.class;
            eventLoopGroup = new NioEventLoopGroup(0, threadFactory);
        }
        
        logger.info("Using channel type {}", eventLoopGroup.getClass());
    }
    
    public void addEndpoint(int port) {
        logger.info("Opening endpoint @ port {} ...", port);
        endpoints.add(new ServerBootstrap().group(eventLoopGroup).channel(channelType).childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                Connection connection = new Connection();
                channel.pipeline().addLast("framer", new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 1024, 1, 4, 0, 0, true));
                channel.pipeline().addLast("encoder", new MessageEncoder(writer, connection));
                channel.pipeline().addLast("decoder", new RequestDecoder(reader));
                channel.pipeline().addLast("handler", connection);
            }
        }).bind(port).syncUninterruptibly());
    }
    
    public void close() {
        logger.info("Closing endpoints ...");
        eventLoopGroup.shutdownGracefully();
    }
}
