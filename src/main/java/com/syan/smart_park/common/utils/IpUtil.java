package com.syan.smart_park.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.service.Config;
import org.lionsoul.ip2region.service.Ip2Region;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP工具类
 * 用于获取客户端IP地址和地理位置信息
 * 使用ip2region库进行IP地理位置查询
 */
@Slf4j
public class IpUtil {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String SEPARATOR = ",";
    
    // ip2region查询服务实例
    private static Ip2Region ip2Region = null;
    
    // ip2region xdb文件路径（相对于classpath）
    private static final String IP2REGION_V4_XDB_PATH = "ip2region/ip2region_v4.xdb";
    private static final String IP2REGION_V6_XDB_PATH = "ip2region/ip2region_v6.xdb";
    
    // 初始化ip2region查询服务
    static {
        initIp2Region();
    }
    
    /**
     * 初始化ip2region查询服务
     */
    private static void initIp2Region() {
        try {
            // 获取classpath下的xdb文件路径
            ClassLoader classLoader = IpUtil.class.getClassLoader();
            java.net.URL v4Url = classLoader.getResource(IP2REGION_V4_XDB_PATH);
            java.net.URL v6Url = classLoader.getResource(IP2REGION_V6_XDB_PATH);
            
            if (v4Url == null) {
                log.error("找不到ip2region_v4.xdb文件: {}", IP2REGION_V4_XDB_PATH);
                ip2Region = null;
                return;
            }
            
            if (v6Url == null) {
                log.error("找不到ip2region_v6.xdb文件: {}", IP2REGION_V6_XDB_PATH);
                ip2Region = null;
                return;
            }
            
            String v4Path = v4Url.getPath();
            String v6Path = v6Url.getPath();
            
            // 处理Windows路径中的特殊字符（如空格）
            v4Path = v4Path.replace("%20", " ");
            v6Path = v6Path.replace("%20", " ");
            
            log.info("加载ip2region_v4.xdb文件: {}", v4Path);
            log.info("加载ip2region_v6.xdb文件: {}", v6Path);
            
            // 创建v4配置
            final Config v4Config = Config.custom()
                .setCachePolicy(Config.VIndexCache)     // 使用VectorIndex缓存策略
                .setSearchers(10)                       // 设置查询器数量
                .setXdbPath(v4Path)                     // 设置v4 xdb文件绝对路径
                .asV4();                                // 指定为v4配置
            
            // 创建v6配置
            final Config v6Config = Config.custom()
                .setCachePolicy(Config.VIndexCache)     // 使用VectorIndex缓存策略
                .setSearchers(10)                       // 设置查询器数量
                .setXdbPath(v6Path)                     // 设置v6 xdb文件绝对路径
                .asV6();                                // 指定为v6配置
            
            // 创建Ip2Region查询服务
            ip2Region = Ip2Region.create(v4Config, v6Config);
            log.info("ip2region查询服务初始化成功");
        } catch (Exception e) {
            log.error("ip2region查询服务初始化失败", e);
            ip2Region = null;
        }
    }
    
    /**
     * 关闭ip2region查询服务
     * 在应用关闭时调用
     */
    public static void closeIp2Region() {
        if (ip2Region != null) {
            try {
                ip2Region.close();
                log.info("ip2region查询服务已关闭");
            } catch (Exception e) {
                log.error("关闭ip2region查询服务时出错", e);
            }
        }
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HttpServletRequest
     * @return IP地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        
        String ip = request.getHeader("x-forwarded-for");
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (LOCALHOST_IP.equals(ip) || LOCALHOST_IPV6.equals(ip)) {
                // 根据网卡取本机配置的IP
                try {
                    InetAddress inetAddress = InetAddress.getLocalHost();
                    ip = inetAddress.getHostAddress();
                } catch (UnknownHostException e) {
                    log.error("获取本机IP地址失败", e);
                }
            }
        }
        
        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (StringUtils.hasText(ip) && ip.contains(SEPARATOR)) {
            ip = ip.substring(1, ip.indexOf(SEPARATOR));
        }
        
