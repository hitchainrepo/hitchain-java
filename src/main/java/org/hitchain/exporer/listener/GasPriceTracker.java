package org.hitchain.exporer.listener;

import org.ethereum.core.BlockSummary;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.util.ByteUtil;

import java.util.Arrays;

public class GasPriceTracker extends EthereumListenerAdapter {

    private static final long defaultPrice = 70_000_000_000L;

    private long[] window = new long[512];
    private int idx = window.length - 1;
    private boolean filled = false;

    private long lastVal;

    @Override
    public void onBlock(BlockSummary blockSummary) {
        for (Transaction tx : blockSummary.getBlock().getTransactionsList()) {
            onTransaction(tx);
        }
    }

    public void onTransaction(Transaction tx) {
        if (idx == -1) {
            idx = window.length - 1;
            filled = true;
            lastVal = 0;  // recalculate only 'sometimes'
        }
        window[idx--] = ByteUtil.byteArrayToLong(tx.getGasPrice());
    }

    public long getGasPrice() {
        if (!filled) {
            return defaultPrice;
        } else {
            if (lastVal == 0) {
                long[] longs = Arrays.copyOf(window, window.length);
                Arrays.sort(longs);
                lastVal = longs[longs.length / 4];  // 25% percentile
            }
            return lastVal;
        }
    }
}
