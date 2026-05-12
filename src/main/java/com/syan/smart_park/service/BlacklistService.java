package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.entity.Blacklist;
import com.syan.smart_park.entity.BlacklistDTO;

import java.util.List;

/**
 * 黑名单服务接口
 */
public interface BlacklistService extends IService<Blacklist> {
    
    /**
     * 统一查询黑名单列表（支持多条件筛选 + 分页）
     *
     * @param plateNumber 车牌号（可选，精确匹配）
     * @param status      状态：0-禁用，1-生效（可选）
     * @param createdBy   创建人ID（可选）
     * @param keyword     搜索关键词，按车牌号/原因模糊搜索（可选）
     * @param expired     是否已过期（可选）
     * @param page        页码
     * @param size        每页大小
     */
    PageResult<BlacklistDTO> listBlacklists(String plateNumber, Integer status, Long createdBy,
                                            String keyword, Boolean expired, Integer page, Integer size);
    
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
     * 检查车牌号是否在黑名单中
     */
    boolean isPlateNumberInBlacklist(String plateNumber);
    
    /**
     * 批量更新黑名单状态
     */
    boolean batchUpdateStatus(List<Long> ids, Integer status);
}
