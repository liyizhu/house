package com.liyizhu.house.biz.service;

import com.liyizhu.house.biz.mapper.CommentMapper;
import com.liyizhu.house.common.constants.CommonConstants;
import com.liyizhu.house.common.model.Comment;
import com.liyizhu.house.common.model.User;
import com.liyizhu.house.common.utils.BeanHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    CommentMapper commentMapper;

    @Autowired
    UserService userService;


    public List<Comment> getHouseComments(Long houseId, int size) {
        List<Comment> comments = commentMapper.selectComments(houseId,size);
        comments.forEach(comment -> {
            User user = userService.getUserById(comment.getUserId());
            comment.setAvatar(user.getAvatar());
            comment.setUserName(user.getName());
        });
        return comments;
    }

    public void addHouseComment(Long houseId, String content, Long userId) {
        addComment(houseId,null, content, userId,1);
    }

    private void addComment(Long houseId, Integer blogId, String content, Long userId, int type) {
        Comment comment = new Comment();
        if (type == 1) {
            comment.setHouseId(houseId);
        }else {
            comment.setBlogId(blogId);
        }
        comment.setContent(content);
        comment.setUserId(userId);
        comment.setType(type);
        BeanHelper.onInsert(comment);
        BeanHelper.setDefaultProp(comment, Comment.class);
        commentMapper.insert(comment);
    }


    public void addBlogComment(Integer blogId, String content, Long userId) {
        addComment(null,blogId, content, userId, CommonConstants.COMMENT_BLOG_TYPE);
    }

    public List<Comment> getBlogComments(long blogId, int size) {
        List<Comment> comments = commentMapper.selectBlogComments(blogId,size);
        comments.forEach(comment -> {
            User user = userService.getUserById(comment.getUserId());
            comment.setUserName(user.getName());
            comment.setAvatar(user.getAvatar());
        });
        return comments;
    }
}
