/*******************************************************************************
 * Copyright (c) 2019-11-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.config;

/**
 * Describes a set of configs for a specific blockchain depending on the block number
 * E.g. the main Ethereum net has at least FrontierConfig and HomesteadConfig depending on the block
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-11-08
 */
public interface BlockchainNetConfig {

    /**
     * Get the config for the specific block
     */
    BlockchainConfig getConfigForBlock(long blockNumber);

    /**
     * Returns the constants common for all the blocks in this blockchain
     */
    Constants getCommonConstants();
}
