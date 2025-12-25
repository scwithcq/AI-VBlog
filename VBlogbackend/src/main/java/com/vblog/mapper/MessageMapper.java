package com.vblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vblog.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息Mapper接口
 * 
 * @author V-Blog团队
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

  /**
   * 分页查询会话消息（按时间倒序）
   */
  Page<Message> selectConversationMessages(
      Page<Message> page,
      @Param("conversationId") String conversationId,
      @Param("userId") Long userId);

  /**
   * 批量标记会话消息为已读
   */
  int batchUpdateMessageStatus(
      @Param("conversationId") String conversationId,
      @Param("userId") Long userId,
      @Param("status") Integer status);

  /**
   * 获取用户总未读消息数
   */
  Integer selectUnreadCount(@Param("userId") Long userId);

  /**
   * 获取会话未读消息数
   */
  Integer selectConversationUnreadCount(
      @Param("conversationId") String conversationId,
      @Param("userId") Long userId);
}
