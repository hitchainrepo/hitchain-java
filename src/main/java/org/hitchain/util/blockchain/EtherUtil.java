/*******************************************************************************
 * Copyright (c) 2019-11-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.util.blockchain;

import java.math.BigInteger;

/**
 * EtherUtil
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-11-08
 */
public class EtherUtil {
    public static BigInteger convert(long amount, Unit unit) {
        return BigInteger.valueOf(amount).multiply(unit.i);
    }

    public enum Unit {
        WEI(BigInteger.valueOf(1)),
        GWEI(BigInteger.valueOf(1_000_000_000)),
        SZABO(BigInteger.valueOf(1_000_000_000_000L)),
        FINNEY(BigInteger.valueOf(1_000_000_000_000_000L)),
        ETHER(BigInteger.valueOf(1_000_000_000_000_000_000L));

        BigInteger i;

        Unit(BigInteger i) {
            this.i = i;
        }
    }
}
