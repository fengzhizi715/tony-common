/**
 * 
 */
package cn.salesuite.common.config;

/**
 * @author Tony Shen
 *
 */
public class Constant {

	// guava cache配置
    public static int maximumSize = 10;                                             // 配置缓存条目的大小  
    public static int refreshAfterWrite = 12;                                       // 配置数据加载到缓存后的刷新时间  
    public static int expireAfterWrite = 12;                                        // 配置数据加载到缓存后的移除时间 
}
