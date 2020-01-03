/*******************************************************************************
 * Copyright (c) 2019-11-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.net.dht;

import java.util.List;

/**
 * DHTUtils
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-11-08
 */
public class DHTUtils {
    public static void printAllLeafs(Bucket root) {
        Bucket.SaveLeaf saveLeaf = new Bucket.SaveLeaf();
        root.traverseTree(saveLeaf);

        for (Bucket bucket : saveLeaf.leafs()) {
            System.out.println(bucket);
        }
    }

    public static List<Bucket> getAllLeafs(Bucket root) {
        Bucket.SaveLeaf saveLeaf = new Bucket.SaveLeaf();
        root.traverseTree(saveLeaf);

        return saveLeaf.leafs();
    }
}
