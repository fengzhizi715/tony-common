/**
 * 
 */
package cn.salesuite.common.redis;


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
@ContextConfiguration(locations = {"classpath:applicationContext-resource.xml", "classpath:applicationContext-redis.xml"})
public class RedisTest extends AbstractJUnit4SpringContextTests {
	
	@Autowired
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
}
