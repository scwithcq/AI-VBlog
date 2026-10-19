package com.vblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vblog.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话Mapper接口
 *
 * @author V-Blog团队
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

  /**
   * 查询用户的所有会话（按最后消息时间倒序）
   */
  List<Conversation> selectUserConversations(@Param("userId") Long userId);

  /**
   * 更新会话未读数
   */
  int updateUnreadCount(
      @Param("conversationId") String conversationId,
      @Param("userId") Long userId,
      @Param("increment") Integer increment);

  /**
   * 清零会话未读数
   */
  int clearUnreadCount(
      @Param("conversationId") String conversationId,
      @Param("userId") Long userId);

  int setUnreadCount(
      @Param("conversationId") String conversationId,
      @Param("userId") Long userId,
      @Param("count") Integer count);
}
