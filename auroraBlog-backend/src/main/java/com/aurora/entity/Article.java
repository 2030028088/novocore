package com.aurora.entity;

import com.aurora.config.ListTypeHandler;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("articles")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String titleZh;

    private String titleEn;

    private String summary;

    private String summaryZh;

    private String summaryEn;

    private String content;

    private String contentZh;

    private String contentEn;

    private String category;

    @TableField(typeHandler = ListTypeHandler.class)
    private List<String> tags;

    private String author;

    private String coverImage;

    private Integer readTime;

    private Integer favorites;

    private Boolean published;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}