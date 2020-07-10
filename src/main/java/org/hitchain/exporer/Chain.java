package org.hitchain.exporer;

import org.ethereum.core.Block;
import org.ethereum.db.ByteArrayWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Chain {

    private static final Logger logger = LoggerFactory.getLogger("blockchain");

    private List<org.ethereum.core.Block> chain = new ArrayList<>();
    private BigInteger totalDifficulty = BigInteger.ZERO;
    private Map<ByteArrayWrapper, org.ethereum.core.Block> index = new HashMap<>();


    public boolean tryToConnect(org.ethereum.core.Block block) {

        if (chain.isEmpty()) {
            add(block);
            return true;
        }

        org.ethereum.core.Block lastBlock = chain.get(chain.size() - 1);
        if (lastBlock.isParentOf(block)) {
            add(block);
            return true;
        }
        return false;
    }

    public void add(org.ethereum.core.Block block) {
        logger.info("adding block to alt chain block.hash: [{}] ", block.getShortHash());
        totalDifficulty = totalDifficulty.add(block.getDifficultyBI());
        logger.info("total difficulty on alt chain is: [{}] ", totalDifficulty);
        chain.add(block);
        index.put(new ByteArrayWrapper(block.getHash()), block);
    }

    public org.ethereum.core.Block get(int i) {
        return chain.get(i);
    }

    public org.ethereum.core.Block getLast() {
        return chain.get(chain.size() - 1);
    }

    public BigInteger getTotalDifficulty() {
        return totalDifficulty;
    }

    public void setTotalDifficulty(BigInteger totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }

    public boolean isParentOnTheChain(Block block) {
        return (index.get(new ByteArrayWrapper(block.getParentHash())) != null);
    }

    public long getSize() {
        return chain.size();
    }


}
