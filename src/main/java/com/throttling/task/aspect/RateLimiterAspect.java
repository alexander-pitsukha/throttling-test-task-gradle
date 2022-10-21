package com.throttling.task.aspect;

import com.throttling.task.config.AppConfig;
import com.throttling.task.exception.RateLimitedException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Aspect
@Component
public class RateLimiterAspect {
    private final ConcurrentMap<String, ConcurrentMap<String, Deque<Long>>> limiters = new ConcurrentHashMap<>();
    @Value("${rate.limit.period}")
    private Integer rateLimitPeriod;
    @Value("${rate.limit}")
    private Integer rateLimit;

    @Before("@annotation(com.throttling.task.aspect.RateLimit)")
    public void rateLimit(JoinPoint joinPoint) {
        HttpServletRequest request = AppConfig.CURRENT_REQUEST.get();
        String ipAddress = request.getHeader("X-FORWARDED-FOR");

        if (ipAddress == null) {
            String remoteAddr = request.getRemoteAddr();
            ipAddress = remoteAddr.contains(",") ? remoteAddr.split(",")[0] : remoteAddr;
        }

        ConcurrentMap<String, Deque<Long>> methodLimiters = limiters.computeIfAbsent(ipAddress,
                k -> new ConcurrentHashMap<>());

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String classMethod = signature.getDeclaringTypeName() + "#" + signature.getName();
        Deque<Long> counter = methodLimiters.computeIfAbsent(classMethod, k -> new LinkedList<>());
        counter.addLast(System.currentTimeMillis() / 1000);

        Iterator<Long> iterator = counter.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() < (System.currentTimeMillis() / 1000) - rateLimitPeriod) {
                iterator.remove();
            } else {
                break;
            }
        }

        if (counter.size() > rateLimit) {
            throw new RateLimitedException();
        }
    }

}
