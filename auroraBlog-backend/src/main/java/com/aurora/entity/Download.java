package com.aurora.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("downloads")
public class Download {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;

    private String originalName;

    private String description;

    private Long fileSize;

    private String fileType;

    private String filePath;

    private Boolean published;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}