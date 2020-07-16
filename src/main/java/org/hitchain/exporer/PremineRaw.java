package org.hitchain.exporer;

import org.ethereum.core.Denomination;

import java.math.BigInteger;

public class PremineRaw {

    byte[] addr;
    BigInteger value;
    org.ethereum.core.Denomination denomination;

    public PremineRaw(byte[] addr, BigInteger value, org.ethereum.core.Denomination denomination) {
        this.addr = addr;
        this.value = value;
        this.denomination = denomination;
    }

    public byte[] getAddr() {
        return addr;
    }

    public BigInteger getValue() {
        return value;
    }

    public Denomination getDenomination() {
        return denomination;
    }
}
