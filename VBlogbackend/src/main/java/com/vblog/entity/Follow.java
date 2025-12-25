package com.vblog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Follow Entity
 * 
 * @author V-Blog Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Follow extends BaseEntity {

    /**
     * Follow ID (primary key)
     */
    private Long followId;

    /**
     * Follower ID (who is following)
     */
    private Long followerId;

    /**
     * Followee ID (who is being followed)
     */
    private Long followeeId;
}


