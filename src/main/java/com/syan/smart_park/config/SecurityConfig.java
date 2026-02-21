package com.syan.smart_park.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // 公开接口（无需认证）
                        .requestMatchers("/api/auth/captcha").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/forgot-password").permitAll()
                        .requestMatchers("/api/auth/reset-password").permitAll()
                        .requestMatchers("/api/auth/refresh").permitAll()
                        .requestMatchers("/api/auth/validate").permitAll()
                        
                        // 静态资源
                        .requestMatchers("/public/**").permitAll()
                        
                        // 其他所有接口都需要认证
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 自定义认证失败处理
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // 无状态会话
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form
                        .disable()  // 禁用登录页面
                )
                .logout(logout -> logout
                        .disable()  // 禁用登出
                )
                .csrf(csrf -> csrf
                        .disable()  // 禁用 CSRF 保护
                )
                .httpBasic(httpBasic -> httpBasic
                        .disable()  // 禁用 HTTP Basic 认证
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame
                                .disable()  // 允许嵌入iframe（可选）
                        )
                );

        return http.build();
    }
}
