/**
 * 
 */
package cn.salesuite.common.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author Tony Shen
 * 
 */
public class AbstractRedisService<K, V> implements IRedisService<K, V> {

	private RedisTemplate<K, V> redisTemplate;

	public void set(final K key, final V value, final long expiredTime) {
		BoundValueOperations<K, V> valueOper = redisTemplate.boundValueOps(key);
		if (expiredTime <= 0) {
			valueOper.set(value);
		} else {
			valueOper.set(value, expiredTime, TimeUnit.MILLISECONDS);
		}
	}

	public V get(final K key) {
		BoundValueOperations<K, V> valueOper = redisTemplate.boundValueOps(key);
		return valueOper.get();
	}

	public void del(K key) {
		if (redisTemplate.hasKey(key)) {
			redisTemplate.delete(key);
		}
	}

	public RedisTemplate<K, V> getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(RedisTemplate<K, V> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
}
