package com.oranges.fileuploaderbackend.aop;

import com.oranges.fileuploaderbackend.annotation.AuthCheck;
import com.oranges.fileuploaderbackend.exception.BusinessException;
import com.oranges.fileuploaderbackend.exception.ErrorCode;
import com.oranges.fileuploaderbackend.model.entity.User;
import com.oranges.fileuploaderbackend.model.enums.UserRoleEnum;
import com.oranges.fileuploaderbackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect//定义切面
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object Interceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        //不需要权限，放行
        if(mustRoleEnum == null){
            return joinPoint.proceed();
        }
        //以下需要权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //如果要求管理员，但用户不是管理员
        if(UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //通过权限校验
        return joinPoint.proceed();
    }
}
