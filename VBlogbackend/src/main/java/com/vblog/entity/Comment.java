package com.vblog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Comment Entity
 * 
 * @author V-Blog Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Comment extends BaseEntity {

    /**
     * Comment ID (primary key)
     */
    private Long commentId;

    /**
     * Post ID
     */
    private Long postId;

    /**
     * User ID (commenter)
     */
    private Long userId;

    /**
     * Parent comment ID (for reply, null for top-level comment)
     */
    private Long parentId;

    /**
     * Comment content
     */
    private String content;
    
    /**
     * Comment like count
     */
    private Long likeCount;
}


