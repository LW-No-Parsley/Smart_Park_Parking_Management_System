package com.syan.smart_park.common.aspect;

import com.syan.smart_park.common.annotation.RequirePermission;
import com.syan.smart_park.common.exception.BusinessException;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.common.utils.JwtUtil;
import com.syan.smart_park.service.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 权限校验切面
 * 拦截带有 @RequirePermission 注解的方法，检查当前用户是否有指定权限
 */
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final RoleService roleService;

    @Before("@annotation(com.syan.smart_park.common.annotation.RequirePermission) || " +
            "@within(com.syan.smart_park.common.annotation.RequirePermission)")
    public void checkPermission(JoinPoint joinPoint) {
        // 获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ReturnCode.RC401);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Long userId)) {
            throw new BusinessException(ReturnCode.RC401);
        }

        // 获取注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);
        if (annotation == null) {
            // 如果方法上没有注解，尝试类级别
            annotation = joinPoint.getTarget().getClass().getAnnotation(RequirePermission.class);
        }
        if (annotation == null) {
            return;
        }

        // 小程序用户（park_user）不经过 RBAC 权限校验
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String userType = (String) request.getAttribute("userType");
            if (JwtUtil.USER_TYPE_PARK_USER.equals(userType)) {
                return;
            }
        }

        String[] permissions = annotation.value();
        RequirePermission.Logical logical = annotation.logical();

        if (logical == RequirePermission.Logical.OR) {
            // 拥有任一权限即可
            for (String perm : permissions) {
                if (roleService.hasPermission(userId, perm)) {
                    return;
                }
            }
            throw new BusinessException(ReturnCode.RC403);
        } else {
            // 必须拥有所有权限
            for (String perm : permissions) {
                if (!roleService.hasPermission(userId, perm)) {
                    throw new BusinessException(ReturnCode.RC403);
                }
            }
        }
    }
}
