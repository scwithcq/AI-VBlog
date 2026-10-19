package com.vblog.exception;

import com.vblog.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global Exception Handler
 *
 * @author V-Blog Team
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Seeking a video or leaving a page can close the media response mid-stream.
    @ExceptionHandler({org.apache.catalina.connector.ClientAbortException.class,
            org.springframework.web.context.request.async.AsyncRequestNotUsableException.class})
    public void handleDisconnectedClient(Exception e) {
        log.debug("Client closed the response stream: {}", e.getMessage());
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public Result<?> handleConstraintViolation(jakarta.validation.ConstraintViolationException e) {
        return Result.badRequest(e.getConstraintViolations().stream()
                .map(v -> v.getMessage()).collect(Collectors.joining(", ")));
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public Result<?> handleLargeUpload(Exception e) {
        return Result.badRequest("上传文件超过大小限制");
    }

    @ExceptionHandler(java.io.IOException.class)
    public Result<?> handleUploadFailure(java.io.IOException e) {
        return Result.badRequest("文件无效或无法保存，请检查类型与大小");
    }

    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public Result<?> handleDuplicate(Exception e) {
        return Result.badRequest("数据已存在，请勿重复提交");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleInvalidArgument(IllegalArgumentException e) {
        return Result.badRequest(e.getMessage());
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public Result<?> handleTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e) {
        return Result.badRequest("请求参数格式不正确: " + e.getName());
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public Result<?> handleValidationException(Exception e) {
        String message = "参数验证失败";
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        }
        log.warn("参数验证错误: {}", message);
        return Result.badRequest(message);
    }

    /**
     * Handle missing request parameter exceptions
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String message = String.format("缺少必要参数: %s，参数类型: %s", e.getParameterName(), e.getParameterType());
        log.warn("缺少请求参数: {}", message);
        return Result.badRequest(message);
    }

    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return Result.serverError("服务器内部错误");
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("意外异常: ", e);
        return Result.serverError("服务器内部错误");
    }
}
