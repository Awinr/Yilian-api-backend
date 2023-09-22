package com.Reflux.ReApi.aop;

import com.Reflux.ReApi.annotation.AuthCheck;
import com.Reflux.ReApi.common.ErrorCode;
import com.Reflux.ReApi.exception.BusinessException;

import com.Reflux.ReApi.model.enums.UserRoleEnum;
import com.Reflux.ReApi.service.UserService;
import javax.annotation.Resource;

import com.Reflux.ReApi.model.vo.LoginUserVO;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 权限校验 AOP
 *
 * @author Aaron
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        LoginUserVO loginUser = userService.getLoginUserByThreadLocal();
        // 必须有该权限才通过
        if (StringUtils.isNotBlank(mustRole)) {
            // 通过方法上传入的所要求角色，获得角色对应的枚举对象
            UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
            if (mustUserRoleEnum == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            String userRole = loginUser.getUserRole();
            // 如果被封号，直接拒绝
            if (UserRoleEnum.BAN.equals(mustUserRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 必须有管理员权限
            if (UserRoleEnum.ADMIN.equals(mustUserRoleEnum)) {
                if (!mustRole.equals(userRole)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
                }
            }
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

