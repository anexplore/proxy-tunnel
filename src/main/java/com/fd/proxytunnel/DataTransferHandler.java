package com.fd.proxytunnel;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * data transfer between channels
 */
public final class DataTransferHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(DataTransferHandler.class);

    private final Connection relayConnection;

    public DataTransferHandler(Connection relayConnection) {
        this.relayConnection = relayConnection;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        tryToReadIfNeeded(ctx);
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (relayConnection.isActive()) {
            relayConnection.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        tryToReadIfNeeded(ctx);
                    } else {
                        relayConnection.closeConnection();
                    }
                }
            });
        } else {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LOG.info("channel {} closed", ctx.channel());
        relayConnection.closeConnection();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("data transfer handler occurs error, channel: {}", ctx.channel(), cause);
        ctx.close();
    }

    private void tryToReadIfNeeded(ChannelHandlerContext ctx) {
        if (!ctx.channel().config().isAutoRead()) {
            ctx.read();
        }
    }
}
