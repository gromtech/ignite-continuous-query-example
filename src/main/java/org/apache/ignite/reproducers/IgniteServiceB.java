package org.apache.ignite.reproducers;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;

/** */
public class IgniteServiceB {
    /** */
    private static final String CACHE_NAME = "cluster-futures";

    /** */
    public static void main(String[] args) {
        try (Ignite ignite = Ignition.start("default-config.xml")) {
            IgniteCache<Integer, String> cache = ignite.cache(CACHE_NAME);

            if (cache != null)
                cache.put(1, "one");
        }
    }
}
