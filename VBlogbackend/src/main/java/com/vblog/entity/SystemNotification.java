package com.vblog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统互动通知实体类
 * 用于记录点赞、评论、@提及、关注等互动行为
 * 
 * @author V-Blog团队
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_system_notification")
public class SystemNotification extends BaseEntity {

  /**
   * 通知ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 接收者用户ID
   */
  private Long recipientId;

  /**
   * 发送者用户ID（触发通知的用户）
   */
  private Long senderId;

  /**
   * 发送者用户名（冗余字段，避免关联查询）
   */
  private String senderUsername;

  /**
   * 发送者头像（冗余字段）
   */
  private String senderAvatar;

  /**
   * 通知类型：LIKE(点赞)、COMMENT(评论)、MENTION(提及)、FOLLOW(关注)、REPOST(转发)
   */
  private String type;

  /**
   * 相关帖子ID
   */
  private Long postId;

  /**
   * 相关评论ID
   */
  private Long commentId;

  /**
   * 通知内容文本
   */
  private String content;

  /**
   * 是否已读：0-未读 1-已读
   */
  private Integer isRead;

  /**
   * 通知类型枚举
   */
  public enum NotificationType {
    LIKE("点赞"),
    COMMENT("评论"),
    MENTION("提及"),
    FOLLOW("关注"),
    REPOST("转发");

    private final String description;

    NotificationType(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }
}
