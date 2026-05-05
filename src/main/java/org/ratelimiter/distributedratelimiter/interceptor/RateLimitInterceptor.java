package org.ratelimiter.distributedratelimiter.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.ratelimiter.distributedratelimiter.services.RateLimitService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. Identify the client.
        String clientId = resolveClientId(request);

        // 2. Ask Redis if this client has tokens left
        if (rateLimitService.isAllowed(clientId)) {
            // Returning true tells Spring to pass the request down the chain to the Controller
            return true;
        }

        // 3. The bucket is empty. Short-circuit the request here.
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");

        // Returning false halts the execution chain immediately.
        // The Controller is never invoked.
        return false;
    }

    /**
     * Extracts the IP Address or API Key to identify the user.
     */
    private String resolveClientId(HttpServletRequest request) {
        // Staff-level detail: If your app is deployed behind a Load Balancer (AWS ALB, Nginx)
        // or a CDN (Cloudflare), getRemoteAddr() will return the Load Balancer's IP, not the user's.
        // We must check the "X-Forwarded-For" header first.
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim(); // Get the first IP in the chain
        }

        return request.getRemoteAddr();
    }
}
