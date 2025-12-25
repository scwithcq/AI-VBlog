package com.vblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vblog.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 通知Mapper接口
 *
 * @author V-Blog Team
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 根据ID查询
     *
     * @param notificationId 通知ID
     * @return 通知对象
     */
    Notification selectById(Long notificationId);

    /**
     * 根据条件查询列表
     *
     * @param map 查询条件
     * @return 通知列表
     */
    List<Notification> selectByMap(Map<String, Object> map);

    /**
     * 插入通知
     *
     * @param notification 通知对象
     * @return 影响行数
     */
    int insert(Notification notification);

    /**
     * 更新通知
     *
     * @param notification 通知对象
     * @return 影响行数
     */
    int update(Notification notification);

    /**
     * 根据ID删除
     *
     * @param notificationId 通知ID
     * @return 影响行数
     */
    int deleteById(Long notificationId);

    /**
     * 更新用户的所有未读通知为已读
     *
     * @param recipientId 接收者ID
     * @return 影响行数
     */
    int markAllAsRead(Long recipientId);

    // 获取未读通知数 - 返回 Long 类型
    Long getUnreadCount(@Param("userId") Long userId);

}
