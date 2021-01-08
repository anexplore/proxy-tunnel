package com.fd.proxytunnel;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


public final class ChannelUtils {

    /**
     * Close Channel, if channel is null or is inactive do nothing
     * @param ch channel
     */
    public static void closeOnFlush(Channel ch) {
        if (ch == null) {
            return;
        }
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * @return if platform is linux return {@link EpollSocketChannel} else {@link NioSocketChannel}
     */
    public static Class<? extends SocketChannel> defaultSocketChannelClass() {
        if (Constants.LINUX) {
            return EpollSocketChannel.class;
        }
        return NioSocketChannel.class;
    }

    /**
     * @return if platform is linux return {@link EpollServerSocketChannel} else {@link NioServerSocketChannel}
     */
    public static Class<? extends ServerSocketChannel> defaultServerSocketChannelClass() {
        if (Constants.LINUX) {
            return EpollServerSocketChannel.class;
        }
        return NioServerSocketChannel.class;
    }

    /**
     * @param coreSize event loop size
     * @return if platform is linux return {@link EpollEventLoopGroup} else {@link NioEventLoopGroup}
     */
    public static EventLoopGroup createEventLoopGroup(int coreSize) {
        if (Constants.LINUX) {
            return new EpollEventLoopGroup(coreSize);
        }
        return new NioEventLoopGroup(coreSize);
    }

}
