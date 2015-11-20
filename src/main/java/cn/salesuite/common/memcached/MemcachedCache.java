/**
 * 
 */
package cn.salesuite.common.memcached;

import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.util.StringUtils;

import com.google.common.base.Joiner;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

/**
 * 自定义spring的cache的实现，参考cache包实现
 * @author Tony Shen
 *
 */
public class MemcachedCache implements Cache {

	private static final Logger logger = LoggerFactory.getLogger(MemcachedCache.class);
	
	/**
     * 缓存的别名
     */
    private String name;
    
    /**
     * memcached客户端
     */
    private MemcachedClient client;
    
    /**
     * 缓存过期时间，默认是1小时
     * 自定义的属性
     */
    private int exp = 3600;

    /**
     * 前缀名
     */
    private String prefix;
    
    private static final String EMPTY_SEPARATOR = "";
    
	public String getName() {
		return name;
	}
	
	public Object getNativeCache() {
		return this.client;
	}
	
	public ValueWrapper get(Object key) {
		Object object = null;
        try {
            object = this.client.get(handleKey(objectToString(key)));
        } catch (TimeoutException e) {
        	logger.error(e.getMessage(), e);
        } catch (InterruptedException e) {
        	logger.error(e.getMessage(), e);
        } catch (MemcachedException e) {
        	logger.error(e.getMessage(), e);
        }

        return (object != null ? new SimpleValueWrapper(object) : null);
	}

	public <T> T get(Object key, Class<T> type) {
		try {
			Object object = this.client.get(handleKey(objectToString(key)));
			return (T) object;
		} catch (TimeoutException e) {
			logger.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		} catch (MemcachedException e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
	
	public void put(Object key, Object value) {
		if (value == null) {
			// this.evict(key);
			return;
		}

		try {
			this.client.set(handleKey(objectToString(key)), exp, value);
		} catch (TimeoutException e) {
			logger.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		} catch (MemcachedException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public ValueWrapper putIfAbsent(Object key, Object value) {
		this.put(key, value);
		return this.get(key);
	}

	public void evict(Object key) {
		try {
			this.client.delete(handleKey(objectToString(key)));
		} catch (TimeoutException e) {
			logger.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		} catch (MemcachedException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void clear() {
		try {
			this.client.flushAll();
		} catch (TimeoutException e) {
			logger.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		} catch (MemcachedException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void setName(String name) {
        this.name = name;
    }

    public MemcachedClient getClient() {
        return client;
    }

    public void setClient(MemcachedClient client) {
        this.client = client;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
	
	/**
     * 处理key
     * @param key
     * @return
     */
    private String handleKey(String key) {
        return Joiner.on(EMPTY_SEPARATOR).skipNulls().join(this.prefix, key);
    }
	
	/**
     * 转换key，去掉空格
     * @param object
     * @return
     */
    private String objectToString(Object object) {
        if (object == null) 
            return null;
        
        if (object instanceof String) {
            return StringUtils.replace((String) object, " ", "_");
        } else {
            return object.toString();
        }
    }
}
