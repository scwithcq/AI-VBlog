package com.vblog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Post Like Entity
 * 
 * @author V-Blog Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostLike extends BaseEntity {

    /**
     * Like ID (primary key)
     */
    private Long likeId;

    /**
     * Post ID
     */
    private Long postId;

    /**
     * User ID (who liked the post)
     */
    private Long userId;
}
