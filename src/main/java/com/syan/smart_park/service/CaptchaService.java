package com.syan.smart_park.service;

import java.awt.image.BufferedImage;

/**
 * 验证码服务接口
 */
public interface CaptchaService {
    
    /**
     * 生成验证码
     *
     * @return 验证码信息，包含ID和Base64编码的图片
     */
    CaptchaInfo generateCaptcha();
    
    /**
     * 验证验证码
     *
     * @param captchaId 验证码ID
     * @param captchaCode 验证码
     * @return 是否验证成功
     */
    boolean validateCaptcha(String captchaId, String captchaCode);
    
    /**
     * 获取验证码图片Base64字符串
     *
     * @param captchaId 验证码ID
     * @return 验证码图片Base64字符串
     */
    String getCaptchaImageBase64(String captchaId);
    
    /**
     * 清理过期验证码
     */
    void cleanExpiredCaptchas();
    
    /**
     * 验证码信息类
     */
    class CaptchaInfo {
        private String captchaId;
        private String captchaCode;
        private String captchaImageBase64;
        
        public CaptchaInfo(String captchaId, String captchaCode, String captchaImageBase64) {
            this.captchaId = captchaId;
            this.captchaCode = captchaCode;
            this.captchaImageBase64 = captchaImageBase64;
        }
        
        public String getCaptchaId() {
            return captchaId;
        }
        
        public String getCaptchaCode() {
            return captchaCode;
        }
        
        public String getCaptchaImageBase64() {
            return captchaImageBase64;
        }
    }
}
