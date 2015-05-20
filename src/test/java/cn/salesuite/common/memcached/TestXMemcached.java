/**
 * 
 */
package cn.salesuite.common.memcached;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
