/*******************************************************************************
 * Copyright (c) 2019-11-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.net.dht;

import lombok.experimental.Accessors;
import org.hitchain.crypto.HashUtil;
import org.spongycastle.util.BigIntegers;

import java.math.BigInteger;

import static org.hitchain.util.ByteUtil.toHexString;

/**
 * Peer
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-11-08
 */
@Accessors(fluent = true)
public class Peer {
    byte[] id;
    String host = "127.0.0.1";
    int port = 0;

    public Peer(byte[] id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public Peer(byte[] ip) {
        this.id = ip;
    }

    public Peer() {
        HashUtil.randomPeerId();
    }

    public byte nextBit(String startPattern) {
        if (this.toBinaryString().startsWith(startPattern + "1")) {
            return 1;
        } else {
            return 0;
        }
    }

    public byte[] calcDistance(Peer toPeer) {

        BigInteger aPeer = new BigInteger(id);
        BigInteger bPeer = new BigInteger(toPeer.id);

        BigInteger distance = aPeer.xor(bPeer);
        return BigIntegers.asUnsignedByteArray(distance);
    }

    @Override
    public String toString() {
        return String.format("Peer {\n id=%s, \n host=%s, \n port=%d\n}", toHexString(id), host, port);
    }

    public String toBinaryString() {

        BigInteger bi = new BigInteger(1, id);
        String out = String.format("%512s", bi.toString(2));
        out = out.replace(' ', '0');

        return out;
    }
}
