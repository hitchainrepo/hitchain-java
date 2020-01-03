/*******************************************************************************
 * Copyright (c) 2019-11-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.net.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeaderWrapper;
import org.ethereum.core.Transaction;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.handler.Eth;
import org.ethereum.net.eth.handler.EthAdapter;
import org.ethereum.net.eth.handler.EthHandler;
import org.ethereum.net.eth.handler.EthHandlerFactory;
import org.ethereum.net.eth.message.Eth62MessageFactory;
import org.ethereum.net.eth.message.Eth63MessageFactory;
import org.ethereum.net.message.MessageFactory;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.p2p.P2pMessageFactory;
import org.ethereum.net.rlpx.*;
import org.ethereum.net.rlpx.discover.NodeManager;
import org.ethereum.net.rlpx.discover.NodeStatistics;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.net.server.PeerStatistics;
import org.ethereum.net.server.WireTrafficStats;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.shh.ShhMessageFactory;
import org.ethereum.net.swarm.bzz.BzzHandler;
import org.ethereum.net.swarm.bzz.BzzMessageFactory;
import org.ethereum.sync.SyncStatistics;
import org.ethereum.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Channel
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-11-08
 */
@Slf4j(topic = "net")
@Component
@Scope("prototype")
public class Channel {

    public static final int MAX_SAFE_TXS = 192;
    @Autowired
    SystemProperties config;
    @Autowired
    private MessageQueue msgQueue;
    @Autowired
    private P2pHandler p2pHandler;
    @Autowired
    private ShhHandler shhHandler;
    @Autowired
    private BzzHandler bzzHandler;
    @Autowired
    private MessageCodec messageCodec;
    @Autowired
    private HandshakeHandler handshakeHandler;
    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private EthHandlerFactory ethHandlerFactory;
    @Autowired
    private StaticMessages staticMessages;
    @Autowired
    private WireTrafficStats stats;
    private ChannelManager channelManager;
    private Eth eth = new EthAdapter();
    private InetSocketAddress inetSocketAddress;
    private Node node;
    private NodeStatistics nodeStatistics;
    private boolean discoveryMode;
    private boolean isActive;
    private boolean isDisconnected;
    private String remoteId;
    private PeerStatistics peerStats = new PeerStatistics();

    public void init(ChannelPipeline pipeline, String remoteId, boolean discoveryMode, ChannelManager channelManager) {
        this.channelManager = channelManager;
        this.remoteId = remoteId;

        isActive = remoteId != null && !remoteId.isEmpty();

        pipeline.addLast("readTimeoutHandler",
                new ReadTimeoutHandler(config.peerChannelReadTimeout(), TimeUnit.SECONDS));
        pipeline.addLast(stats.tcp);
        pipeline.addLast("handshakeHandler", handshakeHandler);

        this.discoveryMode = discoveryMode;

        if (discoveryMode) {
            // temporary key/nodeId to not accidentally smear our reputation with
            // unexpected disconnect
//            handshakeHandler.generateTempKey();
        }

        handshakeHandler.setRemoteId(remoteId, this);

        messageCodec.setChannel(this);

        msgQueue.setChannel(this);

        p2pHandler.setMsgQueue(msgQueue);
        messageCodec.setP2pMessageFactory(new P2pMessageFactory());

        shhHandler.setMsgQueue(msgQueue);
        messageCodec.setShhMessageFactory(new ShhMessageFactory());

        bzzHandler.setMsgQueue(msgQueue);
        messageCodec.setBzzMessageFactory(new BzzMessageFactory());
    }

