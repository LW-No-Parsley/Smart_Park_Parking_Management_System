package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syan.smart_park.common.exception.BusinessException;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.dao.FeeRuleMapper;
import com.syan.smart_park.entity.FeeCalculationResult;
import com.syan.smart_park.entity.FeeRule;
import com.syan.smart_park.entity.FeeRuleDTO;
import com.syan.smart_park.service.FeeRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 停车计费规则服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeeRuleServiceImpl extends ServiceImpl<FeeRuleMapper, FeeRule> implements FeeRuleService {

    private final FeeRuleMapper feeRuleMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<FeeRuleDTO> getAllFeeRules() {
        List<FeeRule> feeRules = this.list();
        return feeRules.stream()
                .map(FeeRuleDTO::fromFeeRule)
                .collect(Collectors.toList());
    }

    @Override
    public FeeRuleDTO getFeeRuleById(Long id) {
        FeeRule feeRule = this.getById(id);
        return FeeRuleDTO.fromFeeRule(feeRule);
    }

    @Override
    @Transactional
    public FeeRuleDTO createFeeRule(FeeRuleDTO feeRuleDTO) {
        FeeRule feeRule = feeRuleDTO.toFeeRule();
        this.save(feeRule);
        return FeeRuleDTO.fromFeeRule(feeRule);
    }

    @Override
    @Transactional
    public FeeRuleDTO updateFeeRule(Long id, FeeRuleDTO feeRuleDTO) {
        FeeRule existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(ReturnCode.RC1300, "计费规则不存在");
        }

        FeeRule feeRule = feeRuleDTO.toFeeRule();
        feeRule.setId(id);
        this.updateById(feeRule);
        return FeeRuleDTO.fromFeeRule(this.getById(id));
    }

    @Override
    @Transactional
    public boolean deleteFeeRule(Long id) {
        return this.removeById(id);
    }

    @Override
    public List<FeeRuleDTO> getFeeRulesByParkAreaId(Long parkAreaId) {
        LambdaQueryWrapper<FeeRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FeeRule::getParkAreaId, parkAreaId)
                   .orderByAsc(FeeRule::getSortOrder);
        List<FeeRule> feeRules = this.list(queryWrapper);
        return feeRules.stream()
                .map(FeeRuleDTO::fromFeeRule)
                .collect(Collectors.toList());
    }

    @Override
    public List<FeeRuleDTO> getFeeRulesByStatus(Integer status) {
        LambdaQueryWrapper<FeeRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FeeRule::getStatus, status)
                   .orderByAsc(FeeRule::getSortOrder);
        List<FeeRule> feeRules = this.list(queryWrapper);
        return feeRules.stream()
                .map(FeeRuleDTO::fromFeeRule)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateFee(Long parkAreaId, Integer vehicleType, LocalDateTime startTime, LocalDateTime endTime) {
        FeeCalculationResult result = calculateFeeWithDetail(parkAreaId, vehicleType, startTime, endTime);
        return result.getTotalFee();
    }

    @Override
    public FeeCalculationResult calculateFeeWithDetail(Long parkAreaId, Integer vehicleType, LocalDateTime startTime, LocalDateTime endTime) {
        // 1. 查找匹配的计费规则
        FeeRule rule = findMatchingRule(parkAreaId, vehicleType);
        if (rule == null) {
            log.warn("未找到匹配的计费规则，parkAreaId={}, vehicleType={}", parkAreaId, vehicleType);
            FeeCalculationResult emptyResult = new FeeCalculationResult();
            emptyResult.setTotalFee(BigDecimal.ZERO);
            emptyResult.setTotalMinutes(Duration.between(startTime, endTime).toMinutes());
            emptyResult.setChargeableMinutes(0L);
            emptyResult.setFreeMinutes(0);
            emptyResult.setStartTime(startTime);
            emptyResult.setEndTime(endTime);
            emptyResult.setDetails(new ArrayList<>());
            return emptyResult;
        }

        // 2. 计算总停车分钟数
        long totalMinutes = Duration.between(startTime, endTime).toMinutes();
        if (totalMinutes <= 0) {
            FeeCalculationResult zeroResult = new FeeCalculationResult();
            zeroResult.setTotalFee(BigDecimal.ZERO);
            zeroResult.setTotalMinutes(0L);
            zeroResult.setChargeableMinutes(0L);
            zeroResult.setFreeMinutes(rule.getFreeMinutes() != null ? rule.getFreeMinutes() : 0);
            zeroResult.setStartTime(startTime);
            zeroResult.setEndTime(endTime);
            zeroResult.setRuleId(rule.getId());
            zeroResult.setRuleName(rule.getRuleName());
            zeroResult.setFeeMode(rule.getFeeMode());
            zeroResult.setDetails(new ArrayList<>());
            return zeroResult;
        }

        // 3. 根据计费模式计算费用
        FeeCalculationResult result;
        switch (rule.getFeeMode()) {
            case 1:
                result = calculateByTimePeriod(rule, startTime, endTime, totalMinutes);
                break;
            case 2:
                result = calculateByFixedPrice(rule, startTime, endTime, totalMinutes);
                break;
            case 3:
                result = calculateByTieredPricing(rule, startTime, endTime, totalMinutes);
                break;
            default:
                throw new BusinessException(ReturnCode.RC500, "不支持的计费模式: " + rule.getFeeMode());
        }

        // 4. 设置公共字段
        result.setRuleId(rule.getId());
        result.setRuleName(rule.getRuleName());
        result.setFeeMode(rule.getFeeMode());
        result.setParkAreaId(parkAreaId);
        result.setVehicleType(vehicleType);
        result.setStartTime(startTime);
        result.setEndTime(endTime);
        result.setTotalMinutes(totalMinutes);
        result.setFreeMinutes(rule.getFreeMinutes() != null ? rule.getFreeMinutes() : 0);
        result.setDailyCap(rule.getDailyCap());

        // 5. 应用每日封顶
        if (rule.getDailyCap() != null && result.getTotalFee().compareTo(rule.getDailyCap()) > 0) {
            result.setTotalFee(rule.getDailyCap());
            result.setCapped(true);
        } else {
            result.setCapped(false);
        }

        return result;
    }

    /**
     * 查找匹配的计费规则（按优先级：园区+车型 > 园区通用 > 全局+车型 > 全局通用）
     */
    private FeeRule findMatchingRule(Long parkAreaId, Integer vehicleType) {
        // 查询所有启用的规则，按sort_order排序
        LambdaQueryWrapper<FeeRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FeeRule::getStatus, 1)
                   .orderByAsc(FeeRule::getSortOrder);
        List<FeeRule> allRules = this.list(queryWrapper);

        // 按优先级匹配
        // 1. 精确匹配：园区ID + 车辆类型
        for (FeeRule rule : allRules) {
            if (rule.getParkAreaId() != null && rule.getParkAreaId().equals(parkAreaId)
                    && rule.getVehicleType() != null && rule.getVehicleType().equals(vehicleType)) {
                return rule;
            }
        }

        // 2. 园区通用：匹配园区ID，车辆类型为NULL
        for (FeeRule rule : allRules) {
            if (rule.getParkAreaId() != null && rule.getParkAreaId().equals(parkAreaId)
                    && rule.getVehicleType() == null) {
                return rule;
            }
        }

        // 3. 全局+车型：parkAreaId为NULL，匹配车辆类型
        for (FeeRule rule : allRules) {
            if (rule.getParkAreaId() == null
                    && rule.getVehicleType() != null && rule.getVehicleType().equals(vehicleType)) {
                return rule;
            }
        }

        // 4. 全局通用：parkAreaId和vehicleType都为NULL
        for (FeeRule rule : allRules) {
            if (rule.getParkAreaId() == null && rule.getVehicleType() == null) {
                return rule;
            }
        }

        return null;
    }

    /**
     * 按时间段计费模式
     */
    private FeeCalculationResult calculateByTimePeriod(FeeRule rule, LocalDateTime startTime, LocalDateTime endTime, long totalMinutes) {
        FeeCalculationResult result = new FeeCalculationResult();
        List<FeeCalculationResult.FeeDetailItem> details = new ArrayList<>();

        // 减去免费时长
        int freeMinutes = rule.getFreeMinutes() != null ? rule.getFreeMinutes() : 0;
        long chargeableMinutes = Math.max(0, totalMinutes - freeMinutes);

        // 应用最大计费小时数限制
        if (rule.getMaxChargeHours() != null && rule.getMaxChargeHours() > 0) {
            long maxMinutes = rule.getMaxChargeHours() * 60L;
            chargeableMinutes = Math.min(chargeableMinutes, maxMinutes);
        }

        // 检查是否有时段差异化定价
        if (rule.getTimePeriods() != null && !rule.getTimePeriods().isEmpty()) {
            result = calculateWithTimePeriods(rule, startTime, endTime, totalMinutes, freeMinutes);
        } else {
            // 简单按单位时长计费
            int unitDuration = rule.getUnitDuration() != null ? rule.getUnitDuration() : 60;
            BigDecimal unitPrice = rule.getUnitPrice() != null ? rule.getUnitPrice() : BigDecimal.ZERO;

            // 计算计费单位数（向上取整）
            long units = (chargeableMinutes + unitDuration - 1) / unitDuration;
            BigDecimal totalFee = unitPrice.multiply(BigDecimal.valueOf(units));

            FeeCalculationResult.FeeDetailItem item = new FeeCalculationResult.FeeDetailItem();
            item.setDescription("停车计费（每" + unitDuration + "分钟" + unitPrice + "元）");
            item.setDurationMinutes(chargeableMinutes);
            item.setUnitPrice(unitPrice);
            item.setSubtotal(totalFee);
            details.add(item);

            result.setChargeableMinutes(chargeableMinutes);
            result.setTotalFee(totalFee);
            result.setDetails(details);
        }

        return result;
    }

    /**
     * 带时段差异化定价的计费计算
     */
    private FeeCalculationResult calculateWithTimePeriods(FeeRule rule, LocalDateTime startTime, LocalDateTime endTime, long totalMinutes, int freeMinutes) {
        FeeCalculationResult result = new FeeCalculationResult();
        List<FeeCalculationResult.FeeDetailItem> details = new ArrayList<>();

        try {
            // 解析时段定价JSON
            List<TimePeriodConfig> periods = objectMapper.readValue(
                    rule.getTimePeriods(),
                    new TypeReference<List<TimePeriodConfig>>() {}
            );

            if (periods == null || periods.isEmpty()) {
                // 没有时段配置，回退到简单计费
                return calculateByTimePeriod(rule, startTime, endTime, totalMinutes);
            }

            // 按时间顺序排序时段
            periods.sort(Comparator.comparing(TimePeriodConfig::getStart));

            // 逐分钟遍历，按时段累加费用
            long chargedMinutes = 0;
            BigDecimal totalFee = BigDecimal.ZERO;
            Map<String, PeriodAccumulator> periodMap = new LinkedHashMap<>();

            LocalDateTime current = startTime;
            while (current.isBefore(endTime)) {
                if (chargedMinutes >= freeMinutes) {
                    // 找到当前时间所属的时段
                    LocalTime currentTime = current.toLocalTime();
                    TimePeriodConfig matchedPeriod = null;
                    for (TimePeriodConfig period : periods) {
                        if (isTimeInPeriod(currentTime, period)) {
                            matchedPeriod = period;
                            break;
                        }
                    }

                    if (matchedPeriod != null) {
                        String periodKey = matchedPeriod.getStart() + "-" + matchedPeriod.getEnd();
                        periodMap.putIfAbsent(periodKey, new PeriodAccumulator(
                                matchedPeriod.getStart() + ":" + matchedPeriod.getEnd() + " 时段",
                                matchedPeriod.getUnitPrice(),
                                matchedPeriod.getUnitDuration() != null ? matchedPeriod.getUnitDuration() : rule.getUnitDuration()
                        ));
                        periodMap.get(periodKey).addMinute();
                    }
                }
                chargedMinutes++;
                current = current.plusMinutes(1);
            }

            // 计算各时段费用
            for (PeriodAccumulator acc : periodMap.values()) {
                long units = (acc.minutes + acc.unitDuration - 1) / acc.unitDuration;
                BigDecimal subtotal = acc.unitPrice.multiply(BigDecimal.valueOf(units));

                FeeCalculationResult.FeeDetailItem item = new FeeCalculationResult.FeeDetailItem();
                item.setDescription(acc.description);
                item.setDurationMinutes(acc.minutes);
                item.setUnitPrice(acc.unitPrice);
                item.setSubtotal(subtotal);
                details.add(item);

                totalFee = totalFee.add(subtotal);
            }

            // 应用最大计费小时数限制
            long chargeableMinutes = Math.max(0, totalMinutes - freeMinutes);
            if (rule.getMaxChargeHours() != null && rule.getMaxChargeHours() > 0) {
                long maxMinutes = rule.getMaxChargeHours() * 60L;
                if (chargeableMinutes > maxMinutes) {
                    // 按比例折算
                    BigDecimal ratio = BigDecimal.valueOf(maxMinutes).divide(BigDecimal.valueOf(chargeableMinutes), 10, RoundingMode.HALF_UP);
                    totalFee = totalFee.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                    chargeableMinutes = maxMinutes;
                }
            }

            result.setChargeableMinutes(chargeableMinutes);
            result.setTotalFee(totalFee);
            result.setDetails(details);

        } catch (Exception e) {
            log.error("解析时段定价JSON失败", e);
            // 解析失败，回退到简单计费
            return calculateByTimePeriod(rule, startTime, endTime, totalMinutes);
        }

        return result;
    }

    /**
     * 判断时间是否在时段内
     */
    private boolean isTimeInPeriod(LocalTime time, TimePeriodConfig period) {
        LocalTime start = LocalTime.parse(period.getStart());
        LocalTime end = LocalTime.parse(period.getEnd());

        if (end.isAfter(start)) {
            // 正常时段，如 08:00-20:00
            return !time.isBefore(start) && time.isBefore(end);
        } else {
            // 跨天时段，如 20:00-08:00
            return !time.isBefore(start) || time.isBefore(end);
        }
    }

    /**
     * 按次计费模式
     */
    private FeeCalculationResult calculateByFixedPrice(FeeRule rule, LocalDateTime startTime, LocalDateTime endTime, long totalMinutes) {
        FeeCalculationResult result = new FeeCalculationResult();
        List<FeeCalculationResult.FeeDetailItem> details = new ArrayList<>();

        BigDecimal fixedPrice = rule.getFixedPrice() != null ? rule.getFixedPrice() : BigDecimal.ZERO;

        FeeCalculationResult.FeeDetailItem item = new FeeCalculationResult.FeeDetailItem();
        item.setDescription("按次计费");
        item.setDurationMinutes(totalMinutes);
        item.setUnitPrice(fixedPrice);
        item.setSubtotal(fixedPrice);
        details.add(item);

        result.setChargeableMinutes(totalMinutes);
        result.setTotalFee(fixedPrice);
        result.setDetails(details);

        return result;
    }

    /**
     * 阶梯计费模式
     */
    private FeeCalculationResult calculateByTieredPricing(FeeRule rule, LocalDateTime startTime, LocalDateTime endTime, long totalMinutes) {
        FeeCalculationResult result = new FeeCalculationResult();
        List<FeeCalculationResult.FeeDetailItem> details = new ArrayList<>();

        // 减去免费时长
        int freeMinutes = rule.getFreeMinutes() != null ? rule.getFreeMinutes() : 0;
        long chargeableMinutes = Math.max(0, totalMinutes - freeMinutes);

        try {
            // 解析阶梯定价JSON
            List<TierConfig> tiers = objectMapper.readValue(
                    rule.getTieredPricing(),
                    new TypeReference<List<TierConfig>>() {}
            );

            if (tiers == null || tiers.isEmpty()) {
                result.setTotalFee(BigDecimal.ZERO);
                result.setChargeableMinutes(chargeableMinutes);
                result.setDetails(details);
                return result;
            }

            // 按fromHour排序
            tiers.sort(Comparator.comparing(TierConfig::getFromHour));

            BigDecimal totalFee = BigDecimal.ZERO;
            double remainingHours = chargeableMinutes / 60.0;

            for (TierConfig tier : tiers) {
                if (remainingHours <= 0) break;

                double tierHours = tier.getToHour() - tier.getFromHour();
                double hoursInTier = Math.min(tierHours, remainingHours);

                BigDecimal tierFee;
                if (tier.getPerHour() != null && tier.getPerHour()) {
                    // 按小时计费
                    long hours = (long) Math.ceil(hoursInTier);
                    tierFee = tier.getPrice().multiply(BigDecimal.valueOf(hours));
                } else {
                    // 按阶梯固定价格
                    tierFee = tier.getPrice();
                }

                FeeCalculationResult.FeeDetailItem item = new FeeCalculationResult.FeeDetailItem();
                item.setDescription("第" + tier.getFromHour() + "-" + tier.getToHour() + "小时");
                item.setDurationMinutes((long) (hoursInTier * 60));
                item.setUnitPrice(tier.getPrice());
                item.setSubtotal(tierFee);
                details.add(item);

                totalFee = totalFee.add(tierFee);
                remainingHours -= hoursInTier;
            }

            result.setTotalFee(totalFee);
            result.setChargeableMinutes(chargeableMinutes);
            result.setDetails(details);

        } catch (Exception e) {
            log.error("解析阶梯定价JSON失败", e);
            result.setTotalFee(BigDecimal.ZERO);
            result.setChargeableMinutes(chargeableMinutes);
            result.setDetails(details);
        }

        return result;
    }

    // ========== 内部辅助类 ==========

    /**
     * 时段配置（用于解析JSON）
     */
    private static class TimePeriodConfig {
        private String start;
        private String end;
        private BigDecimal unitPrice;
        private Integer unitDuration;

        public String getStart() { return start; }
        public void setStart(String start) { this.start = start; }
        public String getEnd() { return end; }
        public void setEnd(String end) { this.end = end; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public Integer getUnitDuration() { return unitDuration; }
        public void setUnitDuration(Integer unitDuration) { this.unitDuration = unitDuration; }
    }

    /**
     * 阶梯配置（用于解析JSON）
     */
    private static class TierConfig {
        private int fromHour;
        private int toHour;
        private BigDecimal price;
        private Boolean perHour;

        public int getFromHour() { return fromHour; }
        public void setFromHour(int fromHour) { this.fromHour = fromHour; }
        public int getToHour() { return toHour; }
        public void setToHour(int toHour) { this.toHour = toHour; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Boolean getPerHour() { return perHour; }
        public void setPerHour(Boolean perHour) { this.perHour = perHour; }
    }

    /**
     * 时段累加器（用于逐分钟统计各时段时长）
     */
    private static class PeriodAccumulator {
        String description;
        BigDecimal unitPrice;
        int unitDuration;
        long minutes = 0;

        PeriodAccumulator(String description, BigDecimal unitPrice, Integer unitDuration) {
            this.description = description;
            this.unitPrice = unitPrice;
            this.unitDuration = unitDuration != null ? unitDuration : 20;
        }

        void addMinute() {
            this.minutes++;
        }
    }
}
