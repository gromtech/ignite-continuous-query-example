package org.apache.ignite.reproducers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.cache.Cache;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.cache.query.ContinuousQuery;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.resources.IgniteInstanceResource;

public class IgniteClusterFuture implements IgniteCallable<String> {
    /** */
    private final Integer key;

    /** */
    private final String cacheName;

    @IgniteInstanceResource
    private Ignite ignite;

    public IgniteClusterFuture(String cacheName, Integer key) {
        this.key = key;
        this.cacheName = cacheName;
    }

    @Override public String call() throws Exception {
        if (ignite != null) {
            IgniteCache<Integer, String> cache = ignite.cache(cacheName);

            if (cache != null) {
                // Creating a continuous query.
                ContinuousQuery<Integer, String> qry = new ContinuousQuery<>();

                qry.setInitialQuery(new ScanQuery<>((k, v) -> k == key));

                final CountDownLatch waitForValueChanged = new CountDownLatch(1);
                final AtomicReference<String> result = new AtomicReference<>();

                CacheEntryUpdatedListener<Integer, String> listener = new CacheEntryUpdatedListener<Integer, String>() {
                    @Override public void onUpdated(
                        Iterable<CacheEntryEvent<? extends Integer, ? extends String>> iterable) throws CacheEntryListenerException {
                        for (CacheEntryEvent<? extends Integer, ? extends String> entry: iterable) {
                            result.set(entry.getValue());
                        }

                        waitForValueChanged.countDown();
                    }
                };

                qry.setLocalListener(listener);

                try (QueryCursor<Cache.Entry<Integer, String>> cur = cache.query(qry);) {
                    waitForValueChanged.await(60000, TimeUnit.MILLISECONDS);
                }

                return result.get();
            }
            else
                throw new IgniteException("Cache instance is null.");
        }
        else
            throw new IgniteException("Ignite instance is null.");
    }
}
