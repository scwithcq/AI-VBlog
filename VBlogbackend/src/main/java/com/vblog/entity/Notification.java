package com.vblog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知实体类
 * 
 * @author V-Blog团队
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_notification")
public class Notification extends BaseEntity {

    /**
     * 通知ID
     */
    @TableId(value = "notification_id", type = IdType.AUTO)
    private Long notificationId;

    /**
     * 接收者ID
     */
    private Long recipientId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 通知类型：1-关注 2-点赞 3-评论 4-系统通知
     */
    private Integer type;

    /**
     * 关联的帖子ID
     */
    private Long postId;

    /**
     * 关联的评论ID
     */
    private Long commentId;

    /**
     * 关联的被关注者ID（关注通知时使用）
     */
    private Long followeeId;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 是否已读：0-未读 1-已读
     */
    private Integer isRead;
}
