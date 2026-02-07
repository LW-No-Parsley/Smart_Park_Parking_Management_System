package com.syan.smart_park.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()  // 允许所有请求，无需认证
                )
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
