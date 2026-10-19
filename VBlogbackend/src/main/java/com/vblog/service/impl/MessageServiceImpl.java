package com.vblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vblog.entity.Conversation;
import com.vblog.entity.Message;
import com.vblog.mapper.ConversationMapper;
import com.vblog.mapper.MessageMapper;
import com.vblog.service.FriendshipService;
import com.vblog.service.MessageService;
import com.vblog.service.NotificationService;
import com.vblog.websocket.MessageWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 消息服务实现类
 *
 * @author V-Blog团队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

  private final MessageMapper messageMapper;
  private final ConversationMapper conversationMapper;
  private final FriendshipService friendshipService;
  private final NotificationService notificationService;
  private final MessageWebSocketHandler webSocketHandler;

  private static final int RECALL_TIME_LIMIT = 2; // 撤回时间限制（分钟）

  @Override
  @Transactional
  public Message sendMessage(Long fromUserId, Long toUserId, String content, Integer messageType) {
    // 检查是否为好友
    if (!friendshipService.isFriend(fromUserId, toUserId)) {
      throw new RuntimeException("只能向好友发送消息");
    }

    // 生成会话ID
    String conversationId = generateConversationId(fromUserId, toUserId);

    // 创建消息
    Message message = new Message();
    message.setConversationId(conversationId);
    message.setFromUserId(fromUserId);
    message.setToUserId(toUserId);
    message.setContent(content);
    message.setMessageType(messageType);
    message.setStatus(0); // 未读
    message.setIsDeletedBySender(0);
    message.setIsDeletedByReceiver(0);

    messageMapper.insert(message);

    // 更新或创建会话
    updateConversation(conversationId, fromUserId, toUserId, message);

    // 暂时禁用通知功能，等好友功能测试通过后再启用
    // try {
    // notificationService.createNotification(
    // toUserId,
    // fromUserId,
    // 5, // 消息通知类型
    // message.getMessageId(),
    // "收到新消息");
    // } catch (Exception e) {
    // log.warn("发送消息通知失败，但消息已发送成功: {}", e.getMessage());
    // }

    // 通过WebSocket实时推送消息
    webSocketHandler.sendEventAfterCommit(toUserId, "new_message", message);

    log.info("用户 {} 向用户 {} 发送消息", fromUserId, toUserId);
    return message;
  }

  @Override
  public com.vblog.common.Page<Message> getConversationMessages(String conversationId, Long userId, int page,
      int size) {
    requireFriendConversation(conversationId, userId);
    Page<Message> mybatisPage = new Page<>(page, size);
    Page<Message> result = messageMapper.selectConversationMessages(mybatisPage, conversationId, userId);

    com.vblog.common.Page<Message> pageResult = new com.vblog.common.Page<>();
    pageResult.setRecords(result.getRecords());
    pageResult.setTotal((int) result.getTotal());
    pageResult.setCurrent((int) result.getCurrent());
    pageResult.setSize((int) result.getSize());

    return pageResult;
  }

  @Override
  public List<Conversation> getConversations(Long userId) {
    return conversationMapper.selectUserConversations(userId);
  }

  @Override
  @Transactional
  public boolean markMessageAsRead(Long messageId, Long userId) {
    Message message = messageMapper.selectById(messageId);
    if (message == null || !message.getToUserId().equals(userId)) {
      return false;
    }

    if (message.getStatus() == 0) {
      message.setStatus(1);
      messageMapper.updateById(message);

      // 单条已读后保留同一会话中其他消息的未读数。
      Integer remaining = messageMapper.selectConversationUnreadCount(message.getConversationId(), userId);
      conversationMapper.setUnreadCount(message.getConversationId(), userId, remaining == null ? 0 : remaining);
    }

    return true;
  }

  @Override
  @Transactional
  public boolean markConversationAsRead(String conversationId, Long userId) {
    requireFriendConversation(conversationId, userId);
    // 批量更新消息状态
    int count = messageMapper.batchUpdateMessageStatus(conversationId, userId, 1);

    // 清零会话未读数
    conversationMapper.clearUnreadCount(conversationId, userId);

    log.info("用户 {} 标记会话 {} 为已读，共 {} 条消息", userId, conversationId, count);
    return true;
  }

  @Override
  @Transactional
  public boolean recallMessage(Long messageId, Long userId) {
    Message message = messageMapper.selectById(messageId);
    if (message == null) {
      throw new RuntimeException("消息不存在");
    }

    // 验证权限
    if (!message.getFromUserId().equals(userId)) {
      throw new RuntimeException("只能撤回自己发送的消息");
    }

    // 检查撤回时间限制
    long seconds = ChronoUnit.SECONDS.between(message.getCreateTime(), LocalDateTime.now());
    if (seconds > RECALL_TIME_LIMIT * 60) {
      throw new RuntimeException("超过撤回时间限制（2分钟）");
    }

    // 标记为已撤回
    message.setStatus(2);
    message.setContent("[消息已撤回]");
    messageMapper.updateById(message);

    // 如果是最后一条消息，更新会话内容
    updateConversationLastMessage(message.getConversationId());
    Integer unread = messageMapper.selectConversationUnreadCount(message.getConversationId(), message.getToUserId());
    conversationMapper.setUnreadCount(message.getConversationId(), message.getToUserId(), unread == null ? 0 : unread);
    webSocketHandler.sendEventAfterCommit(message.getToUserId(), "message_recall", message);
    webSocketHandler.sendEventAfterCommit(message.getFromUserId(), "message_recall", message);

    log.info("用户 {} 撤回消息 {}", userId, messageId);
    return true;
  }

  @Override
  @Transactional
  public boolean deleteMessage(Long messageId, Long userId) {
    Message message = messageMapper.selectById(messageId);
    if (message == null) {
      return false;
    }

    // 单方删除
    if (message.getFromUserId().equals(userId)) {
      message.setIsDeletedBySender(1);
    } else if (message.getToUserId().equals(userId)) {
      message.setIsDeletedByReceiver(1);
    } else {
      return false;
    }

    messageMapper.updateById(message);
    if (message.getToUserId().equals(userId)) {
      Integer unread = messageMapper.selectConversationUnreadCount(message.getConversationId(), userId);
      conversationMapper.setUnreadCount(message.getConversationId(), userId, unread == null ? 0 : unread);
    }
    log.info("用户 {} 删除消息 {}", userId, messageId);
    return true;
  }

  @Override
  public int getUnreadMessageCount(Long userId) {
    Integer count = messageMapper.selectUnreadCount(userId);
    return count != null ? count : 0;
  }

  @Override
  public String generateConversationId(Long user1Id, Long user2Id) {
    long minId = Math.min(user1Id, user2Id);
    long maxId = Math.max(user1Id, user2Id);
    return minId + "_" + maxId;
  }

  @Override
  @Transactional
  public Conversation getOrCreateConversation(Long user1Id, Long user2Id) {
    String conversationId = generateConversationId(user1Id, user2Id);

    Conversation conversation = conversationMapper.selectById(conversationId);
    if (conversation == null) {
      conversation = new Conversation();
      conversation.setConversationId(conversationId);
      conversation.setUser1Id(Math.min(user1Id, user2Id));
      conversation.setUser2Id(Math.max(user1Id, user2Id));
      conversation.setUser1UnreadCount(0);
      conversation.setUser2UnreadCount(0);
      conversationMapper.insert(conversation);
    }

    return conversation;
  }

  private void requireFriendConversation(String conversationId, Long userId) {
    Conversation conversation = conversationMapper.selectById(conversationId);
    Long otherUserId;
    if (conversation == null) {
      // 新好友还没有发送消息时，会话记录尚未建立。
      String[] ids = conversationId.split("_", -1);
      if (ids.length != 2) {
        throw new IllegalArgumentException("会话不存在或无权访问");
      }
      try {
        long first = Long.parseLong(ids[0]);
        long second = Long.parseLong(ids[1]);
        if (first >= second || !conversationId.equals(generateConversationId(first, second)) ||
            (userId != first && userId != second)) {
          throw new IllegalArgumentException("会话不存在或无权访问");
        }
        otherUserId = userId == first ? second : first;
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("会话不存在或无权访问");
      }
    } else {
      if (!userId.equals(conversation.getUser1Id()) && !userId.equals(conversation.getUser2Id())) {
        throw new IllegalArgumentException("会话不存在或无权访问");
      }
      otherUserId = userId.equals(conversation.getUser1Id())
          ? conversation.getUser2Id() : conversation.getUser1Id();
    }
    if (!friendshipService.isFriend(userId, otherUserId)) {
      throw new IllegalArgumentException("仅可查看好友会话");
    }
  }

  /**
   * 更新会话信息
   */
  private void updateConversation(String conversationId, Long fromUserId, Long toUserId, Message message) {
    Conversation conversation = getOrCreateConversation(fromUserId, toUserId);

    conversation.setLastMessageId(message.getMessageId());
    conversation.setLastMessageContent(truncateContent(message.getContent()));
    conversation.setLastMessageTime(message.getCreateTime());

    // 增加接收者的未读数
    long minId = Math.min(fromUserId, toUserId);
    if (toUserId == minId) {
      conversation.setUser1UnreadCount(conversation.getUser1UnreadCount() + 1);
    } else {
      conversation.setUser2UnreadCount(conversation.getUser2UnreadCount() + 1);
    }

    conversationMapper.updateById(conversation);
  }

  /**
   * 更新会话的最后一条消息
   */
  private void updateConversationLastMessage(String conversationId) {
    LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Message::getConversationId, conversationId)
        .orderByDesc(Message::getCreateTime)
        .last("LIMIT 1");

    Message lastMessage = messageMapper.selectOne(wrapper);
    if (lastMessage != null) {
      Conversation conversation = conversationMapper.selectById(conversationId);
      if (conversation != null) {
        conversation.setLastMessageId(lastMessage.getMessageId());
        conversation.setLastMessageContent(truncateContent(lastMessage.getContent()));
        conversation.setLastMessageTime(lastMessage.getCreateTime());
        conversationMapper.updateById(conversation);
      }
    }
  }

  /**
   * 截取消息内容摘要
   */
  private String truncateContent(String content) {
    if (content == null) {
      return "";
    }
    if (content.length() <= 50) {
      return content;
    }
    return content.substring(0, 50) + "...";
  }

}
