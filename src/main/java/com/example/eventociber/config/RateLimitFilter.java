package com.example.eventociber.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.web.filter.OncePerRequestFilter;

public class RateLimitFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> cache;
    private final long capacidadIp;
    private final long ventanaSegundos;
    private final Bucket globalBucket;

    public RateLimitFilter(long capacidadIp, long ventanaSegundos, long capacidadGlobal) {
        this.capacidadIp = capacidadIp;
        this.ventanaSegundos = ventanaSegundos;
        
        // Caché para las IPs
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofSeconds(ventanaSegundos * 2))
                .maximumSize(10000)
                .build();
                
        // Bucket Global (compartido por todas las peticiones)
        Bandwidth limitGlobal = Bandwidth.builder()
                .capacity(capacidadGlobal)
                .refillGreedy(capacidadGlobal, Duration.ofSeconds(ventanaSegundos))
                .build();
        this.globalBucket = Bucket.builder().addLimit(limitGlobal).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!"POST".equalsIgnoreCase(request.getMethod()) || !"/api/inscripciones".equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Verificación Global primero (protege al servidor de ataques distribuidos)
        if (!globalBucket.tryConsume(1)) {
            reject(response);
            return;
        }

        // 2. Verificación por IP (generoso para WiFis compartidos)
        String clientIp = getClientIP(request);
        Bucket ipBucket = cache.get(clientIp, this::createNewIpBucket);

        if (ipBucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            reject(response);
        }
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(429); // 429 Too Many Requests
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"El sistema está saturado. Intenta de nuevo en un minuto.\"}");
    }

    private Bucket createNewIpBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacidadIp)
                .refillGreedy(capacidadIp, Duration.ofSeconds(ventanaSegundos))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIP(HttpServletRequest request) {
        // Obtenemos la IP real (previene spoofing si está detrás de un proxy/balanceador estándar)
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
