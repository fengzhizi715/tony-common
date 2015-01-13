/**
 * 
 */
package cn.salesuite.common.mongodb;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.DBObject;

/**
 * @author Tony Shen
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-resource.xml", "classpath:applicationContext-mongodb.xml"})
public class TestMongoDB {
	
	@Autowired
	MongoDBManager mongoDBManager;

	@Test
	public void testInsertCollection() {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("userName", "tony");
		map.put("password", "000000");
		mongoDBManager.insert("user", map);
		
		DBObject dBObject = mongoDBManager.findOne("user", map);
		Assert.assertEquals(dBObject.get("userName"), map.get("userName"));
	}
}
