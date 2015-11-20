/**
 * 
 */
package cn.salesuite.common.memcached;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.google.common.base.Optional;

/**
 * @author Tony Shen
 *
 */
public class CityService {

	private final Logger logger = LoggerFactory.getLogger(CityService.class);

	@Cacheable(value = "city")
	public City getCityByid(int cityId) {

		// 方法内部实现不考虑缓存逻辑，直接实现业务
		logger.info("real querying city... {}", cityId);
		Optional<City> cityOptional = getFromDB(cityId);
		if (!cityOptional.isPresent()) {
			throw new IllegalStateException(String.format("can not find city by city : [%d]", cityId));
		}

		return cityOptional.get();
	}
	
    private Optional<City> getFromDB(int cityId) {
        logger.info("real querying db... {}", cityId);
        //Todo query data from database
        return Optional.fromNullable(new City(cityId));
    }
    
    @CacheEvict(value="city",allEntries=true)
    public void reload() {
    }
}
