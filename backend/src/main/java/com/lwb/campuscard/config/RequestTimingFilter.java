package com.lwb.campuscard.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 接口耗时统计：只看 /api 下的请求，超过 300 毫秒打一条 warn
 * 压测时可以直接从日志里看出哪几个接口先变慢
 */
@Component
public class RequestTimingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestTimingFilter.class);

    /** 超过这个耗时就认为值得看一眼 */
    private static final long SLOW_MS = 300;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long begin = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long cost = System.currentTimeMillis() - begin;
            if (request.getRequestURI().startsWith("/api") && cost >= SLOW_MS) {
                log.warn("慢请求 {} {} 耗时 {}ms", request.getMethod(), request.getRequestURI(), cost);
            }
        }
    }
}
