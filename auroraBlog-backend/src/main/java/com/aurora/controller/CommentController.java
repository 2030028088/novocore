package com.aurora.controller;

import com.aurora.entity.Comment;
import com.aurora.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestBody Map<String, Object> body) {
        Long articleId = ((Number) body.get("articleId")).longValue();
        Long userId = body.get("userId") != null ? ((Number) body.get("userId")).longValue() : null;
        String username = (String) body.get("username");
        String content = (String) body.get("content");
        Long parentId = body.get("parentId") != null ? ((Number) body.get("parentId")).longValue() : null;
        
        Comment comment = commentService.createComment(articleId, userId, username, content, parentId);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<List<Map<String, Object>>> getCommentsByArticle(@PathVariable Long articleId) {
        List<Map<String, Object>> comments = commentService.getCommentsWithReplies(articleId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<Comment> likeComment(@PathVariable Long commentId) {
        Comment comment = commentService.likeComment(commentId);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, Boolean>> deleteComment(@PathVariable Long commentId) {
        boolean deleted = commentService.deleteComment(commentId);
        return ResponseEntity.ok(Map.of("success", deleted));
    }
}