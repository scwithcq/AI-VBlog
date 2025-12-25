package com.vblog.controller;

import com.vblog.common.Page;
import com.vblog.common.Result;
import com.vblog.entity.Conversation;
import com.vblog.entity.Message;
import com.vblog.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 * 
 * @author V-Blog团队
 */
@Slf4j
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@Validated
public class MessageController extends BaseController {

  private final MessageService messageService;

  /**
   * 发送消息
   */
  @PostMapping("/send")
  public Result<Message> sendMessage(
      @RequestParam @NotNull(message = "接收者ID不能为空") Long toUserId,
      @RequestParam @NotBlank(message = "消息内容不能为空") String content,
      @RequestParam(defaultValue = "1") Integer messageType,
      HttpServletRequest request) {

    Long userId = (Long) request.getAttribute("userId");

    try {
      Message message = messageService.sendMessage(userId, toUserId, content, messageType);
      return Result.success("发送成功", message);
    } catch (RuntimeException e) {
      return Result.error(400, e.getMessage());
    }
  }

  /**
   * 获取会话消息列表
   */
  @GetMapping("/conversation/{conversationId}")
  public Result<Page<Message>> getConversationMessages(
      @PathVariable String conversationId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      HttpServletRequest request) {

    Long userId = (Long) request.getAttribute("userId");
    Page<Message> messages = messageService.getConversationMessages(conversationId, userId, page, size);
    return Result.success(messages);
  }

  /**
   * 获取会话列表
   */
  @GetMapping("/conversations")
  public Result<List<Conversation>> getConversations(HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    List<Conversation> conversations = messageService.getConversations(userId);
    return Result.success(conversations);
  }

  /**
   * 标记消息为已读
   */
  @PutMapping("/{messageId}/read")
  public Result<?> markMessageAsRead(
      @PathVariable Long messageId,
      HttpServletRequest request) {

    Long userId = (Long) request.getAttribute("userId");
    boolean success = messageService.markMessageAsRead(messageId, userId);

    if (success) {
      return Result.success("标记成功");
    } else {
      return Result.error(400, "标记失败");
    }
  }

  /**
   * 标记会话所有消息为已读
   */
  @PutMapping("/conversation/{conversationId}/read")
  public Result<?> markConversationAsRead(
      @PathVariable String conversationId,
      HttpServletRequest request) {

    Long userId = (Long) request.getAttribute("userId");
    boolean success = messageService.markConversationAsRead(conversationId, userId);

    if (success) {
      return Result.success("标记成功");
    } else {
      return Result.error(400, "标记失败");
    }
  }

  /**
   * 撤回消息
   */
  @PutMapping("/{messageId}/recall")
  public Result<?> recallMessage(
      @PathVariable Long messageId,
      HttpServletRequest request) {

    Long userId = (Long) request.getAttribute("userId");

    try {
      boolean success = messageService.recallMessage(messageId, userId);
      if (success) {
        return Result.success("撤回成功");
      } else {
        return Result.error(400, "撤回失败");
      }
    } catch (RuntimeException e) {
      return Result.error(400, e.getMessage());
    }
  }

  /**
   * 删除消息
   */
  @DeleteMapping("/{messageId}")
  public Result<?> deleteMessage(
      @PathVariable Long messageId,
      HttpServletRequest request) {

    Long userId = (Long) request.getAttribute("userId");
    boolean success = messageService.deleteMessage(messageId, userId);

    if (success) {
      return Result.success("删除成功");
    } else {
      return Result.error(400, "删除失败");
    }
  }

  /**
   * 获取未读消息总数
   */
  @GetMapping("/unread/count")
  public Result<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    int count = messageService.getUnreadMessageCount(userId);

    Map<String, Object> result = new HashMap<>();
    result.put("count", count);

    return Result.success(result);
  }

  /**
   * 生成会话ID
   */
  @GetMapping("/conversation/id")
  public Result<Map<String, Object>> generateConversationId(
      @RequestParam @NotNull(message = "对方用户ID不能为空") Long friendId,
      HttpServletRequest request) {

    Long userId = (Long) request.getAttribute("userId");
    String conversationId = messageService.generateConversationId(userId, friendId);

    Map<String, Object> result = new HashMap<>();
    result.put("conversationId", conversationId);

    return Result.success(result);
  }
}
