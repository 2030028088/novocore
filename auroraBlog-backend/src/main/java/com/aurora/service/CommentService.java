package com.aurora.service;

import com.aurora.entity.Comment;
import com.aurora.exception.ResourceNotFoundException;
import com.aurora.mapper.CommentMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CacheService cacheService;

    public Comment createComment(Long articleId, Long userId, String username, String content, Long parentId) {
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setLikes(0);
        
        commentMapper.insert(comment);
        
        cacheService.deleteArticleCache(articleId);
        
        return comment;
    }

    public List<Comment> getCommentsByArticle(Long articleId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleId);
        wrapper.orderByAsc("created_at");
        return commentMapper.selectList(wrapper);
    }

    public List<Map<String, Object>> getCommentsWithReplies(Long articleId) {
        List<Comment> allComments = getCommentsByArticle(articleId);
        
        Map<Long, Comment> commentMap = new HashMap<>();
        List<Comment> topLevelComments = new ArrayList<>();
        
        for (Comment comment : allComments) {
            commentMap.put(comment.getId(), comment);
            if (comment.getParentId() == null || comment.getParentId() == 0) {
                topLevelComments.add(comment);
            }
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Comment topComment : topLevelComments) {
            result.add(buildCommentWithReplies(topComment, commentMap));
        }
        
        return result;
    }

    private Map<String, Object> buildCommentWithReplies(Comment comment, Map<Long, Comment> commentMap) {
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("id", comment.getId());
        commentData.put("articleId", comment.getArticleId());
        commentData.put("userId", comment.getUserId());
        commentData.put("username", comment.getUsername());
        commentData.put("content", comment.getContent());
        commentData.put("parentId", comment.getParentId());
        commentData.put("likes", comment.getLikes());
        commentData.put("createdAt", comment.getCreatedAt());
        
        List<Map<String, Object>> replies = new ArrayList<>();
        for (Comment child : commentMap.values()) {
            if (child.getParentId() != null && child.getParentId().equals(comment.getId())) {
                replies.add(buildCommentWithReplies(child, commentMap));
            }
        }
        commentData.put("replies", replies);
        
        return commentData;
    }

    public Comment likeComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new ResourceNotFoundException("Comment", commentId);
        }
        
        comment.setLikes(comment.getLikes() + 1);
        commentMapper.updateById(comment);
        
        cacheService.deleteArticleCache(comment.getArticleId());
        
        return comment;
    }

    public Comment getCommentById(Long commentId) {
        return commentMapper.selectById(commentId);
    }

    public boolean deleteComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new ResourceNotFoundException("Comment", commentId);
        }
        
        Long articleId = comment.getArticleId();
        
        QueryWrapper<Comment> childWrapper = new QueryWrapper<>();
        childWrapper.eq("parent_id", commentId);
        commentMapper.delete(childWrapper);
        
        boolean deleted = commentMapper.deleteById(commentId) > 0;
        
        if (deleted) {
            cacheService.deleteArticleCache(articleId);
        }
        
        return deleted;
    }

    public int countComments(Long articleId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleId);
        return commentMapper.selectCount(wrapper).intValue();
    }
}