/*******************************************************************************
 * Copyright (c) 2019-11-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.config;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigInteger;
import java.util.List;

/**
 * Describes constants and algorithms used for a specific blockchain at specific stage
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-11-08
 */
public interface BlockchainConfig {

    /**
     * Get blockchain constants
     */
    Constants getConstants();

    /**
     * Returns the mining algorithm
     */
    MinerIfc getMineAlgorithm(SystemProperties config);

    /**
     * Calculates the difficulty for the block depending on the parent
     */
    BigInteger calcDifficulty(BlockHeader curBlock, BlockHeader parent);

    /**
     * Calculates difficulty adjustment to target mean block time
     */
    BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent);

    /**
     * Calculates transaction gas fee
     */
    long getTransactionCost(Transaction tx);

    /**
     * Validates Tx signature (introduced in Homestead)
     */
    boolean acceptTransactionSignature(Transaction tx);

    /**
     * Validates transaction by the changes made by it in the repository
     * @param blockStore
     * @param curBlock The block being imported
     * @param repositoryTrack The repository track changed by transaction
     * @return null if all is fine or String validation error
     */
    String validateTransactionChanges(BlockStore blockStore, Block curBlock, Transaction tx,
                                      Repository repositoryTrack);


    /**
     * Prior to block processing performs some repository manipulations according
     * to HardFork rules.
     * This method is normally executes the logic on a specific hardfork block only
     * for other blocks it just does nothing
     */
    void hardForkTransfers(Block block, Repository repo);

    /**
     * DAO hard fork marker
     */
    byte[] getExtraData(byte[] minerExtraData, long blockNumber);

    /**
     * Fork related validators. Ensure that connected peer operates on the same fork with us
     * For example: DAO config will have validator that checks presence of extra data in specific block
     */
    List<Pair<Long, BlockHeaderValidator>> headerValidators();

    /**
     * EVM operations costs
     */
    GasCost getGasCost();

    /**
     * Calculates available gas to be passed for callee
     * Since EIP150
     * @param op  Opcode
     * @param requestedGas amount of gas requested by the program
     * @param availableGas available gas
     * @throws Program.OutOfGasException If passed args doesn't conform to limitations
     */
    DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws Program.OutOfGasException;

    /**
     * Calculates available gas to be passed for contract constructor
     * Since EIP150
     */
    DataWord getCreateGas(DataWord availableGas);

    /**
     * EIP161: https://github.com/ethereum/EIPs/issues/161
     */
    boolean eip161();

    /**
     * EIP155: https://github.com/ethereum/EIPs/issues/155
     */
    Integer getChainId();

    /**
     * EIP198: https://github.com/ethereum/EIPs/pull/198
     */
    boolean eip198();

    /**
     * EIP206: https://github.com/ethereum/EIPs/pull/206
     */
    boolean eip206();

    /**
     * EIP211: https://github.com/ethereum/EIPs/pull/211
     */
    boolean eip211();

    /**
     * EIP212: https://github.com/ethereum/EIPs/pull/212
     */
    boolean eip212();

    /**
     * EIP213: https://github.com/ethereum/EIPs/pull/213
     */
    boolean eip213();

    /**
     * EIP214: https://github.com/ethereum/EIPs/pull/214
     */
    boolean eip214();

    /**
     * EIP658: https://github.com/ethereum/EIPs/pull/658
     * Replaces the intermediate state root field of the receipt with the status
     */
    boolean eip658();

    /**
     * EIP145: https://eips.ethereum.org/EIPS/eip-145
     * Bitwise shifting instructions in EVM
     */
    boolean eip145();

    /**
     * EIP1052: https://eips.ethereum.org/EIPS/eip-1052
     * EXTCODEHASH opcode
     */
    boolean eip1052();

    /**
     * EIP 1283: https://eips.ethereum.org/EIPS/eip-1283
     * Net gas metering for SSTORE without dirty maps
     */
    boolean eip1283();

    /**
     * EIP 1014: https://eips.ethereum.org/EIPS/eip-1014
     * Skinny CREATE2: same as CREATE but with deterministic address
     */
    boolean eip1014();
}
