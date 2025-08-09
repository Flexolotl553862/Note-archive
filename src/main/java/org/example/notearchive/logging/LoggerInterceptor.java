package org.example.notearchive.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

@Component
public class LoggerInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggerInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String message = "Request: " +
                "[Uri: " + request.getRequestURI() + "]" +
                "[Method: " + request.getMethod() + "]" +
                "[From: " + request.getRemoteAddr() + "]" +
                "[At: " + formatter.format(new Date()) + "]" +
                "[Principal: " + getPrincipal(request) + "]";

        if (Stream.of("/css", "/js", "/assets", "/img")
                .noneMatch((p -> request.getRequestURI().startsWith(p)))
        ) {
            logger.atInfo()
                    .addKeyValue("request", request)
                    .setMessage(message)
                    .log();
        }
        return true;
    }

    private String getPrincipal(HttpServletRequest request) {
        if (request.getUserPrincipal() != null) {
            return request.getUserPrincipal().getName();
        }
        return "Anonymous";
    }
}
