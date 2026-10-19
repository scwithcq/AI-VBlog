package com.vblog.controller;

import com.vblog.common.Page;
import com.vblog.common.Result;
import com.vblog.entity.Notification;
import com.vblog.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 *
 * @author V-Blog团队
 */
@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
@Validated
public class NotificationController extends BaseController {

    private final NotificationService notificationService;

    /**
     * 获取用户的通知列表
     */
    @GetMapping("/list")
    public Result<Page<Notification>> getNotificationList(
            @RequestParam(defaultValue = "1") @Min(1) @jakarta.validation.constraints.Max(100000) int page,
            @RequestParam(defaultValue = "10") @Min(1) @jakarta.validation.constraints.Max(100) int size,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        Page<Notification> notificationPage = notificationService.getNotificationList(userId, page, size);
        return Result.success(notificationPage);
    }

    /**
     * 获取用户未读通知数量
     */
    @GetMapping("/unread/count")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long unreadCount = notificationService.getUnreadCount(userId);
        return Result.success(unreadCount);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{notificationId}/read")
    public Result<?> markAsRead(
            @PathVariable Long notificationId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        boolean marked = notificationService.markAsRead(notificationId, userId);
        if (marked) {
            return Result.success("标记成功");
        } else {
            return Result.error(400, "标记失败，通知不存在或您没有权限");
        }
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/all/read")
    public Result<?> markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int count = notificationService.markAllAsRead(userId);
        return Result.success("成功标记 " + count + " 条通知为已读");
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    public Result<?> deleteNotification(
            @PathVariable Long notificationId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        boolean deleted = notificationService.deleteNotification(notificationId, userId);
        if (deleted) {
            return Result.success("删除成功");
        } else {
            return Result.error(400, "删除失败，通知不存在或您没有权限");
        }
    }
}
