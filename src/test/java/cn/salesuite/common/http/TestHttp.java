/**
 * 
 */
package cn.salesuite.common.http;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Tony Shen
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-resource.xml", "classpath:applicationContext-http.xml"})
public class TestHttp {

	@Autowired
	HttpClient httpClient;
	
	@Test
	public void testHttp() {

		String url = "http://www.baidu.com";
		System.out.println(httpClient.get(url));
	}
}
