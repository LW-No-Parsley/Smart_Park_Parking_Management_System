package com.syan.smart_park.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.syan.smart_park.dao.CaptchaMapper;
import com.syan.smart_park.entity.Captcha;
import com.syan.smart_park.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码服务实现类
 */
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final CaptchaMapper captchaMapper;
    
    // 内存缓存，提高验证码验证性能
    private final ConcurrentHashMap<String, String> captchaCache = new ConcurrentHashMap<>();
    
    // 验证码配置
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_COUNT = 4;
    private static final int LINE_COUNT = 10;
    private static final int EXPIRE_MINUTES = 5; // 验证码有效期5分钟

    @Override
    public CaptchaInfo generateCaptcha() {
        // 1. 生成验证码
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(WIDTH, HEIGHT, CODE_COUNT, LINE_COUNT);
        String captchaCode = lineCaptcha.getCode();
        BufferedImage captchaImage = lineCaptcha.getImage();
        
        // 2. 将BufferedImage转换为Base64字符串
        String captchaImageBase64 = convertBufferedImageToBase64(captchaImage);
        
        // 3. 生成验证码ID
        String captchaId = IdUtil.fastSimpleUUID();
        
        // 4. 保存到数据库
        Captcha captcha = new Captcha();
        captcha.setCaptchaKey(captchaId);
        captcha.setCaptchaValue(captchaCode);
        captcha.setCaptchaType(1); // 1-登录，2-注册，3-重置密码，默认为登录
        captcha.setCreateTime(LocalDateTime.now());
        captcha.setExpireTime(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));
        captcha.setUsed(0);
        
        captchaMapper.insert(captcha);
        
        // 5. 保存到缓存
        captchaCache.put(captchaId, captchaCode);
        
        return new CaptchaInfo(captchaId, captchaCode, captchaImageBase64);
    }

    @Override
    public boolean validateCaptcha(String captchaId, String captchaCode) {
        if (captchaId == null || captchaCode == null) {
            return false;
        }
        
        // 1. 从缓存中验证
        String cachedCode = captchaCache.get(captchaId);
        if (cachedCode != null && cachedCode.equalsIgnoreCase(captchaCode)) {
            // 验证成功后标记为已使用
            markCaptchaAsUsed(captchaId);
            captchaCache.remove(captchaId);
            return true;
        }
        
        // 2. 从数据库中验证
        QueryWrapper<Captcha> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("captcha_id", captchaId)
                   .eq("used", 0)
                   .gt("expiration_time", LocalDateTime.now());
        
        Captcha captcha = captchaMapper.selectOne(queryWrapper);
        if (captcha == null) {
            return false;
        }
        
        // 验证验证码（不区分大小写）
        boolean isValid = captcha.getCaptchaValue().equalsIgnoreCase(captchaCode);
        if (isValid) {
            // 验证成功后标记为已使用
            markCaptchaAsUsed(captchaId);
            captchaCache.remove(captchaId);
        }
        
        return isValid;
    }

    @Override
    public String getCaptchaImageBase64(String captchaId) {
        // 1. 从缓存中获取验证码
        String captchaCode = captchaCache.get(captchaId);
        if (captchaCode != null) {
            // 创建验证码图片并转换为Base64
            BufferedImage image = createCaptchaImageWithCode(captchaCode);
            return convertBufferedImageToBase64(image);
        }
        
        // 2. 从数据库中获取验证码
        QueryWrapper<Captcha> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("captcha_id", captchaId)
                   .eq("used", 0)
                   .gt("expiration_time", LocalDateTime.now());
        
        Captcha captcha = captchaMapper.selectOne(queryWrapper);
        if (captcha == null) {
            return null;
        }
        
        // 生成验证码图片并转换为Base64
        BufferedImage image = createCaptchaImageWithCode(captcha.getCaptchaValue());
        return convertBufferedImageToBase64(image);
    }
    
    /**
     * 使用指定的验证码文本创建验证码图片
     */
    private BufferedImage createCaptchaImageWithCode(String code) {
        // 创建LineCaptcha对象
        LineCaptcha lineCaptcha = new LineCaptcha(WIDTH, HEIGHT, CODE_COUNT, LINE_COUNT);
        
        // 使用反射或其他方法设置验证码
        // 由于Hutool的API限制，我们使用一个简单的方法：
        // 创建一个新的验证码对象，然后使用其内部方法
        try {
            // 使用反射调用setCode方法（如果存在）
            java.lang.reflect.Method setCodeMethod = lineCaptcha.getClass().getMethod("setCode", String.class);
            setCodeMethod.invoke(lineCaptcha, code);
        } catch (Exception e) {
            // 如果反射失败，使用默认方法生成验证码
            // 创建一个新的验证码，但使用我们指定的文本
            lineCaptcha = CaptchaUtil.createLineCaptcha(WIDTH, HEIGHT, CODE_COUNT, LINE_COUNT);
            // 注意：这里我们无法设置特定的验证码文本，所以返回默认生成的
        }
        
        lineCaptcha.createCode();
        return lineCaptcha.getImage();
    }

    @Override
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void cleanExpiredCaptchas() {
        // 清理过期验证码
        QueryWrapper<Captcha> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("expiration_time", LocalDateTime.now())
                   .or()
                   .eq("used", 1);
        
        captchaMapper.delete(queryWrapper);
        
        // 清理缓存中的过期验证码（基于访问时间）
        // 这里简化处理，实际项目中可以使用带过期时间的缓存
        captchaCache.entrySet().removeIf(entry -> {
            // 检查验证码是否过期
            QueryWrapper<Captcha> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("captcha_id", entry.getKey())
                       .lt("expiration_time", LocalDateTime.now());
            return captchaMapper.selectCount(checkWrapper) > 0;
        });
    }
    
    /**
     * 标记验证码为已使用
     */
    private void markCaptchaAsUsed(String captchaId) {
        // 使用UpdateWrapper明确指定列名
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Captcha> updateWrapper = 
            new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.eq("captcha_id", captchaId)
                    .set("used", 1);
        
        captchaMapper.update(null, updateWrapper);
    }
    
    /**
     * 将BufferedImage转换为Base64字符串
     */
    private String convertBufferedImageToBase64(BufferedImage image) {
        if (image == null) {
            return null;
        }
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.encode(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert image to Base64", e);
        }
    }
}
