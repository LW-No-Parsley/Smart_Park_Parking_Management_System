package com.syan.smart_park.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.entity.FeeCalculationResult;
import com.syan.smart_park.entity.FeeRule;
import com.syan.smart_park.entity.FeeRuleDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 停车计费规则服务接口
 */
public interface FeeRuleService extends IService<FeeRule> {

    /**
     * 统一查询计费规则列表（支持多条件筛选 + 分页）
     *
     * @param parkAreaId 园区ID（可选）
     * @param status     状态：0-禁用，1-启用（可选）
     * @param page       页码
     * @param size       每页大小
     */
    PageResult<FeeRuleDTO> listFeeRules(Long parkAreaId, Integer status, Integer page, Integer size);

    /**
     * 根据ID获取计费规则详情
     */
    FeeRuleDTO getFeeRuleById(Long id);

    /**
     * 创建计费规则
     */
    FeeRuleDTO createFeeRule(FeeRuleDTO feeRuleDTO);

    /**
     * 更新计费规则
     */
    FeeRuleDTO updateFeeRule(Long id, FeeRuleDTO feeRuleDTO);

    /**
     * 删除计费规则
     */
    boolean deleteFeeRule(Long id);

    /**
     * 计算停车费用
     *
     * @param parkAreaId  园区ID（用于查找匹配的计费规则）
     * @param vehicleType 车辆类型（1-小型车，2-大型车）
     * @param startTime   停车开始时间
     * @param endTime     停车结束时间
     * @return 总费用（元）
     */
    BigDecimal calculateFee(Long parkAreaId, Integer vehicleType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 计算停车费用（带明细）
     *
     * @param parkAreaId  园区ID
     * @param vehicleType 车辆类型
     * @param startTime   停车开始时间
     * @param endTime     停车结束时间
     * @return 费用计算结果（含明细）
     */
    FeeCalculationResult calculateFeeWithDetail(Long parkAreaId, Integer vehicleType, LocalDateTime startTime, LocalDateTime endTime);
}
