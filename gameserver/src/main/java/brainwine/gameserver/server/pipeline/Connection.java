package brainwine.gameserver.server.pipeline;

import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.Request;
import brainwine.gameserver.server.messages.KickMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class Connection extends SimpleChannelInboundHandler<Request> {

    private Logger logger = LogManager.getLogger();
    private Channel channel;
    private SocketAddress address;
    private Player player;
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channel = ctx.channel();
        address = channel.remoteAddress();
        logger = LogManager.getLogger(address.toString());
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        kick("Connection terminated");
        
        if(player == null) {
            return;
        }
        
        GameServer.getInstance().queueSynchronousTask(new Runnable() {
            @Override
            public void run() {
                player.onDisconnect();
            }
        });
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        GameServer.getInstance().queueSynchronousTask(new Runnable() {
            @Override
            public void run() {
                request.process(Connection.this);
            }
        });
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause instanceof ClosedChannelException) {
            return;
        }
        
        String error = cause.getMessage();
        logger.warn(error);
        //kick(error);
    }
    
    public ChannelFuture sendMessage(Message message) {
        return channel.writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }
    
    public void sendDelayedMessage(Message message, int delay) {
        channel.eventLoop().schedule(() -> {
            sendMessage(message);
        }, delay, TimeUnit.MILLISECONDS);
    }
    
    public void kick(String reason) {
        kick(reason, false);
    }
    
    public void kick(String reason, boolean shouldReconnect) {
        if(isOpen()) {
            sendMessage(new KickMessage(reason, shouldReconnect)).addListener(ChannelFutureListener.CLOSE);
        }
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public boolean isOpen() {
        return channel.isOpen();
    }
}
