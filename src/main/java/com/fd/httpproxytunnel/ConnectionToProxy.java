package com.fd.httpproxytunnel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConnectionToProxy implements Connection {
    public static final Logger LOG = LoggerFactory.getLogger(ConnectionToProxy.class);
    private final Bootstrap bootstrap = new Bootstrap();
    private final ConnectionFromClient connectionFromClient;
    private final Configuration configuration;
    private Channel channel;

    public ConnectionToProxy(ConnectionFromClient connectionFromClient, Configuration configuration) {
        this.connectionFromClient = connectionFromClient;
        this.configuration = configuration;
    }

    /**
     * Connect to http proxy
     * @return ChannelFuture for tcp connect
     */
    public ChannelFuture connect() {
        bootstrap.group(connectionFromClient.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(Constants.MAIN_HANDLER, new DataTransferHandler(connectionFromClient));
                        if (configuration.openNettyLoggingHandler()) {
                            pipeline.addFirst(Constants.NETTY_LOGGING_HANDLER_NAME, Constants.DEBUG_LOGGING_HANDLER);
                        }
                    }
                })
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, configuration.connectionTimeoutToProxyServer())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.TCP_NODELAY, true);
        return bootstrap.connect(configuration.proxyHost(), configuration.proxyPort()).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    channel = channelFuture.channel();
                    LOG.debug("tcp connect to proxy success, {}, {}:{}", channel, configuration.proxyHost(), configuration.proxyPort());
                    // may be client channel has closed before tcp connect to proxy success
                    if (connectionFromClient.isConnectionClosed()) {
                        closeConnection();
                    } else {
                        // send pending messages
                        sendPendingMessages(channel);
                    }
                } else {
                    LOG.debug("tcp connect to proxy failed, {}:{}", configuration.proxyHost(), configuration.proxyPort());
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
        LOG.debug("close connection to proxy: {}", channel);
        ChannelUtils.closeOnFlush(channel);
    }

    private void sendPendingMessageFailed(Channel channel) {
        LOG.debug("send pending message failed");
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
                    connectionFromClient.channel().pipeline().addLast(Constants.MAIN_HANDLER, new DataTransferHandler(ConnectionToProxy.this));
                    // open auto read
                    connectionFromClient.enableChannelAutoRead();
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
