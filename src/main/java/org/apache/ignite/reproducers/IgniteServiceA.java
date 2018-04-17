package org.apache.ignite.reproducers;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteFuture;

/** */
public class IgniteServiceA {
    /** */
    private static final String CACHE_NAME = "cluster-futures";

    /** */
    public static void main(String[] args) {
        try (Ignite ignite = Ignition.start("default-config.xml")) {
            IgniteCompute compute = ignite.compute();

            IgniteFuture<String> future = compute.callAsync(new IgniteClusterFuture(CACHE_NAME, 1));

            System.out.println("Result = " + future.get());
        }
    }
}
