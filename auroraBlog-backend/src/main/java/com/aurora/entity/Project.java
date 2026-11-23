package com.aurora.entity;

import com.aurora.config.ListTypeHandler;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("projects")
public class Project {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String titleZh;
    
    private String titleEn;
    
    private String category;
    
    @TableField(typeHandler = ListTypeHandler.class)
    private List<String> tags;
    
    private String year;
    
    private String image;
    
    private String detailImage;
    
    @TableField(typeHandler = ListTypeHandler.class)
    private List<String> detailImages;
    
    private String description;
    
    private String descriptionZh;
    
    private String descriptionEn;
    
    private String liveUrl;
    
    private String githubUrl;
    
    private String role;
    
    private String client;
    
    private Boolean published;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
