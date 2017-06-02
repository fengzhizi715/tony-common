/**
 * 
 */
package com.safframework.tony.common.utils;

import com.alibaba.fastjson.JSON;


/**
 * @author Tony Shen
 *
 */
public class StringUtils {
    
	
	/**
	 * 将对象以json格式打印出来
	 * @param obj
	 * @return
	 */
	public static String printObject(Object obj) {
		return JSON.toJSONString(obj);
	}
}
