/*******************************************************************************
 * Copyright (c) 2019-11-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.net.server;

/**
 * PeerStatistics
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-11-08
 */
public class PeerStatistics {
    private double avgLatency = 0;
    private long pingCount = 0;

    public void pong(long pingStamp) {
        long latency = System.currentTimeMillis() - pingStamp;
        avgLatency = ((avgLatency * pingCount) + latency) / ++pingCount;
    }

    public double getAvgLatency() {
        return avgLatency;
    }
}
