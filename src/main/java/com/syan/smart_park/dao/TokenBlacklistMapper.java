package com.syan.smart_park.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syan.smart_park.entity.TokenBlacklist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * Token黑名单数据访问层
 */
@Mapper
public interface TokenBlacklistMapper extends BaseMapper<TokenBlacklist> {
    
    /**
     * 检查token是否在黑名单中
     *
     * @param token token
     * @return 是否在黑名单中
     */
    boolean existsByToken(@Param("token") String token);
    
    /**
     * 根据token查询黑名单记录
     *
     * @param token token
     * @return 黑名单记录
     */
    TokenBlacklist selectByToken(@Param("token") String token);
    
    /**
     * 删除过期token
     *
     * @param expireTime 过期时间
     * @return 删除数量
     */
    int deleteExpiredTokens(@Param("expireTime") LocalDateTime expireTime);
    
    /**
     * 根据用户ID删除token
     *
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteByUserId(@Param("userId") Long userId);
}
