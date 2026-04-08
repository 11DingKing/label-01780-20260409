package com.help.mp.interceptor;

import com.help.mp.entity.PerfLog;
import com.help.mp.mapper.PerfLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class PerformanceInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "_perf_start";
    private final PerfLogMapper perfLogMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME);
        if (startTime == null) return;

        long duration = System.currentTimeMillis() - startTime;
        String uri = request.getRequestURI();
        String method = request.getMethod();
        int status = response.getStatus();

        // 超过500ms记录警告
        if (duration > 500) {
            log.warn("SLOW API: {} {} took {}ms (status={})", method, uri, duration, status);
        }

        // 异步写入性能日志（简单实现，生产环境可用队列）
        try {
            PerfLog perfLog = new PerfLog();
            perfLog.setUri(uri.length() > 256 ? uri.substring(0, 256) : uri);
            perfLog.setMethod(method);
            perfLog.setDurationMs(duration);
            perfLog.setStatusCode(status);
            perfLogMapper.insert(perfLog);
        } catch (Exception e) {
            log.debug("Failed to save perf log: {}", e.getMessage());
        }
    }
}
