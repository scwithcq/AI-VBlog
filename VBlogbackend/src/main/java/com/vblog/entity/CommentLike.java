package com.vblog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Comment Like Entity
 * 
 * @author V-Blog Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentLike extends BaseEntity {

  /**
   * Like ID (primary key)
   */
  private Long likeId;

  /**
   * Comment ID
   */
  private Long commentId;

  /**
   * User ID (who liked the comment)
   */
  private Long userId;
}
