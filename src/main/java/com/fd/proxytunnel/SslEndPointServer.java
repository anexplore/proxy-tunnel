package com.fd.proxytunnel;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class SslEndPointServer {
    private static final Logger LOG = LoggerFactory.getLogger(SslEndPointServer.class);
    private final Configuration configuration;
    private volatile EventLoopGroup bossGroup;
    private volatile EventLoopGroup workerGroup;
    private volatile Channel channel;

    public SslEndPointServer(Configuration configuration) {
        this.configuration = configuration;
    }

    public void startup() {
        bossGroup = ChannelUtils.createEventLoopGroup(configuration.mainEventGroupNumber());
        workerGroup = ChannelUtils.createEventLoopGroup(configuration.workerEventGroupNumber());
        try {
            // build ssl handler
            SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(new File(configuration.keyCertChainFile())
                    , new File(configuration.keyFile()), configuration.keyPassword());
            sslContextBuilder.trustManager(new File(configuration.trustCertFile()));
            sslContextBuilder.protocols(configuration.sslProtocol());
            SslContext context = sslContextBuilder.build();
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(ChannelUtils.defaultServerSocketChannelClass())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new IdleStateHandler(0, 0,
                                    configuration.idleTimeoutForClient(), TimeUnit.MILLISECONDS));
                            pipeline.addLast(Constants.SSL_HANDLER, new SslHandler(context.newEngine(ByteBufAllocator.DEFAULT)));
                            pipeline.addLast(new StateHandler());
                            pipeline.addLast(Constants.MAIN_HANDLER, new ConnectionFromClientHandler(configuration, true));
                            if (configuration.openNettyLoggingHandler()) {
                                pipeline.addFirst(Constants.NETTY_LOGGING_HANDLER_NAME, Constants.DEBUG_LOGGING_HANDLER);
                            }
                        }
                    })
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_BACKLOG, configuration.maxConnectionBacklog())
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.AUTO_READ, configuration.channelAutoRead());
            if (Constants.LINUX) {
                b.childOption(EpollChannelOption.TCP_QUICKACK, true);
            }
            if (Constants.LINUX && configuration.openTcpFastOpen()) {
                b.option(EpollChannelOption.TCP_FASTOPEN, configuration.tcpFastOpenBacklog());
            }
            if (configuration.openNettyLoggingHandler()) {
                b.handler(Constants.DEBUG_LOGGING_HANDLER);
            }
            channel = b.bind(configuration.serverBindLocalAddress(), configuration.serverBindLocalPort()).sync().channel();
            LOG.info("ssl endpoint server success bind on: {}:{}", configuration.serverBindLocalAddress(), configuration.serverBindLocalPort());
        } catch (Exception error){
            LOG.error("ssl endpoint server occurs error", error);
            shutdown();
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
        if (channel != null) {
            ChannelUtils.closeOnFlush(channel);
            channel = null;
        }
        LOG.info("ssl endpoint server shutdown");
    }

}
