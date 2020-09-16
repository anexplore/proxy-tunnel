package com.fd.proxytunnel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;


public class ConnectionFromClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionFromClientHandler.class);

    // pending messages which need to send to proxy server after connection established
    private List<Object> pendingMessages = new LinkedList<>();
    // stop read before connection to proxy server established
    private boolean stopRead = false;
    private final boolean forSslEndPoint;
    private final Configuration configuration;
    private Connection connectionOut;

    public ConnectionFromClientHandler(Configuration configuration, boolean forSslEndPoint) {
        this.configuration = configuration;
        this.forSslEndPoint = forSslEndPoint;
    }

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        tryToReadIfNeeded(ctx);
        ctx.fireChannelReadComplete();
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("connection from client active: {}", ctx.channel().remoteAddress());
        tryToReadIfNeeded(ctx);
        ctx.fireChannelActive();
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("connection from client inactive: {}", ctx.channel().remoteAddress());
        closeChannelConnection(ctx);
    }

    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        LOG.debug("connection from client handler received message: {}", msg);
        pendingMessages.add(msg);
        stopRead = true;
        if (connectionOut == null) {
            ConnectionFromClient cf = new ConnectionFromClient(ctx.channel(), pendingMessages, configuration);
            if (forSslEndPoint) {
                connectionOut = new ConnectionToProxy(cf, configuration);
            } else {
                connectionOut = new ConnectionToSslEndPoint(cf, configuration);
            }
			connectionOut.connect();
		}
        // this handler must be remove after connect to ssl endpoint established,
		// because we do not trigger fire channel read
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("connection from client handler occurs error", cause);
        closeChannelConnection(ctx);
    }

    private void tryToReadIfNeeded(ChannelHandlerContext ctx) {
        if (!ctx.channel().config().isAutoRead() && !stopRead) {
            ctx.read();
        }
    }

    private void closeChannelConnection(ChannelHandlerContext ctx) {
        LOG.debug("close client connection: {}", ctx.channel().remoteAddress());
        ChannelUtils.closeOnFlush(ctx.channel());
        if (connectionOut != null) {
            connectionOut.closeConnection();
        }
    }
}
