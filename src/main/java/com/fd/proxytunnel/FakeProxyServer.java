package com.fd.proxytunnel;

import com.fd.proxytunnel.handlers.ConnectionFromClientHandler;
import com.fd.proxytunnel.handlers.StateHandler;
import com.fd.proxytunnel.mapping.Address;
import com.fd.proxytunnel.mapping.MappingHolder;
import com.fd.proxytunnel.mapping.TunnelMap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FakeProxyServer {
    private static final Logger LOG = LoggerFactory.getLogger(FakeProxyServer.class);
    private final Configuration configuration;
    private volatile EventLoopGroup bossGroup;
    private volatile EventLoopGroup workerGroup;

    public FakeProxyServer(Configuration configuration) {
        this.configuration = configuration;
    }

    public void startup() {
        bossGroup = ChannelUtils.createEventLoopGroup(configuration.mainEventGroupNumber());
        workerGroup = ChannelUtils.createEventLoopGroup(configuration.workerEventGroupNumber());
        // loop create bootstraps
        MappingHolder mappingHolder = configuration.mappingHolder();
        for (Map.Entry<String, TunnelMap> tunnelMapEntry : mappingHolder.entrySet()) {
            TunnelMap tunnelMap = tunnelMapEntry.getValue();
            Address localAddress = tunnelMap.localAddress;
            Address remoteAddress = tunnelMap.remoteAddress;
            ServerBootstrap serverBootstrap = buildServerBootstrap(remoteAddress);
            serverBootstrap.bind(localAddress.host, localAddress.port).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) {
                    if (channelFuture.isSuccess()) {
                        LOG.info("fake proxy server success bind on: {}:{}", localAddress.host, localAddress.port);
                    } else {
                        LOG.error("fake proxy server failed bind on: {}:{}", localAddress.host, localAddress.port, channelFuture.cause());
                    }
                }
            });
        }
    }

    public void shutdown() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
        LOG.info("fake server shutdown");
    }

    private ServerBootstrap buildServerBootstrap(Address remoteAddress) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(ChannelUtils.defaultServerSocketChannelClass())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 0, configuration.idleTimeoutForClient(), TimeUnit.MILLISECONDS));
                        pipeline.addLast(new StateHandler());
                        pipeline.addLast(Constants.MAIN_HANDLER, new ConnectionFromClientHandler(configuration, remoteAddress,false));
                        if (configuration.openNettyLoggingHandler()) {
                            pipeline.addFirst(Constants.NETTY_LOGGING_HANDLER_NAME, Constants.DEBUG_LOGGING_HANDLER);
                        }
                    }
                })
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, configuration.maxConnectionBacklog())
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.AUTO_READ, false);
        if (Constants.LINUX) {
            serverBootstrap.childOption(EpollChannelOption.TCP_QUICKACK, true);
        }
        if (configuration.openNettyLoggingHandler()) {
            serverBootstrap.handler(Constants.DEBUG_LOGGING_HANDLER);
        }
        return serverBootstrap;
    }

}
