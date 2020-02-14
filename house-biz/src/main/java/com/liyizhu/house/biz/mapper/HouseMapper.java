package com.liyizhu.house.biz.mapper;

import com.liyizhu.house.common.model.Community;
import com.liyizhu.house.common.model.House;
import com.liyizhu.house.common.model.HouseUser;
import com.liyizhu.house.common.model.UserMsg;
import com.liyizhu.house.common.page.PageParams;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HouseMapper {

    List<Community> selectCommunity(Community community);

    Long selectPageCount(@Param("house") House query);

    List<House> selectPageHouses(@Param("house") House house,@Param("pageParams") PageParams pageParams);

    int insert(House house);

    HouseUser selectHouseUser(@Param("userId") Long userId, @Param("houseId") Long houseId, @Param("type") Integer type);

    int insertHouseUser(HouseUser houseUser);

    HouseUser selectSaleHouseUser(@Param("id") Long houseId);

    int insertUserMsg(UserMsg userMsg);

    int updateHouse(House updateHouse);

    int downHouse(Long id);

    int deleteHouseUser(@Param("id") Long houseId, @Param("userId") Long userId, @Param("type") Integer value);
}
