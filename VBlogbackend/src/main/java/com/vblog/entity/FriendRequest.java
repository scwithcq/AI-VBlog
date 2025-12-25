package com.vblog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 好友申请实体
 * 
 * @author V-Blog团队
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_friend_request")
public class FriendRequest extends BaseEntity {

  /**
   * 申请ID
   */
  @TableId(type = IdType.AUTO)
  private Long requestId;

  /**
   * 发起者用户ID
   */
  private Long fromUserId;

  /**
   * 接收者用户ID
   */
  private Long toUserId;

  /**
   * 申请留言
   */
  private String message;

  /**
   * 申请状态：0-待处理，1-已同意，2-已拒绝
   */
  private Integer status;
}
