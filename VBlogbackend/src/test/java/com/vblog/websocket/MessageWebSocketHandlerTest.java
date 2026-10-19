package com.vblog.websocket;

import com.vblog.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class MessageWebSocketHandlerTest {
  @Test
  void sendsConnectionConfirmationWithNullData() throws Exception {
    JwtUtil jwtUtil = mock(JwtUtil.class);
    WebSocketSession session = mock(WebSocketSession.class);
    when(session.getUri()).thenReturn(URI.create("ws://localhost/ws/message?token=test-token"));
    when(session.getId()).thenReturn("first-session");
    when(session.isOpen()).thenReturn(true);
    when(jwtUtil.validateToken("test-token")).thenReturn(true);
    when(jwtUtil.getUserIdFromToken("test-token")).thenReturn(1L);

    MessageWebSocketHandler handler = new MessageWebSocketHandler(jwtUtil);
    handler.afterConnectionEstablished(session);

    verify(session).sendMessage(any(TextMessage.class));
    assertTrue(handler.isUserOnline(1L));
    handler.afterConnectionClosed(session, org.springframework.web.socket.CloseStatus.NORMAL);
  }

  @Test
  void closingOneTabKeepsOtherTabConnected() throws Exception {
    JwtUtil jwtUtil = mock(JwtUtil.class);
    when(jwtUtil.validateToken("test-token")).thenReturn(true);
    when(jwtUtil.getUserIdFromToken("test-token")).thenReturn(2L);
    WebSocketSession first = mock(WebSocketSession.class);
    WebSocketSession second = mock(WebSocketSession.class);
    when(first.getUri()).thenReturn(URI.create("ws://localhost/ws/message?token=test-token"));
    when(second.getUri()).thenReturn(URI.create("ws://localhost/ws/message?token=test-token"));
    when(first.getId()).thenReturn("first");
    when(second.getId()).thenReturn("second");
    when(first.isOpen()).thenReturn(true);
    when(second.isOpen()).thenReturn(true);
    MessageWebSocketHandler handler = new MessageWebSocketHandler(jwtUtil);

    handler.afterConnectionEstablished(first);
    handler.afterConnectionEstablished(second);
    handler.afterConnectionClosed(first, org.springframework.web.socket.CloseStatus.NORMAL);
    handler.sendMessage(2L, "new message");

    assertTrue(handler.isUserOnline(2L));
    verify(second, times(2)).sendMessage(any(TextMessage.class));
    handler.afterConnectionClosed(second, org.springframework.web.socket.CloseStatus.NORMAL);
  }
}
