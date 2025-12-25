package com.vblog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Post Entity
 * 
 * @author V-Blog Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Post extends BaseEntity {

    /**
     * Post ID (primary key)
     */
    private Long postId;

    /**
     * User ID (author)
     */
    private Long userId;

    /**
     * Post content
     */
    private String content;

    /**
     * Images JSON array (e.g., ["url1", "url2"])
     */
    private String images;

    /**
     * Video URL
     */
    private String videoUrl;

    /**
     * Source post ID (for repost)
     */
    private Long sourceId;
}

