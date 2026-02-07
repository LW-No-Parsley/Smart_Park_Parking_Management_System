package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.entity.Blacklist;
import com.syan.smart_park.entity.BlacklistDTO;

import java.util.List;

/**
 * 黑名单服务接口
 */
public interface BlacklistService extends IService<Blacklist> {
    
    /**
     * 获取所有黑名单记录
     */
    List<BlacklistDTO> getAllBlacklists();
    
    /**
     * 根据ID获取黑名单记录
     */
    BlacklistDTO getBlacklistById(Long id);
    
    /**
     * 创建黑名单记录
     */
    BlacklistDTO createBlacklist(BlacklistDTO blacklistDTO);
    
    /**
     * 更新黑名单记录
     */
    BlacklistDTO updateBlacklist(Long id, BlacklistDTO blacklistDTO);
    
    /**
     * 删除黑名单记录
     */
    boolean deleteBlacklist(Long id);
    
    /**
     * 根据车牌号查询黑名单记录
     */
    List<BlacklistDTO> getBlacklistsByPlateNumber(String plateNumber);
    
    /**
     * 根据状态查询黑名单记录
     */
    List<BlacklistDTO> getBlacklistsByStatus(Integer status);
    
    /**
     * 检查车牌号是否在黑名单中
     */
    boolean isPlateNumberInBlacklist(String plateNumber);
    
    /**
     * 获取当前生效的黑名单记录
     */
    List<BlacklistDTO> getActiveBlacklists();
    
    /**
     * 获取已过期的黑名单记录
     */
    List<BlacklistDTO> getExpiredBlacklists();
    
    /**
     * 批量更新黑名单状态
     */
    boolean batchUpdateStatus(List<Long> ids, Integer status);
    
    /**
     * 根据创建人查询黑名单记录
     */
    List<BlacklistDTO> getBlacklistsByCreatedBy(Long createdBy);
    
    /**
     * 搜索黑名单记录
     */
    List<BlacklistDTO> searchBlacklists(String keyword);
}
