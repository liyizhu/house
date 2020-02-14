package com.liyizhu.house.biz.mapper;

import com.liyizhu.house.common.model.Agency;
import com.liyizhu.house.common.model.User;
import com.liyizhu.house.common.page.PageParams;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgencyMapper {

    List<User> selectAgent(@Param("user") User user, @Param("pageParams") PageParams pageParams);

    List<Agency> select(Agency agency);

    Long selectAgentCount(@Param("user") User user);

    int insert(Agency agency);
}
