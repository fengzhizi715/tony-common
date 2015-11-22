/**
 * 
 */
package cn.salesuite.common.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.salesuite.common.memcached.CityService;

/**
 * @author Tony Shen
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-resource.xml","classpath:applicationContext-cacheable-redis.xml"})
public class TestCityService extends AbstractJUnit4SpringContextTests {

	@Autowired
	CityService cityService;
	
	@Test
	public void testGetCityById() {
        cityService.reload();
        
        logger.info("first query...");
        cityService.getCityByid(3100);

        logger.info("second query...");
        cityService.getCityByid(3100);
        
        cityService.reload();
        
        logger.info("third query...");
        cityService.getCityByid(3100);
        
        logger.info("forth query...");
        cityService.getCityByid(3100);
        
        logger.info("firth query...");
        cityService.getCityByid(1100);
        
        logger.info("sixth query...");
        cityService.getCityByid(1100);
	}
}