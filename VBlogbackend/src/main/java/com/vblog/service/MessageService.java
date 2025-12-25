package com.vblog.service;

import com.vblog.common.Page;
import com.vblog.entity.Conversation;
import com.vblog.entity.Message;

import java.util.List;

/**
 * 消息服务接口
 * 
 * @author V-Blog团队
 */
public interface MessageService {

  /**
   * 发送消息
   * 
   * @param fromUserId  发送者用户ID
   * @param toUserId    接收者用户ID
   * @param content     消息内容
   * @param messageType 消息类型
   * @return 消息记录
   */
  Message sendMessage(Long fromUserId, Long toUserId, String content, Integer messageType);

  /**
   * 获取会话消息列表（分页）
   * 
   * @param conversationId 会话ID
   * @param userId         当前用户ID
   * @param page           页码
   * @param size           每页大小
   * @return 消息分页列表
   */
  Page<Message> getConversationMessages(String conversationId, Long userId, int page, int size);

  /**
   * 获取会话列表
   * 
   * @param userId 用户ID
   * @return 会话列表
   */
  List<Conversation> getConversations(Long userId);

  /**
   * 标记消息为已读
   * 
   * @param messageId 消息ID
   * @param userId    用户ID
   * @return 操作结果
   */
  boolean markMessageAsRead(Long messageId, Long userId);

  /**
   * 批量标记会话消息为已读
   * 
   * @param conversationId 会话ID
   * @param userId         用户ID
   * @return 操作结果
   */
  boolean markConversationAsRead(String conversationId, Long userId);

  /**
   * 撤回消息
   * 
   * @param messageId 消息ID
   * @param userId    用户ID
   * @return 操作结果
   */
  boolean recallMessage(Long messageId, Long userId);

  /**
   * 删除消息（单方删除）
   * 
   * @param messageId 消息ID
   * @param userId    用户ID
   * @return 操作结果
   */
  boolean deleteMessage(Long messageId, Long userId);

  /**
   * 获取未读消息总数
   * 
   * @param userId 用户ID
   * @return 未读消息数
   */
  int getUnreadMessageCount(Long userId);

  /**
   * 生成会话ID
   * 
   * @param user1Id 用户1 ID
   * @param user2Id 用户2 ID
   * @return 会话ID
   */
  String generateConversationId(Long user1Id, Long user2Id);

  /**
   * 获取或创建会话
   * 
   * @param user1Id 用户1 ID
   * @param user2Id 用户2 ID
   * @return 会话对象
   */
  Conversation getOrCreateConversation(Long user1Id, Long user2Id);
}
