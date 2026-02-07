package com.syan.smart_park.controller.advice;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.BusinessException;
import com.syan.smart_park.common.exception.ReturnCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
@ResponseBody
public class RestExceptionHandler {

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(BusinessException.class)
    public R<String> businessException(BusinessException e) {
        log.error("业务异常 code={}, BusinessException = {}", e.getCode(), e.getMessage(), e);
        return R.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数绑定异常（@RequestParam 参数缺失或类型转换失败）
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<String> missingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("请求参数缺失: {}", e.getParameterName(), e);
        String message = String.format("参数 '%s' 不能为空", e.getParameterName());
        return R.error(ReturnCode.RC400.getCode(), message);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<String> methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("参数类型不匹配: {}={}", e.getName(), e.getValue(), e);
        String message = String.format("参数 '%s' 类型错误，期望类型: %s", e.getName(), e.getRequiredType().getSimpleName());
        return R.error(ReturnCode.RC400.getCode(), message);
    }

    /**
     * 处理JSON解析异常（请求体格式错误）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<String> httpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("请求体格式错误", e);
        return R.error(ReturnCode.RC400.getCode(), "请求体格式错误，请检查JSON格式");
    }

    /**
     * 处理数据绑定异常（表单验证失败）
     */
    @ExceptionHandler(BindException.class)
    public R<String> bindException(BindException e) {
        log.error("数据绑定异常", e);
        List<FieldError> fieldErrors = e.getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(error -> String.format("参数 '%s': %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return R.error(ReturnCode.RC400.getCode(), errorMessage);
    }

    /**
     * 处理空指针的异常
     */
    @ExceptionHandler(value = NullPointerException.class)
    public R<String> nullPointerException(NullPointerException e) {
        log.error("空指针异常 NullPointerException ", e);
        return R.error(ReturnCode.RC500.getCode(), "系统内部错误");
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public R<String> noHandlerFoundException(HttpServletRequest req, Exception e) {
        log.error("404异常 NoHandlerFoundException, method = {}, path = {} ", req.getMethod(), req.getServletPath(), e);
        return R.error(ReturnCode.RC404);
    }

    /**
     * 处理请求方式错误(405)异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<String> httpRequestMethodNotSupportedException(HttpServletRequest req, Exception e) {
        log.error("请求方式错误(405)异常 HttpRequestMethodNotSupportedException, method = {}, path = {}", req.getMethod(), req.getServletPath(), e);
        return R.error(ReturnCode.RC405);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public R<String> exception(Exception e) {
        log.error("未知异常 exception = {}", e.getMessage(), e);
        return R.error(ReturnCode.RC500);
    }
}