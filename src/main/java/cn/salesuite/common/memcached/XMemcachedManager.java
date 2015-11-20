/**
 * 
 */
package cn.salesuite.common.memcached;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.salesuite.common.utils.StringUtils;

/**
 * XMemcached管理类
 * @author Tony Shen
 *
 */
public class XMemcachedManager {

	private Logger logger = LoggerFactory.getLogger(XMemcachedManager.class);
	private static final String SEPARATOR = "-";

	private MemcachedClient memcachedClient;
	private String channel;

	/**
     * 初始化方法
     */
	public void init() {
	}
	
	public Object get(String key) throws IOException {
		
		if (StringUtils.isBlank(key)) {
			logger.debug("访问memcache的key不可为空");
			return null;
		}

		String realKey = composeKey(key);
		Object value = null;
		try {
			value = memcachedClient.get(realKey);
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}

		logger.debug("get value from memcache key:{}", realKey);
		if (value == null) {
			return null;
		}
		return value;
	}
	
	public void set(String key, Object value) throws IOException {
		set(key, 0, value);
	}
	
	/**
	 * 
	 * @param key
	 * @param expre 单位 秒
	 * @param value
	 * @throws IOException
	 */
	public void set(String key, int expre, Object value) throws IOException {		
		if (StringUtils.isBlank(key)) {
			logger.debug("设置memcache的key不可为空");
			return;
		}
		
		if (expre<0) {
			logger.debug("expre time must >= 0");
			return;
		}

		String realKey = composeKey(key);
		//logger.debug("begin to set value to memcache key:{} value:{}", realKey, value);
		logger.debug("begin to set value to memcache key:{}", realKey);
		
		try {
			memcachedClient.set(realKey, expre, value);
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
	}
	
	public void delete(String key) {
		if (StringUtils.isBlank(key)) {
			logger.debug("删除memcache的key不可为空");			
			return;
		}

		String realKey = composeKey(key);
		logger.debug("begin to delete value from memcache key:{}", realKey);
		try {
			memcachedClient.delete(realKey);
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
	}
	
	protected String composeKey(String key) {
		String result = this.channel + SEPARATOR + key;
		return result;
	}

	public MemcachedClient getMemcachedClient() {
		return memcachedClient;
	}

	public void setMemcachedClient(MemcachedClient memcachedClient) {
		this.memcachedClient = memcachedClient;
	}
	
    public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
}
