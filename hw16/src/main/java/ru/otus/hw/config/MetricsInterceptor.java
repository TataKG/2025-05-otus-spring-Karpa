package ru.otus.hw.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.otus.hw.metrics.ApplicationMetrics;

@Component
@Slf4j
@RequiredArgsConstructor
public class MetricsInterceptor implements HandlerInterceptor {

    private final ApplicationMetrics metrics;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute("startTime");
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            String endpoint = request.getRequestURI();

            metrics.recordRequestDuration(endpoint, duration);

            if (duration > 1000) {
                log.warn("Slow request detected: {} took {}ms", endpoint, duration);
            }
        }
    }
}
