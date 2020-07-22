package org.hitchain.exporer.sync;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockHeaderWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface SyncQueueIfc {

    /**
     * Wanted headers
     */
    interface HeadersRequest {

        long getStart();

        byte[] getHash();

        int getCount();

        boolean isReverse();

        List<HeadersRequest> split(int maxCount);

        int getStep();
    }

    /**
     * Wanted blocks
     */
    interface BlocksRequest {
        List<BlocksRequest> split(int count);

        List<BlockHeaderWrapper> getBlockHeaders();
    }

    /**
     * Handles result of {@link #addHeadersAndValidate(Collection)} invocation.
     *
     * <p>
     *     If {@code valid} is true then validation passed successfully
     *     and {@code headers} list contains the same result as if {@link #addHeaders(Collection)} was called.
     *     Otherwise, the list contains invalid headers.
     */
    class ValidatedHeaders {
        public static final ValidatedHeaders Empty = new ValidatedHeaders(Collections.emptyList(), true);

        private final List<BlockHeaderWrapper> headers;
        private final boolean valid;
        private final String reason;

        public ValidatedHeaders(List<BlockHeaderWrapper> headers, boolean valid, String reason) {
            this.headers = headers;
            this.valid = valid;
            this.reason = reason;
        }

        public ValidatedHeaders(List<BlockHeaderWrapper> headers, boolean valid) {
            this(headers, valid, "");
        }

        public boolean isValid() {
            return valid;
        }

        @Nonnull
        public List<BlockHeaderWrapper> getHeaders() {
            return headers;
        }

        public String getReason() {
            return reason;
        }

        @Nullable
        public byte[] getNodeId() {
            if (headers.isEmpty()) return null;
            return headers.get(0).getNodeId();
        }

        @Nullable
        public BlockHeader getHeader() {
            if (headers.isEmpty()) return null;
            return headers.get(0).getHeader();
        }
    }

    /**
     * Returns wanted headers requests
     * @param maxSize Maximum number of headers in a singles request
     * @param maxRequests Maximum number of requests
     * @param maxTotalHeaders The total maximum of cached headers in the implementation
     * @return null if the end of headers reached (e.g. when download is limited with a block number)
     *   empty list if no headers for now (e.g. max allowed number of cached headers reached)
     */
    List<HeadersRequest> requestHeaders(int maxSize, int maxRequests, int maxTotalHeaders);

    /**
     * Adds received headers.
     * Headers themselves need to be verified (except parent hash)
     * The list can be in any order and shouldn't correspond to prior headers request
     * @return If this is 'header-only' SyncQueue then the next chain of headers
     * is popped from SyncQueue and returned
     * The reverse implementation should return headers in revers order (N, N-1, ...)
     * If this instance is for headers+blocks downloading then null returned
     */
    List<BlockHeaderWrapper> addHeaders(Collection<BlockHeaderWrapper> headers);

    /**
     * In general, does the same work as {@link #addHeaders(Collection)} does.
     * But before trimming, the longest chain is checked with parent header validator.
     * If validation is failed, the chain is erased from the queue.
     *
     * <p>
     *     <b>Note:</b> in reverse queue falls to {@link #addHeaders(Collection)} invocation
     *
     * @return check {@link ValidatedHeaders} for details
     */
    ValidatedHeaders addHeadersAndValidate(Collection<BlockHeaderWrapper> headers);

    /**
     * Returns wanted blocks hashes
     */
    BlocksRequest requestBlocks(int maxSize);

    /**
     * Adds new received blocks to the queue
     * The blocks need to be verified but can be passed in any order and need not correspond
     * to prior returned block request
     * @return  blocks ready to be imported in the valid import order.
     */
    List<Block> addBlocks(Collection<Block> blocks);

    /**
     * Returns approximate header count waiting for their blocks
     */
    int getHeadersCount();
}