    public void publicRLPxHandshakeFinished(ChannelHandlerContext ctx, FrameCodec frameCodec,
                                            HelloMessage helloRemote) throws IOException, InterruptedException {

        log.debug("publicRLPxHandshakeFinished with " + ctx.channel().remoteAddress());

        messageCodec.setSupportChunkedFrames(false);

        FrameCodecHandler frameCodecHandler = new FrameCodecHandler(frameCodec, this);
        ctx.pipeline().addLast("medianFrameCodec", frameCodecHandler);

        if (SnappyCodec.isSupported(Math.min(config.defaultP2PVersion(), helloRemote.getP2PVersion()))) {
            ctx.pipeline().addLast("snappyCodec", new SnappyCodec(this));
            log.debug("{}: use snappy compression", ctx.channel());
        }

        ctx.pipeline().addLast("messageCodec", messageCodec);
        ctx.pipeline().addLast(Capability.P2P, p2pHandler);

        p2pHandler.setChannel(this);
        p2pHandler.setHandshake(helloRemote, ctx);

        getNodeStatistics().rlpxHandshake.add();
    }

    public void sendHelloMessage(ChannelHandlerContext ctx, FrameCodec frameCodec,
                                 String nodeId) throws IOException, InterruptedException {

        final HelloMessage helloMessage = staticMessages.createHelloMessage(nodeId);

        ByteBuf byteBufMsg = ctx.alloc().buffer();
        frameCodec.writeFrame(new FrameCodec.Frame(helloMessage.getCode(), helloMessage.getEncoded()), byteBufMsg);
        ctx.writeAndFlush(byteBufMsg).sync();

        if (log.isDebugEnabled())
            log.debug("To:   {}    Send:  {}", ctx.channel().remoteAddress(), helloMessage);
        getNodeStatistics().rlpxOutHello.add();
    }

    public void activateEth(ChannelHandlerContext ctx, EthVersion version) {
        EthHandler handler = ethHandlerFactory.create(version);
        MessageFactory messageFactory = createEthMessageFactory(version);
        messageCodec.setEthVersion(version);
        messageCodec.setEthMessageFactory(messageFactory);

        log.debug("Eth{} [ address = {} | id = {} ]", handler.getVersion(), inetSocketAddress, getPeerIdShort());

        ctx.pipeline().addLast(Capability.ETH, handler);

        handler.setMsgQueue(msgQueue);
        handler.setChannel(this);
        handler.setPeerDiscoveryMode(discoveryMode);

        handler.activate();

        eth = handler;
    }

    private MessageFactory createEthMessageFactory(EthVersion version) {
        switch (version) {
            case V62:
                return new Eth62MessageFactory();
            case V63:
                return new Eth63MessageFactory();
            default:
                throw new IllegalArgumentException("Eth " + version + " is not supported");
        }
    }

    public void activateShh(ChannelHandlerContext ctx) {
        ctx.pipeline().addLast(Capability.SHH, shhHandler);
        shhHandler.activate();
    }

    public void activateBzz(ChannelHandlerContext ctx) {
        ctx.pipeline().addLast(Capability.BZZ, bzzHandler);
        bzzHandler.activate();
    }

    public NodeStatistics getNodeStatistics() {
        return nodeStatistics;
    }

    /**
     * Set node and register it in NodeManager if it is not registered yet.
     */
    public void initWithNode(byte[] nodeId, int remotePort) {
        node = new Node(nodeId, inetSocketAddress.getHostString(), remotePort);
        nodeStatistics = nodeManager.getNodeStatistics(node);
    }

    public void initWithNode(byte[] nodeId) {
        initWithNode(nodeId, inetSocketAddress.getPort());
    }

    public Node getNode() {
        return node;
    }

    public void initMessageCodes(List<Capability> caps) {
        messageCodec.initMessageCodes(caps);
    }

    public boolean isProtocolsInitialized() {
        return eth.hasStatusPassed();
    }

    public void onDisconnect() {
        isDisconnected = true;
    }

    public boolean isDisconnected() {
        return isDisconnected;
    }

    public void onSyncDone(boolean done) {

        if (done) {
            eth.enableTransactions();
        } else {
            eth.disableTransactions();
        }

        eth.onSyncDone(done);
    }

