/**
 * 
 */
package cn.salesuite.common.memcached;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.KeyIterator;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.utils.AddrUtil;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.salesuite.common.utils.StringUtils;

/**
 * @author Tony Shen
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-resource.xml", "classpath:applicationContext-memcached.xml"})
public class TestXMemcached extends AbstractJUnit4SpringContextTests {
	
	@Autowired
	XMemcachedManager memcachedManager;
	
	private static int ONE_DAY = 86400; 

	@Test
	public void testSet() {
		
		City city = new City();
		city.cityId = 3100;
		city.cityName = "上海";
		
		try {
			memcachedManager.set("city",ONE_DAY, city);
			City other = (City) memcachedManager.get("city");
			if (other==null) {
				logger.info("other is null..............");
			}
			Assert.assertEquals(city.cityName, other.cityName);
			Assert.assertEquals(city.cityId, other.cityId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testKey() {
		MemcachedClient client = memcachedManager.getMemcachedClient();
		KeyIterator it;
		try {
			it = client.getKeyIterator(AddrUtil.getOneAddress("121.199.39.115:11211"));
			while(it.hasNext())
			{
			   String key=it.next();
			   System.out.println("key="+key);
			   
			   System.out.println(StringUtils.printObject(memcachedManager.get(key)));
			}
		} catch (MemcachedException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
