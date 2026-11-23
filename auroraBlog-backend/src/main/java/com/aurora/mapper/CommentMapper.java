package com.aurora.mapper;

import com.aurora.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    List<Comment> selectByArticleId(@Param("articleId") Long articleId);

    List<Comment> selectByParentId(@Param("parentId") Long parentId);

    int countByArticleId(@Param("articleId") Long articleId);

    int countByParentId(@Param("parentId") Long parentId);
}