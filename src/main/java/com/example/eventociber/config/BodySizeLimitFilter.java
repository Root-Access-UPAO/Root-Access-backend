package com.example.eventociber.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.springframework.web.filter.OncePerRequestFilter;

public class BodySizeLimitFilter extends OncePerRequestFilter {

    private final long maxBodyBytes;

    public BodySizeLimitFilter(long maxBodyBytes) {
        this.maxBodyBytes = maxBodyBytes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!"POST".equalsIgnoreCase(request.getMethod()) || !"/api/inscripciones".equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        long contentLength = request.getContentLengthLong();

        if (contentLength > maxBodyBytes) {
            reject(response);
            return;
        }

        if (contentLength == -1) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream is = request.getInputStream();
            byte[] buffer = new byte[1024];
            int read;
            long total = 0;
            while ((read = is.read(buffer)) != -1) {
                total += read;
                if (total > maxBodyBytes) {
                    reject(response);
                    return;
                }
                baos.write(buffer, 0, read);
            }
            
            byte[] cachedBody = baos.toByteArray();
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public ServletInputStream getInputStream() {
                    return new CachedServletInputStream(cachedBody);
                }
                @Override
                public BufferedReader getReader() {
                    String enc = request.getCharacterEncoding();
                    if (enc == null) enc = "UTF-8";
                    try {
                        return new BufferedReader(new InputStreamReader(getInputStream(), enc));
                    } catch (java.io.UnsupportedEncodingException e) {
                        return new BufferedReader(new InputStreamReader(getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
                    }
                }
            };
            filterChain.doFilter(wrappedRequest, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Connection", "close");
        response.getWriter().write("{\"error\":\"Petición demasiado grande\"}");
    }

    private static class CachedServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream bais;
        public CachedServletInputStream(byte[] cachedBody) { this.bais = new ByteArrayInputStream(cachedBody); }
        @Override public boolean isFinished() { return bais.available() == 0; }
        @Override public boolean isReady() { return true; }
        @Override public void setReadListener(ReadListener readListener) { throw new UnsupportedOperationException(); }
        @Override public int read() throws IOException { return bais.read(); }
    }
}
