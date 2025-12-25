package com.vblog.controller;

import com.vblog.common.Page;
import com.vblog.common.Result;
import com.vblog.entity.SystemNotification;
import com.vblog.service.SystemNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统互动通知控制器
 * 
 * @author V-Blog团队
 */
@Slf4j
@RestController
@RequestMapping("/system-notification")
@RequiredArgsConstructor
public class SystemNotificationController extends BaseController {

  private final SystemNotificationService notificationService;

  /**
   * 获取通知列表（分页）
   */
  @GetMapping("/list")
  public Result<Page<SystemNotification>> getNotifications(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    Page<SystemNotification> notifications = notificationService.getNotificationsByRecipient(userId, page, size);
    return Result.success(notifications);
  }

  /**
   * 获取未读通知列表
   */
  @GetMapping("/unread")
  public Result<Page<SystemNotification>> getUnreadNotifications(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    Page<SystemNotification> notifications = notificationService.getUnreadNotifications(userId, page, size);
    return Result.success(notifications);
  }

  /**
   * 根据类型获取通知列表
   */
  @GetMapping("/type/{type}")
  public Result<Page<SystemNotification>> getNotificationsByType(
      @PathVariable String type,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    Page<SystemNotification> notifications = notificationService.getNotificationsByType(userId, type, page, size);
    return Result.success(notifications);
  }

  /**
   * 获取未读通知数
   */
  @GetMapping("/unread/count")
  public Result<Integer> getUnreadCount(HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    int count = notificationService.getUnreadCount(userId);
    return Result.success(count);
  }

  /**
   * 获取各类型未读通知数统计
   */
  @GetMapping("/unread/count/by-type")
  public Result<Map<String, Integer>> getUnreadCountByType(HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    Map<String, Integer> counts = notificationService.getUnreadCountByType(userId);
    return Result.success(counts);
  }

  /**
   * 标记通知为已读
   */
  @PutMapping("/{notificationId}/read")
  public Result<?> markAsRead(
      @PathVariable Long notificationId,
      HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    boolean success = notificationService.markAsRead(notificationId, userId);
    return success ? Result.success("标记成功") : Result.serverError("标记失败");
  }

  /**
   * 批量标记为已读
   */
  @PutMapping("/read/batch")
  public Result<?> markAsReadBatch(
      @RequestBody List<Long> notificationIds,
      HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    boolean success = notificationService.markAsReadBatch(notificationIds, userId);
    return success ? Result.success("标记成功") : Result.serverError("标记失败");
  }

  /**
   * 标记所有通知为已读
   */
  @PutMapping("/read/all")
  public Result<?> markAllAsRead(HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    boolean success = notificationService.markAllAsRead(userId);
    return success ? Result.success("标记成功") : Result.serverError("标记失败");
  }

  /**
   * 标记某类型所有通知为已读
   */
  @PutMapping("/read/all/{type}")
  public Result<?> markAllAsReadByType(
      @PathVariable String type,
      HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    boolean success = notificationService.markAllAsReadByType(userId, type);
    return success ? Result.success("标记成功") : Result.serverError("标记失败");
  }

  /**
   * 删除通知
   */
  @DeleteMapping("/{notificationId}")
  public Result<?> deleteNotification(
      @PathVariable Long notificationId,
      HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    boolean success = notificationService.deleteNotification(notificationId, userId);
    return success ? Result.success("删除成功") : Result.serverError("删除失败");
  }

  /**
   * 清理旧的已读通知（管理员功能）
   */
  @DeleteMapping("/cleanup")
  public Result<Integer> cleanupReadNotifications(
      @RequestParam(defaultValue = "30") int days) {
    int count = notificationService.cleanupReadNotifications(days);
    return Result.success("清理了 " + count + " 条通知", count);
  }
}
