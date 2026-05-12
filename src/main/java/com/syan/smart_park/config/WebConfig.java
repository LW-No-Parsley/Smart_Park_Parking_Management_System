package com.syan.smart_park.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Web MVC 全局配置
 * 处理日期时间格式转换等
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToLocalDateTimeConverter());
    }

    /**
     * 自定义 String -> LocalDateTime 转换器
     * 支持以下格式：
     * - ISO 本地时间: 2026-05-11T16:00:00
     * - ISO UTC 时间: 2026-05-11T16:00:00.000Z (去掉 Z 后解析)
     * - 标准格式: 2026-05-11 16:00:00
     */
    public static class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

        @Override
        public LocalDateTime convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }

            String value = source.trim();

            // 处理带 Z (UTC) 后缀的格式: 2026-05-11T16:00:00.000Z
            if (value.endsWith("Z") || value.endsWith("z")) {
                value = value.substring(0, value.length() - 1);
            }

            // 处理空格分隔格式: 2026-05-11 16:00:00
            if (value.contains(" ") && !value.contains("T")) {
                value = value.replace(" ", "T");
            }

            // 尝试解析
            String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd"
            };

            for (String pattern : patterns) {
                try {
                    return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(pattern));
                } catch (DateTimeParseException ignored) {
                }
            }

            // 最后尝试使用 ISO_LOCAL_DATE_TIME 解析
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                    "无法解析日期时间: '" + source + "', 支持的格式: yyyy-MM-dd'T'HH:mm:ss, yyyy-MM-dd HH:mm:ss 等", e);
            }
        }
    }
}
