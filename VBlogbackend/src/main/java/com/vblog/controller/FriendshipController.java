package com.vblog.controller;

import com.vblog.common.Result;
import com.vblog.entity.FriendRequest;
import com.vblog.entity.User;
import com.vblog.service.FriendshipService;
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
 * 好友关系控制器
 * 
 * @author V-Blog团队
 */
@Slf4j
@RestController
@RequestMapping("/friendship")
@RequiredArgsConstructor
@Validated
public class FriendshipController extends BaseController {

  private final FriendshipService friendshipService;

  /**
   * 搜索用户
   */
  @GetMapping("/search")
  public Result<List<User>> searchUsers(
      @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword,
      HttpServletRequest request) {

    Long userId = (Long) request.getAttribute("userId");
    List<User> users = friendshipService.searchUsers(keyword, userId);
    return Result.success(users);
  }

  /**
   * 发送好友申请
   */
  @PostMapping("/request")
  public Result<FriendRequest> sendFriendRequest(
      @RequestParam @NotNull(message = "目标用户ID不能为空") Long toUserId,
      @RequestParam(required = false) String message,
      HttpServletRequest request) {

    Long userId = (Long) request.getAttribute("userId");

    try {
      FriendRequest friendRequest = friendshipService.sendFriendRequest(userId, toUserId, message);
      return Result.success("好友申请已发送", friendRequest);
    } catch (RuntimeException e) {
      return Result.error(400, e.getMessage());
    }
  }

  /**
   * 获取收到的好友申请列表
   */
  @GetMapping("/requests/received")
  public Result<List<FriendRequest>> getReceivedRequests(
      @RequestParam(required = false) Integer status,
      HttpServletRequest request) {

    Long userId = (Long) request.getAttribute("userId");
    List<FriendRequest> requests = friendshipService.getReceivedRequests(userId, status);
    return Result.success(requests);
  }

  /**
   * 获取发送的好友申请列表
   */
  @GetMapping("/requests/sent")
  public Result<List<FriendRequest>> getSentRequests(
      @RequestParam(required = false) Integer status,
      HttpServletRequest request) {

    Long userId = (Long) request.getAttribute("userId");
    List<FriendRequest> requests = friendshipService.getSentRequests(userId, status);
    return Result.success(requests);
  }

  /**
   * 处理好友申请
   */
  @PutMapping("/request/{requestId}")
  public Result<?> handleFriendRequest(
      @PathVariable Long requestId,
      @RequestParam(name = "accept", required = true) Boolean accept,
      HttpServletRequest request) {

    Long userId = (Long) request.getAttribute("userId");

    log.info("处理好友申请: requestId={}, userId={}, accept={}", requestId, userId, accept);

    try {
      boolean success = friendshipService.handleFriendRequest(requestId, userId, accept);
      if (success) {
        return Result.success(accept ? "已同意好友申请" : "已拒绝好友申请");
      } else {
        return Result.error(400, "处理失败");
      }
    } catch (RuntimeException e) {
      log.error("处理好友申请失败: requestId={}, error={}", requestId, e.getMessage(), e);
      return Result.error(400, e.getMessage());
    }
  }

  /**
   * 获取好友列表
   */
  @GetMapping("/friends")
  public Result<List<User>> getFriends(HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    List<User> friends = friendshipService.getFriends(userId);
    return Result.success(friends);
  }

  /**
   * 删除好友
   */
  @DeleteMapping("/friend/{friendId}")
  public Result<?> deleteFriend(
      @PathVariable Long friendId,
      HttpServletRequest request) {

    Long userId = (Long) request.getAttribute("userId");
    boolean success = friendshipService.deleteFriend(userId, friendId);

    if (success) {
      return Result.success("删除好友成功");
    } else {
      return Result.error(400, "删除失败");
    }
  }

  /**
   * 修改好友备注
   */
  @PutMapping("/friend/{friendId}/remark")
  public Result<?> updateFriendRemark(
      @PathVariable Long friendId,
      @RequestParam(name = "remark", required = true) @NotBlank(message = "备注不能为空") String remark,
      HttpServletRequest request) {

    Long userId = (Long) request.getAttribute("userId");
    log.info("修改好友备注: userId={}, friendId={}, remark={}", userId, friendId, remark);

    try {
      boolean success = friendshipService.updateFriendRemark(userId, friendId, remark);
      if (success) {
        return Result.success("修改成功");
      } else {
        return Result.error(400, "未找到好友关系或修改失败");
      }
    } catch (RuntimeException e) {
      log.error("修改好友备注失败: userId={}, friendId={}, error={}", userId, friendId, e.getMessage());
      return Result.error(400, e.getMessage());
    }
  }

  /**
   * 检查好友关系
   */
  @GetMapping("/check/{targetUserId}")
  public Result<Map<String, Object>> checkFriendship(
      @PathVariable Long targetUserId,
      HttpServletRequest request) {

    Long userId = (Long) request.getAttribute("userId");
    boolean isFriend = friendshipService.isFriend(userId, targetUserId);

    Map<String, Object> result = new HashMap<>();
    result.put("isFriend", isFriend);

    return Result.success(result);
  }

  /**
   * 获取好友数量
   */
  @GetMapping("/count")
  public Result<Map<String, Object>> getFriendCount(HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    int count = friendshipService.getFriendCount(userId);

    Map<String, Object> result = new HashMap<>();
    result.put("count", count);

    return Result.success(result);
  }
}
