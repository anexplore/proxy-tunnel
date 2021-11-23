package com.fd.proxytunnel.handlers;

import com.fd.proxytunnel.ChannelUtils;
import com.fd.proxytunnel.Configuration;
import com.fd.proxytunnel.Connection;
import com.fd.proxytunnel.Constants;
import com.fd.proxytunnel.mapping.Address;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class ConnectionToSslEndPoint implements Connection {
    public static final Logger LOG = LoggerFactory.getLogger(ConnectionToSslEndPoint.class);
    private final Bootstrap bootstrap = new Bootstrap();
    private final ConnectionFromClient connectionFromClient;
    private final Configuration configuration;
    private final Address endpointAddress;
    private Channel channel;

    public ConnectionToSslEndPoint(ConnectionFromClient connectionFromClient, Configuration configuration, Address endpointAddress) {
        this.connectionFromClient = connectionFromClient;
        this.configuration = configuration;
        this.endpointAddress = endpointAddress;
    }

    /**
     * Connect to http proxy
     * @return ChannelFuture for tcp connect
     */
    public ChannelFuture connect() throws SSLException {
        // build ssl handler
        SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
        sslContextBuilder.trustManager(new File(configuration.trustCertFile()))
                .keyManager(new File(configuration.keyCertChainFile()),
                        new File(configuration.keyFile()),
                        configuration.keyPassword());
        sslContextBuilder.protocols(configuration.sslProtocol());
        SslContext context = sslContextBuilder.build();
        SslHandler sslHandler = new SslHandler(context.newEngine(ByteBufAllocator.DEFAULT));
        sslHandler.setHandshakeTimeout(configuration.connectionTimeoutToRemoteServer(), TimeUnit.MILLISECONDS);
        sslHandler.handshakeFuture().addListener(new GenericFutureListener<Future<Channel>>() {
            @Override
            public void operationComplete(Future<Channel> future) throws Exception {
                if (future.isSuccess()) {
                    LOG.info("tcp connect to ssl endpoint handshake success, try to send pending messages. {}",
                            future.get());
                    sendPendingMessages(future.get());
                } else {
                    LOG.error("tcp connect to ssl endpoint handshake failed. {}", future.get(), future.cause());
                }
            }
        });
        bootstrap.group(connectionFromClient.channel().eventLoop())
                .channel(ChannelUtils.defaultSocketChannelClass())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 0,
                                configuration.idleTimeoutForRemoteServer(), TimeUnit.MILLISECONDS));
                        pipeline.addLast(new StateHandler());
                        pipeline.addLast(Constants.SSL_HANDLER, sslHandler);
                        pipeline.addLast(Constants.MAIN_HANDLER, new DataTransferHandler(connectionFromClient));
                        if (configuration.openNettyLoggingHandler()) {
                            pipeline.addFirst(Constants.NETTY_LOGGING_HANDLER_NAME, Constants.DEBUG_LOGGING_HANDLER);
                        }
                    }
                })
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, configuration.connectionTimeoutToRemoteServer())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.AUTO_READ, false);
        if (Constants.LINUX) {
            bootstrap.option(EpollChannelOption.TCP_QUICKACK, true);
        }
        return bootstrap.connect(endpointAddress.host, endpointAddress.port).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    channel = channelFuture.channel();
                    LOG.info("tcp connect to ssl end point success, {}, {}:{}", channel, endpointAddress.host, endpointAddress.port);
                    // may be client channel has closed before tcp connect to proxy success
                    if (connectionFromClient.isConnectionClosed()) {
                        closeConnection();
                    }
                } else {
                    LOG.info("tcp connect to ssl end point failed, {}:{}", endpointAddress.host, endpointAddress.port);
                    connectionFromClient.closeConnection();
                }
            }
        });
    }

    @Override
    public ChannelFuture writeAndFlush(Object message) {
        if (!isActive()) {
            throw new RuntimeException("channel has already closed");
        }
        return channel.writeAndFlush(message);
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    @Override
    public void closeConnection() {
        LOG.info("close connection to ssl endpoint: {}", channel);
        ChannelUtils.closeOnFlush(channel);
    }

    private void sendPendingMessageFailed(Channel channel) {
        LOG.debug("send pending message failed to ssl endpoint, {}", channel);
        channel.close().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                connectionFromClient.closeConnection();
            }
        });
    }

    private void sendPendingMessages(Channel channel) {
        ChannelFutureListener pendingMessageProcessFinishedListener = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                LOG.debug("send pending messages finished with success: {}", channelFuture.isSuccess());
                if (channelFuture.isSuccess()) {
                    connectionFromClient.channel().pipeline().remove(Constants.MAIN_HANDLER);
                    connectionFromClient.channel().pipeline().addLast(Constants.MAIN_HANDLER, new DataTransferHandler(ConnectionToSslEndPoint.this));
                    // try to read more msg
                    connectionFromClient.channel().read();
                } else {
                    sendPendingMessageFailed(channel);
                }
            }
        };
        ChannelFuture last = null;
        while (connectionFromClient.pendingMessages().size() > 0) {
            last = channel.pipeline().writeAndFlush(connectionFromClient.pendingMessages().remove(0));
        }
        if (last != null) {
            last.addListener(pendingMessageProcessFinishedListener);
        }
    }
}
