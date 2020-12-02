package com.fd.proxytunnel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.IOException;

public interface Connection {
    /**
     * close connection
     */
    void closeConnection();

    /**
     * @return {@link Channel}
     */
    Channel channel();

    /**
     * write and flush message
     * @param message message instances
     * @return {@link ChannelFuture}
     */
    ChannelFuture writeAndFlush(Object message);

    /**
     * @return true if connection is active
     */
    boolean isActive();

    /**
     * Connect
     *
     * @return {@link ChannelFuture} channel future
     * @exception  {@link IOException} io exception
     */
    ChannelFuture connect() throws IOException;

    default void checkNotNull(String message, Object object) {
        if (object == null) {
            throw new NullPointerException(message + " is null");
        }
    }
}
