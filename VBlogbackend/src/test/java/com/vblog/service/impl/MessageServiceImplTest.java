package com.vblog.service.impl;

import com.vblog.entity.Conversation;
import com.vblog.entity.Message;
import com.vblog.mapper.ConversationMapper;
import com.vblog.mapper.MessageMapper;
import com.vblog.service.FriendshipService;
import com.vblog.service.NotificationService;
import com.vblog.websocket.MessageWebSocketHandler;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {
  @Mock MessageMapper messageMapper;
  @Mock ConversationMapper conversationMapper;
  @Mock FriendshipService friendshipService;
  @Mock NotificationService notificationService;
  @Mock MessageWebSocketHandler webSocketHandler;
  @InjectMocks MessageServiceImpl service;

  private Conversation conversation;

  @BeforeEach
  void setUp() {
    conversation = new Conversation();
    conversation.setConversationId("1_2");
    conversation.setUser1Id(1L);
    conversation.setUser2Id(2L);
  }

  @Test
  void unrelatedUserCannotReadConversation() {
    when(conversationMapper.selectById("1_2")).thenReturn(conversation);

    assertThrows(IllegalArgumentException.class, () -> service.getConversationMessages("1_2", 3L, 1, 20));
    verify(messageMapper, never()).selectConversationMessages(any(), eq("1_2"), eq(3L));
  }

  @Test
  void formerFriendCannotMarkConversationRead() {
    when(conversationMapper.selectById("1_2")).thenReturn(conversation);
    when(friendshipService.isFriend(1L, 2L)).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> service.markConversationAsRead("1_2", 1L));
    verify(messageMapper, never()).batchUpdateMessageStatus(any(), any(), any());
  }

  @Test
  void friendsCanOpenConversationBeforeFirstMessage() {
    when(friendshipService.isFriend(1L, 2L)).thenReturn(true);
    when(messageMapper.selectConversationMessages(any(), eq("1_2"), eq(1L)))
        .thenReturn(new Page<>(1, 20));

    assertEquals(0, service.getConversationMessages("1_2", 1L, 1, 20).getTotal());
  }

  @Test
  void markingOneMessageReadKeepsOtherUnreadMessages() {
    Message message = new Message();
    message.setMessageId(10L);
    message.setConversationId("1_2");
    message.setToUserId(2L);
    message.setStatus(0);
    when(messageMapper.selectById(10L)).thenReturn(message);
    when(messageMapper.selectConversationUnreadCount("1_2", 2L)).thenReturn(2);

    assertEquals(true, service.markMessageAsRead(10L, 2L));
    verify(conversationMapper).setUnreadCount("1_2", 2L, 2);
    verify(conversationMapper, never()).clearUnreadCount("1_2", 2L);
  }
}