        return ip;
    }

    /**
     * 根据IP地址获取地理位置
     * 使用ip2region库进行精确的地理位置查询
     *
     * @param ip IP地址
     * @return 地理位置信息
     */
    public static String getLocationByIp(String ip) {
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            return "未知";
        }
        
        // 本地IP
        if (LOCALHOST_IP.equals(ip) || LOCALHOST_IPV6.equals(ip)) {
            return "本地";
        }
        
        // 内网IP
        if (isInternalIp(ip)) {
            return "内网";
        }
        
        // 使用ip2region库查询地理位置
        if (ip2Region != null) {
            try {
                String region = ip2Region.search(ip);
                if (StringUtils.hasText(region)) {
                    // ip2region返回的格式通常是：国家|区域|省份|城市|ISP
                    // 例如：中国|0|广东省|深圳市|电信
                    // 我们提取有用的信息
                    String[] parts = region.split("\\|");
                    if (parts.length >= 5) {
                        String country = parts[0];
                        String province = parts[2];
                        String city = parts[3];
                        String isp = parts[4];
                        
                        // 构建友好的地理位置信息
                        StringBuilder location = new StringBuilder();
                        if (!"0".equals(country) && StringUtils.hasText(country)) {
                            location.append(country);
                        }
                        if (!"0".equals(province) && StringUtils.hasText(province)) {
                            if (location.length() > 0) location.append(" ");
                            location.append(province);
                        }
                        if (!"0".equals(city) && StringUtils.hasText(city)) {
                            if (location.length() > 0) location.append(" ");
                            location.append(city);
                        }
                        if (!"0".equals(isp) && StringUtils.hasText(isp)) {
                            if (location.length() > 0) location.append(" ");
                            location.append(isp);
                        }
                        
                        return location.toString();
                    } else if (parts.length > 0) {
                        // 如果格式不符合预期，返回原始信息
                        return region.replace("|", " ");
                    }
                }
            } catch (Exception e) {
                log.error("使用ip2region查询IP地理位置失败: {}", ip, e);
                // 查询失败时返回"未知"
            }
        } else {
            log.warn("ip2region查询服务未初始化，无法查询IP地理位置: {}", ip);
        }
        
        return "未知";
    }
    
    /**
     * 根据IP地址获取详细的地理位置信息（包含国家、省份、城市、ISP）
     *
     * @param ip IP地址
     * @return 包含国家、省份、城市、ISP的数组，如果查询失败返回null
     */
    public static String[] getLocationDetailsByIp(String ip) {
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip) || 
            LOCALHOST_IP.equals(ip) || LOCALHOST_IPV6.equals(ip) || isInternalIp(ip)) {
            return null;
        }
        
        if (ip2Region != null) {
            try {
                String region = ip2Region.search(ip);
                if (StringUtils.hasText(region)) {
                    String[] parts = region.split("\\|");
                    if (parts.length >= 5) {
                        return new String[] {
                            "0".equals(parts[0]) ? "" : parts[0],  // 国家
                            "0".equals(parts[2]) ? "" : parts[2],  // 省份
                            "0".equals(parts[3]) ? "" : parts[3],  // 城市
                            "0".equals(parts[4]) ? "" : parts[4]   // ISP
                        };
                    }
                }
            } catch (Exception e) {
                log.error("使用ip2region查询IP详细地理位置失败: {}", ip, e);
            }
        }
        
        return null;
    }

    /**
     * 判断是否为内网IP
     *
     * @param ip IP地址
     * @return 是否为内网IP
     */
    public static boolean isInternalIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }
        
        return ip.startsWith("192.168.") || 
               ip.startsWith("10.") || 
               ip.startsWith("172.") ||
               LOCALHOST_IP.equals(ip) || 
               LOCALHOST_IPV6.equals(ip);
    }
}
