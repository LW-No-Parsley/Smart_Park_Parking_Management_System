package com.smart_park_parking_management_system.controller.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart_park_parking_management_system.common.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 拦截controller返回值，封装后统一返回格式
 */
@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 更精确的匹配条件，避免对某些特定类型进行处理
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // 如果返回的结果已经是R对象，直接返回
        if (body instanceof R) {
            return body;
        }

        // 处理void返回类型（通常是Controller方法返回void）
        if (body == null && returnType.getParameterType().equals(void.class)) {
            return R.success(null);
        }

        // 如果Controller返回String，需要手动转换成json
        if (body instanceof String) {
            try {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return objectMapper.writeValueAsString(R.success(body));
            } catch (Exception e) {
                throw new RuntimeException("JSON转换失败", e);
            }
        }

        // 其他情况统一封装为成功响应
        return R.success(body);
    }
}