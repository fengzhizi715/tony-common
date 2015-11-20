/**
 * 
 */
package cn.salesuite.common.memcached;

import java.io.Serializable;

/**
 * @author Tony
 *
 */
public class City implements Serializable {

	private static final long serialVersionUID = 8572418102162489136L;
	
	public int cityId;
	public String cityName;
	
	public City() {
	}
	
	public City(int cityId) {
		this.cityId = cityId;
	}
	
	public City(int cityId,String cityName) {
		this.cityId = cityId;
		this.cityName = cityName;
	}
	
	public int getCityId() {
		return cityId;
	}
	
	public void setCityId(int cityId) {
		this.cityId = cityId;
	}
	
	public String getCityName() {
		return cityName;
	}
	
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
}
