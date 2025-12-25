package com.vblog.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Base Entity Class
 * Common fields for all entities
 * 
 * @author V-Blog Team
 */
@Data
public abstract class BaseEntity {

    /**
     * Create time
     */
    private LocalDateTime createTime;

    /**
     * Update time
     */
    private LocalDateTime updateTime;
}