    public boolean isDiscoveryMode() {
        return discoveryMode;
    }

    public String getPeerId() {
        return node == null ? "<null>" : node.getHexId();
    }

    public String getPeerIdShort() {
        return node == null ? (remoteId != null && remoteId.length() >= 8 ? remoteId.substring(0, 8) : remoteId)
                : node.getHexIdShort();
    }

    public byte[] getNodeId() {
        return node == null ? null : node.getId();
    }

    /**
     * Indicates whether this connection was initiated by our peer
     */
    public boolean isActive() {
        return isActive;
    }

    public ByteArrayWrapper getNodeIdWrapper() {
        return node == null ? null : new ByteArrayWrapper(node.getId());
    }

    public void disconnect(ReasonCode reason) {
        getNodeStatistics().nodeDisconnectedLocal(reason);
        msgQueue.disconnect(reason);
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }

    public PeerStatistics getPeerStats() {
        return peerStats;
    }

    // ETH sub protocol

    public void fetchBlockBodies(List<BlockHeaderWrapper> headers) {
        eth.fetchBodies(headers);
    }

    public boolean isEthCompatible(org.ethereum.net.server.Channel peer) {
        return peer != null && peer.getEthVersion().isCompatible(getEthVersion());
    }

    public Eth getEthHandler() {
        return eth;
    }

    public boolean hasEthStatusSucceeded() {
        return eth.hasStatusSucceeded();
    }

    public String logSyncStats() {
        return eth.getSyncStats();
    }

    public BigInteger getTotalDifficulty() {
        return getEthHandler().getTotalDifficulty();
    }

    public SyncStatistics getSyncStats() {
        return eth.getStats();
    }

    public boolean isHashRetrievingDone() {
        return eth.isHashRetrievingDone();
    }

    public boolean isHashRetrieving() {
        return eth.isHashRetrieving();
    }

    public boolean isMaster() {
        return eth.isHashRetrieving() || eth.isHashRetrievingDone();
    }

    public boolean isIdle() {
        return eth.isIdle();
    }

    public void prohibitTransactionProcessing() {
        eth.disableTransactions();
    }

    /**
     * Send transactions from input to peer corresponded with channel
     * Using {@link #sendTransactionsCapped(List)} is recommended instead
     *
     * @param txs Transactions
     */
    public void sendTransactions(List<Transaction> txs) {
        eth.sendTransaction(txs);
    }

    /**
     * Sames as {@link #sendTransactions(List)} but input list is randomly sliced to
     * contain not more than {@link #MAX_SAFE_TXS} if needed
     *
     * @param txs List of txs to send
     */
    public void sendTransactionsCapped(List<Transaction> txs) {
        List<Transaction> slicedTxs;
        if (txs.size() <= MAX_SAFE_TXS) {
            slicedTxs = txs;
        } else {
            slicedTxs = CollectionUtils.truncateRand(txs, MAX_SAFE_TXS);
        }
        eth.sendTransaction(slicedTxs);
    }

    public void sendNewBlock(Block block) {
        eth.sendNewBlock(block);
    }

    public void sendNewBlockHashes(Block block) {
        eth.sendNewBlockHashes(block);
    }

    public EthVersion getEthVersion() {
        return eth.getVersion();
    }

    public void dropConnection() {
        eth.dropConnection();
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        org.ethereum.net.server.Channel channel = (org.ethereum.net.server.Channel) o;


        if (inetSocketAddress != null ? !inetSocketAddress.equals(channel.inetSocketAddress) : channel.inetSocketAddress != null)
            return false;
        if (node != null ? !node.equals(channel.node) : channel.node != null) return false;
        return false;
    }

    @Override
    public int hashCode() {
        int result = inetSocketAddress != null ? inetSocketAddress.hashCode() : 0;
        result = 31 * result + (node != null ? node.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s | %s", getPeerIdShort(), inetSocketAddress);
    }
}
