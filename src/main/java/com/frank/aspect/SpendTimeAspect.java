package com.frank.aspect;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * Created by frank on 17/4/12.
 */
@Aspect
@Order(1)  //优先级最大
@Component
public class SpendTimeAspect {
    private Logger logger = Logger.getLogger(getClass());

    private ThreadLocal<Long> time = new ThreadLocal<>();

    @Pointcut("execution(public * com.frank.controller..*.*(..))")
    public void timeSpend(){}


    @Before("timeSpend()")
    public void doBefore(JoinPoint joinPoint) {
        time.set(System.currentTimeMillis());

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        logger.info("路径 : " + request.getRequestURL().toString());
        logger.info("请求方法 : " + request.getMethod());
        logger.info("IP地址 : " + request.getRemoteAddr());
        logger.info("执行的方法 : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        logger.info("参数 : " + Arrays.toString(joinPoint.getArgs()));

    }

    @AfterReturning(returning = "JsonBody", pointcut = "timeSpend()")
    public void doAfterReturning(Object JsonBody) throws Throwable {
        // 处理完请求，返回内容
        logger.info("响应内容 : " + JsonBody.toString());
        logger.info("花费时间 : " + (System.currentTimeMillis() - time.get()));
    }
}
