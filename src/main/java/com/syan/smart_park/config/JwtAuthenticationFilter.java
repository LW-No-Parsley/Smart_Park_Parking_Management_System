package com.syan.smart_park.config;

import com.syan.smart_park.common.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 获取Authorization header
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 提取token
        final String jwt = authHeader.substring(7);
        
        // 获取请求URI，用于判断是否为刷新接口
        String requestUri = request.getRequestURI();
        
        try {
            // 验证token
            if (jwtUtil.validateToken(jwt)) {
                // 检查token类型：refreshToken只能用于刷新接口，不能用于普通API请求
                if (jwtUtil.isRefreshToken(jwt)) {
                    // 只有刷新接口允许使用refreshToken
                    if (!requestUri.endsWith("/refresh")) {
                        // refreshToken用于普通请求，拒绝访问
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":401,\"status\":false,\"message\":\"未授权\",\"timestamp\":" + System.currentTimeMillis() + "}");
                        return;
                    }
                }
                
                // 从token中获取用户名
                String username = jwtUtil.getUsernameFromToken(jwt);
                Long userId = jwtUtil.getUserIdFromToken(jwt);
                
                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userId, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 设置认证信息到SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // token无效（包括过期或被拉黑）
                // 不设置认证信息，让JwtAuthenticationEntryPoint处理
            }
        } catch (Exception e) {
            // token验证失败（格式错误、签名无效等）
            // 不设置认证信息，让JwtAuthenticationEntryPoint处理
            logger.error("JWT token验证失败: " + e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
}
