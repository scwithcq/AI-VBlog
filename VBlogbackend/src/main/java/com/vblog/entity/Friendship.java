package com.vblog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 好友关系实体
 * 
 * @author V-Blog团队
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_friendship")
public class Friendship extends BaseEntity {

  /**
   * 好友关系ID
   */
  @TableId(type = IdType.AUTO)
  private Long friendshipId;

  /**
   * 用户ID
   */
  private Long userId;

  /**
   * 好友ID
   */
  private Long friendId;

  /**
   * 好友状态：1-正常，2-已删除
   */
  private Integer status;

  /**
   * 好友备注
   */
  private String remark;
}
