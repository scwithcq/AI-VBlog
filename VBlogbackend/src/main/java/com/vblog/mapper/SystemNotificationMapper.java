package com.vblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vblog.entity.SystemNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 系统互动通知 Mapper 接口
 * 
 * @author V-Blog团队
 */
@Mapper
public interface SystemNotificationMapper extends BaseMapper<SystemNotification> {

  /**
   * 根据ID查询
   *
   * @param id 通知ID
   * @return 通知对象
   */
  SystemNotification selectById(Long id);

  /**
   * 根据条件查询列表
   *
   * @param map 查询条件
   * @return 通知列表
   */
  List<SystemNotification> selectByMap(Map<String, Object> map);

  /**
   * 插入通知
   *
   * @param notification 通知对象
   * @return 影响行数
   */
  int insert(SystemNotification notification);

  /**
   * 更新通知
   *
   * @param notification 通知对象
   * @return 影响行数
   */
  int update(SystemNotification notification);

  /**
   * 根据ID删除
   *
   * @param id 通知ID
   * @return 影响行数
   */
  int deleteById(Long id);

  /**
   * 统计未读通知数
   *
   * @param recipientId 接收者ID
   * @return 未读通知数
   */
  int countUnread(@Param("recipientId") Long recipientId);

  /**
   * 批量标记为已读
   *
   * @param recipientId 接收者ID
   * @param type        通知类型（可选）
   * @return 影响行数
   */
  int markAsReadByRecipient(@Param("recipientId") Long recipientId, @Param("type") String type);

  /**
   * 批量标记指定ID列表为已读
   *
   * @param ids 通知ID列表
   * @return 影响行数
   */
  int markAsReadByIds(@Param("ids") List<Long> ids);

  /**
   * 删除指定天数之前的已读通知
   *
   * @param days 天数
   * @return 影响行数
   */
  int deleteReadNotificationsBeforeDays(@Param("days") int days);

  /**
   * 根据类型统计未读通知数
   *
   * @param recipientId 接收者ID
   * @param type        通知类型
   * @return 未读通知数
   */
  int countUnreadByType(@Param("recipientId") Long recipientId, @Param("type") String type);
}
