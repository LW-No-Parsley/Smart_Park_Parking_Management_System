package com.syan.smart_park.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 微信登录凭证校验服务
 * 调用微信 code2session 接口，用临时 code 换取真实 openid
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatService {

    private final RestTemplate restTemplate;

    @Value("${wechat.miniapp.app-id:}")
    private String miniAppId;

    @Value("${wechat.miniapp.app-secret:}")
    private String miniAppSecret;

    @Value("${wechat.miniapp.login-url:}")
    private String miniAppLoginUrl;

    /**
     * 通过临时 code 获取微信 openid
     *
     * @param code  微信临时登录凭证
     * @return openid，验证失败返回 null
     */
    public String getOpenidByCode(String code) {
        if (code == null || code.isBlank()) {
            log.warn("code 为空，无法通过微信验证");
            return null;
        }

        if (miniAppId.isEmpty() || miniAppSecret.isEmpty()) {
            log.warn("微信小程序配置不完整（app-id 或 app-secret 为空），跳过 code2session 验证");
            return null;
        }

        String url = String.format(miniAppLoginUrl, miniAppId, miniAppSecret, code);
        try {
            // 先用 String 接收，避免微信返回非标准 Content-Type 导致转换失败
            String respBody = restTemplate.getForObject(url, String.class);
            if (respBody == null) {
                log.error("code2session 响应为空");
                return null;
            }
            log.debug("code2session 原始响应: {}", respBody);

            ObjectMapper mapper = new ObjectMapper();
            WechatSessionResponse resp = mapper.readValue(respBody, WechatSessionResponse.class);

            if (resp.getErrcode() != null && resp.getErrcode() != 0) {
                log.error("code2session 失败，errcode: {}, errmsg: {}", resp.getErrcode(), resp.getErrmsg());
                return null;
            }
            if (resp.getOpenid() == null) {
                log.error("code2session 响应中缺少 openid");
                return null;
            }
            log.info("code2session 成功，openid: {}", resp.getOpenid());
            return resp.getOpenid();
        } catch (Exception e) {
            log.error("调用 code2session 接口失败", e);
            return null;
        }
    }

    @Data
    public static class WechatSessionResponse {
        private String openid;
        private String session_key;
        private String unionid;

        @JsonProperty("errcode")
        private Integer errcode;

        @JsonProperty("errmsg")
        private String errmsg;
    }
}
