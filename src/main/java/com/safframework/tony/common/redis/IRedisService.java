/**
 * 
 */
package com.safframework.tony.common.redis;

/**
 * @author Tony Shen
 *
 */
public interface IRedisService<K, V> {
	
    /**
     * if expiredTime <=0 then never timeout
     * @param key
     * @param value
     * @param expiredTime
     */
    public void set(K key, V value, long expiredTime);
 
    public V get(K key);
 
    public void del(K key);
}
