package com.liyizhu.house.biz.mapper;

import com.liyizhu.house.common.model.Blog;
import com.liyizhu.house.common.page.PageParams;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BlogMapper {

    List<Blog> selectBlog(@Param("blog")Blog query, @Param("pageParams") PageParams params);

    Long selectBlogCount(Blog query);
}
