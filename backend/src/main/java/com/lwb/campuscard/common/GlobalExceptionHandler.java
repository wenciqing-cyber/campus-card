package com.lwb.campuscard.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

    /**
     * 静态资源不存在：浏览器自动请求的 favicon.ico、直接敲错的路径都走这里
     * 这不是程序出错，按 404 返回，不打堆栈，否则控制台会被刷满
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResource(NoResourceFoundException e) {
        log.debug("资源不存在：{}", e.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.fail(HttpStatus.NOT_FOUND.value(), "资源不存在"));
    }

    /**
     * 数据库连不上：最常见的就是没设 DB_PASSWORD 而走了默认值，或者 MySQL 没启动
     * 这种情况只打一行，不打整条堆栈
     */
    @ExceptionHandler({CannotCreateTransactionException.class, CannotGetJdbcConnectionException.class})
    public Result<Void> handleDbDown(Exception e) {
        log.error("数据库连接失败：{}", rootMessage(e));
        return Result.fail(ErrorCode.BIZ_ERROR,
                "数据库连接失败，请检查 MySQL 是否启动、DB_PASSWORD 环境变量是否正确");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        String ref = Long.toHexString(System.nanoTime());
        log.error("未处理异常 ref={}", ref, e);
        return Result.fail(ErrorCode.BIZ_ERROR, "系统繁忙，请稍后重试（错误编号 " + ref + "）");
    }

    /** Hikari 会把真实原因裹好几层，取最里面那句 */
    private String rootMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t.getMessage();
    }
}
