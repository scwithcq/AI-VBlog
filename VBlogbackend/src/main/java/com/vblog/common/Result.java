package com.vblog.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * 统一响应包装类
 * 
 * @param <T> 响应数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应代码
     * 0: 成功
     * 400xx: 客户端错误
     * 401xx: 认证错误
     * 403xx: 授权错误
     * 404xx: 资源未找到错误
     * 500xx: 服务器错误
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 用于日志记录和调试的跟踪ID
     */
    private String traceId;

    /**
     * 带有traceId生成的私有构造函数
     */
    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.traceId = generateTraceId();
    }

    /**
     * 生成跟踪ID (yyyyMMddHHmmss + 6位随机数字)
     */
    private String generateTraceId() {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", (int) (Math.random() * 1000000));
        return timestamp + random;
    }

    /**
     * 无数据的成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(0, "成功", null);
    }

    /**
     * 带数据的成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(0, "成功", data);
    }

    /**
     * 带自定义消息和数据的成功响应
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(0, msg, data);
    }

    /**
     * 客户端错误响应 (400 Bad Request)
     */
    public static <T> Result<T> badRequest(String msg) {
        return new Result<>(40001, msg, null);
    }

    /**
     * 未授权错误响应 (401 Unauthorized)
     */
    public static <T> Result<T> unauthorized(String msg) {
        return new Result<>(40101, msg != null ? msg : "登录已过期，请重新登录", null);
    }

    /**
     * 禁止访问错误响应 (403 Forbidden)
     */
    public static <T> Result<T> forbidden(String msg) {
        return new Result<>(40301, msg != null ? msg : "您没有该操作的权限", null);
    }

    /**
     * 资源未找到错误响应 (404 Not Found)
     */
    public static <T> Result<T> notFound(String msg) {
        return new Result<>(40401, msg != null ? msg : "记录不存在", null);
    }

    /**
     * 服务器错误响应 (500 Internal Server Error)
     */
    public static <T> Result<T> serverError(String msg) {
        return new Result<>(50001, msg != null ? msg : "服务器开小差了，请稍后重试", null);
    }

    /**
     * 带特定代码和消息的自定义错误响应
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }
}
