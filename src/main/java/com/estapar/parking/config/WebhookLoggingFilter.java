package com.estapar.parking.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
@Order(1)
public class WebhookLoggingFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(WebhookLoggingFilter.class);

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    if ("/webhook".equals(request.getRequestURI()) && "POST".equals(request.getMethod())) {
      log.info("webhook recebido no filtro: method={}, uri={}", request.getMethod(), request.getRequestURI());
      ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
      filterChain.doFilter(wrappedRequest, response);

      byte[] contentAsByteArray = wrappedRequest.getContentAsByteArray();
      if (contentAsByteArray.length > 0) {
        String jsonBody = new String(contentAsByteArray, StandardCharsets.UTF_8);
        log.debug("JSON recebido no webhook: {}", jsonBody);
      } else {
        log.warn("webhook recebido sem body");
      }
    } else {
      filterChain.doFilter(request, response);
    }
  }
}
