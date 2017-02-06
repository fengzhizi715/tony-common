/**
 * 
 */
package com.safframework.tony.common.cache;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.safframework.tony.common.config.Constant;

/**
 * 实现了一个缓存的模板
 * @author Tony Shen
 *
 */
public abstract class AbstractLoadCache <K, V> {

    /** 
     * 数据缓存的构建 
     */  
    LoadingCache<K, V> cache = CacheBuilder.newBuilder()  
            //设计缓存条目  
            .maximumSize(Constant.maximumSize)  
            //设计刷新时间  
            .refreshAfterWrite(Constant.refreshAfterWrite, TimeUnit.HOURS)
            .build(new CacheLoader<K, V>(){  
  
                @Override  
                public V load(K key) throws Exception {  
                    //执行缓存数据方法获取数据  
                    return getData(key);  
                }  
                  
            });  
      
    /** 
     * 抽象方法，执行缓存数据 
     * @param key 
     * @return 
     */  
    public abstract V getData(K key);  
  
  
    public LoadingCache<K, V> getCache() {  
        return cache;  
    }  
  
    public void setCache(LoadingCache<K, V> cache) {  
        this.cache = cache;  
    }
}
