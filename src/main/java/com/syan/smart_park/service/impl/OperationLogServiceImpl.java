package com.syan.smart_park.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.common.utils.IpUtil;
import com.syan.smart_park.dao.OperationLogMapper;
import com.syan.smart_park.entity.OperationLog;
import com.syan.smart_park.entity.OperationLogDTO;
import com.syan.smart_park.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志服务实现类
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public PageResult<OperationLogDTO> pageOperationLogs(long current, long size,
                                                         Long userId,
                                                         String module, String action, String ip,
                                                         LocalDateTime startTime, LocalDateTime endTime,
                                                         String keyword) {
        LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();

        // 动态条件：仅传入非 null 值时生效
        queryWrapper.eq(userId != null, OperationLog::getUserId, userId);
        queryWrapper.like(module != null && !module.isEmpty(), OperationLog::getModule, module);
        queryWrapper.like(action != null && !action.isEmpty(), OperationLog::getAction, action);
        queryWrapper.like(ip != null && !ip.isEmpty(), OperationLog::getIp, ip);
        queryWrapper.ge(startTime != null, OperationLog::getCreateTime, startTime);
        queryWrapper.le(endTime != null, OperationLog::getCreateTime, endTime);

        // 关键词模糊搜索 module / action / detail
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(w -> w
                    .like(OperationLog::getModule, keyword)
                    .or()
                    .like(OperationLog::getAction, keyword)
                    .or()
                    .like(OperationLog::getDetail, keyword));
        }

        queryWrapper.orderByDesc(OperationLog::getCreateTime);

        IPage<OperationLog> page = this.page(new Page<>(current, size), queryWrapper);
        List<OperationLogDTO> dtoList = page.getRecords().stream()
                .map(OperationLogDTO::fromOperationLog)
                .collect(Collectors.toList());

        return PageResult.of(dtoList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public OperationLogDTO getOperationLogById(Long id) {
        OperationLog operationLog = this.getById(id);
        if (operationLog == null || operationLog.getDeleted() == 1) {
            return null;
        }
        return OperationLogDTO.fromOperationLog(operationLog);
    }

    @Override
    public OperationLogDTO createOperationLog(OperationLogDTO operationLogDTO) {
        // 如果未设置userId，则从SecurityContextHolder获取当前登录用户ID
        if (operationLogDTO.getUserId() == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Long) {
                operationLogDTO.setUserId((Long) authentication.getPrincipal());
            }
        }

        // 如果未设置IP，则从当前请求中获取
        if (operationLogDTO.getIp() == null || operationLogDTO.getIp().isEmpty()) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = IpUtil.getIpAddress(request);
                operationLogDTO.setIp(ip);
            }
        }

        OperationLog operationLog = operationLogDTO.toOperationLog();
        this.save(operationLog);
        return OperationLogDTO.fromOperationLog(operationLog);
    }
}
