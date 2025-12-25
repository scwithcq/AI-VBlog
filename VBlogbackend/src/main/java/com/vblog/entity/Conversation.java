package com.vblog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 会话实体
 * 
 * @author V-Blog团队
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_conversation")
public class Conversation extends BaseEntity {

  /**
   * 会话ID
   */
  @TableId
  private String conversationId;

  /**
   * 用户1的ID（较小的ID）
   */
  private Long user1Id;

  /**
   * 用户2的ID（较大的ID）
   */
  private Long user2Id;

  /**
   * 最后一条消息ID
   */
  private Long lastMessageId;

  /**
   * 最后一条消息内容摘要
   */
  private String lastMessageContent;

  /**
   * 最后消息时间
   */
  private LocalDateTime lastMessageTime;

  /**
   * 用户1未读消息数
   */
  private Integer user1UnreadCount;

  /**
   * 用户2未读消息数
   */
  private Integer user2UnreadCount;
}
