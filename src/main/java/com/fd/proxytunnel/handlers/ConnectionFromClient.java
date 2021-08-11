package com.fd.proxytunnel.handlers;

import com.fd.proxytunnel.ChannelUtils;
import com.fd.proxytunnel.Configuration;
import com.fd.proxytunnel.Connection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ConnectionFromClient implements Connection {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionFromClient.class);
    // channel
    private final Channel channel;
    // pending messages, this message will be share in same event group
    private final List<Object> pendingMessages;
    // Configuration
    private final Configuration configuration;

    public ConnectionFromClient(Channel channel, List<Object> pendingMessages, Configuration configuration) {
        this.channel = channel;
        this.pendingMessages = pendingMessages;
        this.configuration = configuration;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    public List<Object> pendingMessages() {
        return pendingMessages;
    }

    public Configuration configuration() {
        return configuration;
    }

    /**
     * enable channel auto read
     */
    public void enableChannelAutoRead() {
        channel.config().setAutoRead(true);
    }

    /**
     * Disable channel auto read
     */
    public void disableChannelAutoRead() {
        channel.config().setAutoRead(false);
    }

    /**
     * {@code channel} is closed
     *
     * @return true if channel is closed
     */
    public boolean isConnectionClosed() {
        return !channel.isActive();
    }

    @Override
    public ChannelFuture writeAndFlush(Object message) {
        return channel.writeAndFlush(message);
    }

    @Override
    public boolean isActive() {
        return channel.isActive();
    }

    @Override
    public ChannelFuture connect() {
        return null;
    }

    @Override
    public void closeConnection() {
        LOG.info("close connection from client: {}", channel);
        ChannelUtils.closeOnFlush(channel);
    }
}
