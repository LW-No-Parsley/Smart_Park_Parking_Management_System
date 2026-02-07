package com.syan.smart_park.controller.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syan.smart_park.common.R;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

/**
 * 拦截controller返回值，封装后统一返回格式
 */
@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    public ResponseAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 排除ResourceController的响应，避免包装文件资源
        if (returnType.getContainingClass() != null && 
            returnType.getContainingClass().getName().contains("ResourceController")) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType, Class selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // 如果返回的结果已经是R对象，直接返回
        if (body instanceof R) {
            return body;
        }

        // 处理void返回类型 - 创建无数据的成功响应
        if (body == null && returnType.getParameterType() == void.class) {
            return R.success(null); // 传入null作为数据
        }

        // 处理String类型返回
        if (body instanceof String) {
            try {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return objectMapper.writeValueAsString(R.success(body));
            } catch (Exception e) {
                throw new RuntimeException("JSON转换异常", e);
            }
        }

        // 排除特定类型的响应（如文件下载）
        if (isExcludedResponse(body, selectedContentType)) {
            return body;
        }

        // 默认成功包装
        return R.success(body);
    }

    /**
     * 判断是否排除响应包装
     */
    private boolean isExcludedResponse(Object body, MediaType mediaType) {
        // 排除文件流响应
        if (body instanceof ResponseEntity ||
                body instanceof Resource ||
                (mediaType != null && mediaType.includes(MediaType.APPLICATION_OCTET_STREAM))) {
            return true;
        }

        // 检查ResponseEntity的内容类型
        if (body instanceof ResponseEntity) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) body;
            Object responseBody = responseEntity.getBody();
            if (responseBody instanceof Resource) {
                return true;
            }
            // 检查响应头中的Content-Type
            if (responseEntity.getHeaders().getContentType() != null &&
                    !responseEntity.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON)) {
                return true;
            }
            // 如果ResponseEntity的body是null，也排除包装（如404响应）
            if (responseBody == null) {
                return true;
            }
        }

        // 排除非JSON响应
        return mediaType != null && !mediaType.includes(MediaType.APPLICATION_JSON);
    }
}
