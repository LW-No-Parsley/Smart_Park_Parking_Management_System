package com.syan.smart_park.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT认证入口点
 * 用于处理未认证的请求，返回统一的JSON响应
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        // 设置响应内容类型为JSON
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // 创建统一的响应对象
        R<Object> errorResponse = R.error(ReturnCode.RC401, "未授权，请先登录");
        
        // 将响应对象转换为JSON并写入响应
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}
