package org.hitchain.exporer;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Repository;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.ByteUtil;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Genesis extends org.ethereum.core.Block {

    private Map<ByteArrayWrapper, PremineAccount> premine = new HashMap<>();

    public  static byte[] ZERO_HASH_2048 = new byte[256];
    public static byte[] DIFFICULTY = BigInteger.valueOf(2).pow(17).toByteArray();
    public static long NUMBER = 0;

    private static org.ethereum.core.Block instance;

    public Genesis(byte[] parentHash, byte[] unclesHash, byte[] coinbase, byte[] logsBloom,
                   byte[] difficulty, long number, long gasLimit,
                   long gasUsed, long timestamp,
                   byte[] extraData, byte[] mixHash, byte[] nonce){
        super(parentHash, unclesHash, coinbase, logsBloom, difficulty,
                number, ByteUtil.longToBytesNoLeadZeroes(gasLimit), gasUsed, timestamp, extraData,
                mixHash, nonce, null, null);
    }

    public static Block getInstance() {
        return SystemProperties.getDefault().getGenesis();
    }

    public static org.ethereum.core.Genesis getInstance(SystemProperties config) {
        return config.getGenesis();
    }


    public Map<ByteArrayWrapper, PremineAccount> getPremine() {
        return premine;
    }

    public void setPremine(Map<ByteArrayWrapper, PremineAccount> premine) {
        this.premine = premine;
    }

    public void addPremine(ByteArrayWrapper address, org.ethereum.core.AccountState accountState) {
        premine.put(address, new PremineAccount(accountState));
    }

    public static void populateRepository(Repository repository, Genesis genesis) {
        for (ByteArrayWrapper key : genesis.getPremine().keySet()) {
            final PremineAccount premineAccount = genesis.getPremine().get(key);
            final org.ethereum.core.AccountState accountState = premineAccount.accountState;

            repository.createAccount(key.getData());
            repository.setNonce(key.getData(), accountState.getNonce());
            repository.addBalance(key.getData(), accountState.getBalance());
            if (premineAccount.code != null) {
                repository.saveCode(key.getData(), premineAccount.code);
            }
        }
    }

    /**
     * Used to keep addition fields.
     */
    public static class PremineAccount {

        public byte[] code;

        public org.ethereum.core.AccountState accountState;

        public byte[] getStateRoot() {
            return accountState.getStateRoot();
        }

        public PremineAccount(AccountState accountState) {
            this.accountState = accountState;
        }

        public PremineAccount() {
        }
    }
}
