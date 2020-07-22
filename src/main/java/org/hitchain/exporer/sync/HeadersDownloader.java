package org.hitchain.exporer.sync;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockHeaderWrapper;
import org.ethereum.core.BlockWrapper;
import org.ethereum.core.Blockchain;
import org.ethereum.db.DbFlushManager;
import org.ethereum.db.HeaderStore;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.net.server.Channel;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.validator.BlockHeaderValidator;
import org.ethereum.validator.EthashRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.ethereum.util.ByteUtil.toHexString;

@Component
@Lazy
public class HeadersDownloader extends BlockDownloader {
    private final static Logger logger = LoggerFactory.getLogger("sync");

    @Autowired
    SyncPool syncPool;

    @Autowired
    ChannelManager channelManager;

    @Autowired
    IndexedBlockStore blockStore;

    @Autowired
    HeaderStore headerStore;

    @Autowired
    DbFlushManager dbFlushManager;

    @Autowired
    Blockchain blockchain;

    byte[] genesisHash;

    int headersLoaded  = 0;

    private EthashRule reverseEthashRule;

    @Autowired
    public HeadersDownloader(BlockHeaderValidator headerValidator, SystemProperties systemProperties) {
        super(headerValidator);
        reverseEthashRule = EthashRule.createReverse(systemProperties);
        setHeaderQueueLimit(200000);
        setBlockBodiesDownload(false);
        logger.info("HeaderDownloader created.");
    }

    public void init(byte[] startFromBlockHash) {
        logger.info("HeaderDownloader init: startHash = " + toHexString(startFromBlockHash));
        SyncQueueReverseImpl syncQueue = new SyncQueueReverseImpl(startFromBlockHash, true);
        super.init(syncQueue, syncPool, "HeadersDownloader");
        syncPool.init(channelManager, blockchain);
    }

    @Override
    protected synchronized void pushBlocks(List<BlockWrapper> blockWrappers) {}

    @Override
    protected void pushHeaders(List<BlockHeaderWrapper> headers) {
        if (headers.get(headers.size() - 1).getNumber() == 0) {
            genesisHash = headers.get(headers.size() - 1).getHash();
        }
        if (headers.get(headers.size() - 1).getNumber() == 1) {
            genesisHash = headers.get(headers.size() - 1).getHeader().getParentHash();
        }
        logger.info(name + ": " + headers.size() + " headers loaded: " + headers.get(0).getNumber() + " - " + headers.get(headers.size() - 1).getNumber());
        for (BlockHeaderWrapper header : headers) {
            headerStore.saveHeader(header.getHeader());
            headersLoaded++;
        }
        dbFlushManager.commit();
    }

    /**
     * Headers download could block chain synchronization occupying all peers
     * Prevents this by leaving one peer without work
     * Fallbacks to any peer when low number of active peers available
     */
    @Override
    Channel getAnyPeer() {
        return syncPool.getActivePeersCount() > 2 ? syncPool.getNotLastIdle() : syncPool.getAnyIdle();
    }

    @Override
    protected int getBlockQueueFreeSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected int getMaxHeadersInQueue() {
        return getHeaderQueueLimit();
    }

    public int getHeadersLoaded() {
        return headersLoaded;
    }

    @Override
    protected void finishDownload() {
        stop();
    }

    public byte[] getGenesisHash() {
        return genesisHash;
    }

    @Override
    protected boolean isValid(BlockHeader header) {
        return super.isValid(header) && reverseEthashRule.validateAndLog(header, logger);
    }
}
