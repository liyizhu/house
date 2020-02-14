package com.liyizhu.house.biz.mapper;

import com.liyizhu.house.common.model.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    //查询房产 id 为 houseId 的 size 条房产评论
    List<Comment> selectComments(@Param("houseId")Long houseId, @Param("size") int size);

    int insert(Comment comment);

    List<Comment> selectBlogComments(@Param("blogId")long blogId, @Param("size")int size);
}
