package com.vblog.websocket;

import com.alibaba.fastjson2.JSON;
import com.vblog.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息WebSocket处理器
 *
 * @author V-Blog团队
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageWebSocketHandler extends TextWebSocketHandler {

  private final JwtUtil jwtUtil;

  // 存储所有在线用户的session，key为userId
  private static final Map<Long, Map<String, WebSocketSession>> ONLINE_USERS = new ConcurrentHashMap<>();

  /**
   * 连接建立时
   */
  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    // 从URL参数中获取token
    Long userId = getUserIdFromSession(session);

    if (userId != null) {
      ONLINE_USERS.computeIfAbsent(userId, ignored -> new ConcurrentHashMap<>()).put(session.getId(), session);
      log.info("用户 {} 建立WebSocket连接", userId);

      // 发送连接成功消息
      sendMessage(userId, createMessage("connect", "连接成功", null));
    } else {
      session.close(CloseStatus.BAD_DATA);
      log.warn("WebSocket连接失败：无效的token");
    }
  }

  /**
   * 接收消息时
   */
  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String payload = message.getPayload();
    log.debug("Received WebSocket heartbeat");

    // 可以在这里处理心跳、状态更新等消息
    // 实际的消息发送仍通过HTTP API
  }

  /**
   * 连接关闭时
   */
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    Long userId = getUserIdFromSession(session);
    if (userId != null) {
      removeSession(userId, session);
      log.info("用户 {} 断开WebSocket连接", userId);
    }
  }

  /**
   * 传输错误时
   */
  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    Long userId = getUserIdFromSession(session);
    log.error("WebSocket传输错误，用户ID: {}", userId, exception);
    if (session.isOpen()) {
      session.close(CloseStatus.SERVER_ERROR);
    }
    if (userId != null) {
      removeSession(userId, session);
    }
  }

  /**
   * 向指定用户发送消息
   */
  public void sendMessage(Long userId, String message) {
    Map<String, WebSocketSession> sessions = ONLINE_USERS.get(userId);
    if (sessions == null) return;
    for (WebSocketSession session : sessions.values()) {
      if (!session.isOpen()) continue;
      if (getUserIdFromSession(session) == null) {
        try { session.close(CloseStatus.POLICY_VIOLATION); } catch (IOException ignored) {}
        removeSession(userId, session);
        continue;
      }
      try {
        session.sendMessage(new TextMessage(message));
        log.debug("向用户 {} 发送WebSocket消息", userId);
      } catch (IOException e) {
        log.error("发送WebSocket消息失败，用户ID: {}", userId, e);
        removeSession(userId, session);
      }
    }
  }

  private void removeSession(Long userId, WebSocketSession session) {
    Map<String, WebSocketSession> sessions = ONLINE_USERS.get(userId);
    if (sessions != null) {
      sessions.remove(session.getId(), session);
      if (sessions.isEmpty()) ONLINE_USERS.remove(userId, sessions);
    }
  }

  public void sendEventAfterCommit(Long userId, String type, Object data) {
    Map<String, Object> event = new HashMap<>();
    event.put("type", type);
    event.put("data", data);
    event.put("timestamp", System.currentTimeMillis());
    String payload = JSON.toJSONString(event);
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          sendMessage(userId, payload);
        }
      });
    } else {
      sendMessage(userId, payload);
    }
  }

  /**
   * 检查用户是否在线
   */
  public boolean isUserOnline(Long userId) {
    Map<String, WebSocketSession> sessions = ONLINE_USERS.get(userId);
    return sessions != null && sessions.values().stream().anyMatch(WebSocketSession::isOpen);
  }

  /**
   * 获取在线用户数量
   */
  public int getOnlineUserCount() {
    return ONLINE_USERS.size();
  }

  /**
   * 从session中获取userId
   */
  private Long getUserIdFromSession(WebSocketSession session) {
    try {
      URI uri = session.getUri();
      if (uri != null) {
        String query = uri.getQuery();
        if (query != null && query.contains("token=")) {
          String token = query.substring(query.indexOf("token=") + 6);
          if (token.contains("&")) {
            token = token.substring(0, token.indexOf("&"));
          }

          // 验证token并获取userId
          if (jwtUtil.validateToken(token)) {
            return jwtUtil.getUserIdFromToken(token);
          }
        }
      }
    } catch (Exception e) {
      log.error("从session获取userId失败", e);
    }
    return null;
  }

  /**
   * 创建消息JSON
   */
  private String createMessage(String type, String content, Object data) {
    // 连接确认消息的 data 可以为 null，ConcurrentHashMap 不接受 null 值。
    Map<String, Object> message = new HashMap<>();
    message.put("type", type);
    message.put("content", content);
    message.put("data", data);
    message.put("timestamp", System.currentTimeMillis());
    return JSON.toJSONString(message);
  }
}
