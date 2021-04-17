package brainwine.gameserver.server;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.gameserver.server.pipeline.CommandDecoder;
import brainwine.gameserver.server.pipeline.Connection;
import brainwine.gameserver.server.pipeline.MessageEncoder;
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
                channel.pipeline().addLast("framer", new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, Short.MAX_VALUE, 1, 4, 0, 0, true));
                channel.pipeline().addLast("encoder", new MessageEncoder());
                channel.pipeline().addLast("decoder", new CommandDecoder());
                channel.pipeline().addLast("handler", new Connection());
            }
        }).bind(port).syncUninterruptibly());
    }
    
    public void close() {
        logger.info("Closing endpoints ...");
        eventLoopGroup.shutdownGracefully();
    }
}
