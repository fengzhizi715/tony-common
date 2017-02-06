/**
 * 
 */
package com.safframework.tony.common.memcached;

import java.util.Collection;

import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;

/**
 * @author Tony Shen
 *
 */
public class MemcachedCacheManager extends AbstractCacheManager {

	private Collection<Cache> caches;

    @Override
    protected Collection<? extends Cache> loadCaches() {
        return this.caches;
    }

    public void setCaches(Collection<Cache> caches) {
        this.caches = caches;
    }

    public Cache getCache(String name) {
        return super.getCache(name);
    }

}
