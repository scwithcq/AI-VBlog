package com.vblog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息实体
 * 
 * @author V-Blog团队
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_message")
public class Message extends BaseEntity {

  /**
   * 消息ID
   */
  @TableId(type = IdType.AUTO)
  private Long messageId;

  /**
   * 会话ID
   */
  private String conversationId;

  /**
   * 发送者用户ID
   */
  private Long fromUserId;

  /**
   * 接收者用户ID
   */
  private Long toUserId;

  /**
   * 消息内容
   */
  private String content;

  /**
   * 消息类型：1-文本，2-图片，3-语音，4-视频，5-文件
   */
  private Integer messageType;

  /**
   * 消息状态：0-未读，1-已读，2-已撤回
   */
  private Integer status;

  /**
   * 发送者是否删除：0-否，1-是
   */
  private Integer isDeletedBySender;

  /**
   * 接收者是否删除：0-否，1-是
   */
  private Integer isDeletedByReceiver;
}
