/*******************************************************************************
 * Copyright (c) 2019-11-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.core.genesis;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * GenesisJson
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-11-08
 */
@Data
@Accessors(fluent = true)
public class GenesisJson {

    String mixhash;
    String coinbase;
    String timestamp;
    String parentHash;
    String extraData;
    String gasLimit;
    String nonce;
    String difficulty;

    Map<String, AllocatedAccount> alloc;

    GenesisConfig config;

    public GenesisJson() {
    }

    public static class AllocatedAccount {

        public Map<String, String> storage;
        public String nonce;
        public String code;
        public String balance;

    }
}
