package com.liyizhu.house.biz.service;

import com.google.common.collect.Lists;
import com.liyizhu.house.common.model.City;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityService {

    public List<City> getAllCitys() {
        City city = new City();
        city.setId(1);
        city.setCityCode("110000");
        city.setCityName("北京");
        return Lists.newArrayList(city);
    }
}
