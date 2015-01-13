/**
 * 
 */
package cn.salesuite.common.redis;


import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Tony Shen
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-resource.xml", "classpath:applicationContext-redis.xml"})
public class TestRedis extends AbstractJUnit4SpringContextTests {
	
	@Resource
	UserRedisService userRedisService;
	
	private static long ONE_DAY = 86400000L; 

	@Test
	public void testSaveUser() {

		User u = new User();
		u.setUserName("tony");
		u.setPassword("tony");
		
		userRedisService.set("user", u, ONE_DAY);
		Assert.assertEquals(u, userRedisService.get("user"));
	}
	
	@Test
	public void testDeleteUser() {
		
		userRedisService.del("user");
		Assert.assertNull(userRedisService.get("user"));
	}
}
