/*******************************************************************************
 * Copyright (c) 2019-11-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.core.genesis;

import java.util.List;

/**
 * GenesisConfig
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-11-08
 */
public class GenesisConfig {
    public Integer homesteadBlock;
    public Integer daoForkBlock;
    public Integer eip150Block;
    public Integer eip155Block;
    public boolean daoForkSupport;
    public Integer eip158Block;
    public Integer byzantiumBlock;
    public Integer constantinopleBlock;
    public Integer petersburgBlock;
    public Integer chainId;

    // EthereumJ private options

    public static class HashValidator {
        public long number;
        public String hash;
    }

    public List<HashValidator> headerValidators;

    public boolean isCustomConfig() {
        return homesteadBlock != null || daoForkBlock != null || eip150Block != null ||
                eip155Block != null || eip158Block != null || byzantiumBlock != null ||
                constantinopleBlock != null || petersburgBlock != null;
    }
}
