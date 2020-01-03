/*******************************************************************************
 * Copyright (c) 2019-11-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.net.dht;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * Bucket
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-11-08
 */
@Data
@Accessors(fluent = true)
public class Bucket {

    public static int MAX_KADEMLIA_K = 5;
    // if bit = 1 go left
    Bucket left;
    // if bit = 0 go right
    Bucket right;

    String name;

    List<Peer> peers = new ArrayList<>();

    public Bucket(String name) {
        this.name = name;
    }


    public void add(Peer peer) {

        if (peer == null) {
            throw new Error("Not a leaf");
        }

        if (peers == null) {
            if (peer.nextBit(name) == 1) {
                left.add(peer);
            } else {
                right.add(peer);
            }
            return;
        }

        peers.add(peer);

        if (peers.size() > MAX_KADEMLIA_K) {
            splitBucket();
        }
    }

    public void splitBucket() {
        left = new Bucket(name + "1");
        right = new Bucket(name + "0");

        for (Peer id : peers) {
            if (id.nextBit(name) == 1) {
                left.add(id);
            } else {
                right.add(id);
            }
        }

        this.peers = null;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(name).append("\n");

        if (peers == null) {
            return sb.toString();
        }

        for (Peer id : peers) {
            sb.append(id.toBinaryString()).append("\n");
        }

        return sb.toString();
    }


    public void traverseTree(Bucket.DoOnTree doOnTree) {

        if (left != null) {
            left.traverseTree(doOnTree);
        }
        if (right != null) {
            right.traverseTree(doOnTree);
        }

        doOnTree.call(this);
    }

    public interface DoOnTree {

        void call(Bucket bucket);
    }

    @Data
    @Accessors(fluent = true)
    public static class SaveLeaf implements DoOnTree {

        List<Bucket> leafs = new ArrayList<>();

        @Override
        public void call(Bucket bucket) {
            if (bucket.peers != null) {
                leafs.add(bucket);
            }
        }
    }
}
