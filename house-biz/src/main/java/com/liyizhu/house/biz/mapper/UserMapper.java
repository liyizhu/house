package com.liyizhu.house.biz.mapper;

import com.liyizhu.house.common.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    List<User> selectUsers();

    int insert(User account);

    List<User> selectUserByQuery(User user);

    int delete(String email);

    int update(User updateUser);
}
