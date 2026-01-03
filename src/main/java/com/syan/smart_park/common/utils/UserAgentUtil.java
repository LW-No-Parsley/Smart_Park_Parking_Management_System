package com.syan.smart_park.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User-Agent解析工具类
 * 用于解析HTTP请求中的User-Agent头信息，获取浏览器和操作系统信息
 */
@Slf4j
public class UserAgentUtil {

    private static final String UNKNOWN = "unknown";
    
    // 浏览器正则表达式
    private static final Pattern CHROME_PATTERN = Pattern.compile("Chrome/([0-9.]+)");
    private static final Pattern FIREFOX_PATTERN = Pattern.compile("Firefox/([0-9.]+)");
    private static final Pattern SAFARI_PATTERN = Pattern.compile("Version/([0-9.]+).*Safari");
    private static final Pattern EDGE_PATTERN = Pattern.compile("Edg?e?/([0-9.]+)");
    private static final Pattern OPERA_PATTERN = Pattern.compile("OPR/([0-9.]+)");
    private static final Pattern IE_PATTERN = Pattern.compile("(?:MSIE |rv:)([0-9.]+)");
    
    // 操作系统正则表达式
    private static final Pattern WINDOWS_PATTERN = Pattern.compile("Windows NT ([0-9.]+)");
    private static final Pattern MAC_PATTERN = Pattern.compile("Mac OS X ([0-9._]+)");
    private static final Pattern LINUX_PATTERN = Pattern.compile("Linux");
    private static final Pattern ANDROID_PATTERN = Pattern.compile("Android ([0-9.]+)");
    private static final Pattern IOS_PATTERN = Pattern.compile("(?:iPhone|iPad|iPod).*? OS ([0-9_]+)");

    /**
     * 获取浏览器信息
     *
     * @param userAgent User-Agent字符串
     * @return 浏览器信息
     */
    public static String getBrowser(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return UNKNOWN;
        }

        // 检查Edge（必须在Chrome之前检查，因为Edge的User-Agent包含"Edg/"和"Chrome/"）
        Matcher edgeMatcher = EDGE_PATTERN.matcher(userAgent);
        if (edgeMatcher.find()) {
            return "Edge " + edgeMatcher.group(1);
        }

        // 检查Chrome
        Matcher chromeMatcher = CHROME_PATTERN.matcher(userAgent);
        if (chromeMatcher.find()) {
            return "Chrome " + chromeMatcher.group(1);
        }

        // 检查Firefox
        Matcher firefoxMatcher = FIREFOX_PATTERN.matcher(userAgent);
        if (firefoxMatcher.find()) {
            return "Firefox " + firefoxMatcher.group(1);
        }

        // 检查Safari
        Matcher safariMatcher = SAFARI_PATTERN.matcher(userAgent);
        if (safariMatcher.find()) {
            return "Safari " + safariMatcher.group(1);
        }

        // 检查Opera
        Matcher operaMatcher = OPERA_PATTERN.matcher(userAgent);
        if (operaMatcher.find()) {
            return "Opera " + operaMatcher.group(1);
        }

        // 检查IE
        Matcher ieMatcher = IE_PATTERN.matcher(userAgent);
        if (ieMatcher.find()) {
            return "IE " + ieMatcher.group(1);
        }

        return UNKNOWN;
    }

    /**
     * 获取操作系统信息
     *
     * @param userAgent User-Agent字符串
     * @return 操作系统信息
     */
    public static String getOs(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return UNKNOWN;
        }

        // 检查Windows
        Matcher windowsMatcher = WINDOWS_PATTERN.matcher(userAgent);
        if (windowsMatcher.find()) {
            String version = windowsMatcher.group(1);
            switch (version) {
                case "10.0":
                    return "Windows 10";
                case "6.3":
                    return "Windows 8.1";
                case "6.2":
                    return "Windows 8";
                case "6.1":
                    return "Windows 7";
                case "6.0":
                    return "Windows Vista";
                case "5.1":
                case "5.2":
                    return "Windows XP";
                default:
                    return "Windows " + version;
            }
        }

        // 检查Mac OS
        Matcher macMatcher = MAC_PATTERN.matcher(userAgent);
        if (macMatcher.find()) {
            return "Mac OS X " + macMatcher.group(1).replace("_", ".");
        }

        // 检查Android
        Matcher androidMatcher = ANDROID_PATTERN.matcher(userAgent);
        if (androidMatcher.find()) {
            return "Android " + androidMatcher.group(1);
        }

        // 检查iOS
        Matcher iosMatcher = IOS_PATTERN.matcher(userAgent);
        if (iosMatcher.find()) {
            return "iOS " + iosMatcher.group(1).replace("_", ".");
        }

        // 检查Linux
        Matcher linuxMatcher = LINUX_PATTERN.matcher(userAgent);
        if (linuxMatcher.find()) {
            return "Linux";
        }

        return UNKNOWN;
    }

    /**
     * 从HttpServletRequest中获取浏览器信息
     *
     * @param request HttpServletRequest
     * @return 浏览器信息
     */
    public static String getBrowser(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        String userAgent = request.getHeader("User-Agent");
        return getBrowser(userAgent);
    }

    /**
     * 从HttpServletRequest中获取操作系统信息
     *
     * @param request HttpServletRequest
     * @return 操作系统信息
     */
    public static String getOs(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        String userAgent = request.getHeader("User-Agent");
        return getOs(userAgent);
    }

    /**
     * 从HttpServletRequest中获取完整的User-Agent信息
     *
     * @param request HttpServletRequest
     * @return User-Agent字符串
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "";
    }
}
