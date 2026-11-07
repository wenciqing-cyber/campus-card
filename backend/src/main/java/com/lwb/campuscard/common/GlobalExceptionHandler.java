package com.lwb.campuscard.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：业务异常转成 Result，其余兜底
 * 兜底分支不把内部信息抛给前端，只给一个短编号，按编号去日志里找堆栈
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArg(IllegalArgumentException e) {
        return Result.fail(ErrorCode.AMOUNT_INVALID, e.getMessage());
    }

    /** 请求体不是合法 JSON，或者字段类型对不上，比如 amount 传成了字符串 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleUnreadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败：{}", e.getMessage());
        return Result.fail(ErrorCode.BIZ_ERROR, "请求体格式不正确");
    }

    /** 路径参数或 query 参数没带 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.fail(ErrorCode.BIZ_ERROR, "缺少必要参数：" + e.getParameterName());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethod(HttpRequestMethodNotSupportedException e) {
        return Result.fail(ErrorCode.BIZ_ERROR, "不支持的请求方法：" + e.getMethod());
    }

    /** 唯一键冲突，比如两个人同时用同一个学号开户，先到的建成功，后到的落到这里 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDuplicate(DataIntegrityViolationException e) {
        log.warn("唯一约束冲突：{}", e.getMostSpecificCause().getMessage());
        return Result.fail(ErrorCode.BIZ_ERROR, "数据已存在，请勿重复提交");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        String ref = Long.toHexString(System.nanoTime());
        log.error("未处理异常 ref={}", ref, e);
        return Result.fail(ErrorCode.BIZ_ERROR, "系统繁忙，请稍后重试（错误编号 " + ref + "）");
    }
}
