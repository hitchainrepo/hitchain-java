/*******************************************************************************
 * Copyright (c) 2019-11-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.net.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.ethereum.config.SystemProperties;
import org.ethereum.listener.EthereumListener;
import org.hitchain.net.server.EthereumChannelInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import static org.hitchain.util.ByteUtil.toHexString;

/**
 * PeerServer
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-11-08
 */
@Slf4j(topic = "net")
@Component
public class PeerServer {

    public EthereumChannelInitializer ethereumChannelInitializer;
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    ChannelFuture channelFuture;
    private SystemProperties config;
    private ApplicationContext ctx;
    private EthereumListener ethereumListener;
    private boolean listening;

    @Autowired
    public PeerServer(final SystemProperties config, final ApplicationContext ctx,
                      final EthereumListener ethereumListener) {
        this.ctx = ctx;
        this.config = config;
        this.ethereumListener = ethereumListener;
    }

    public void start(int port) {

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ethereumChannelInitializer = ctx.getBean(EthereumChannelInitializer.class, "");

        ethereumListener.trace("Listening on port " + port);


        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);

            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.peerConnectionTimeout());

            b.handler(new LoggingHandler());
            b.childHandler(ethereumChannelInitializer);

            // Start the client.
            log.info("Listening for incoming connections, port: [{}] ", port);
            log.info("NodeId: [{}] ", toHexString(config.nodeId()));

            channelFuture = b.bind(port).sync();

            listening = true;
            // Wait until the connection is closed.
            channelFuture.channel().closeFuture().sync();
            log.debug("Connection is closed");

        } catch (Exception e) {
            log.error("Peer server error: {} ({})", e.getMessage(), e.getClass().getName());
            throw new Error("Server Disconnected");
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            listening = false;
        }
    }

    public void close() {
        if (!(listening && channelFuture != null && channelFuture.channel().isOpen())) {
            return;
        }
        try {
            log.info("Closing PeerServer...");
            channelFuture.channel().close().sync();
            log.info("PeerServer closed.");
        } catch (Exception e) {
            log.warn("Problems closing server channel", e);
        }
    }

    public boolean isListening() {
        return listening;
    }
}
