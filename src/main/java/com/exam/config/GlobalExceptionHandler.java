package com.exam.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();

        // favicon.ico 忽略
        if (requestUrl.contains("favicon.ico")) {
            return null;
        }

        // 已知的非项目路径（浏览器插件等），仅 WARN 级别
        if (requestUrl.contains("hybridaction") || requestUrl.contains("zybTracker")) {
            return null;
        }

        // 其他 404 也静默处理
        return null;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        // AJAX 请求返回 JSON
        if (isAjaxRequest(request)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "权限不足，无法访问此资源");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("error", "权限不足，您没有权限访问此页面");
        modelAndView.addObject("errorTitle", "访问被拒绝");
        modelAndView.setViewName("error");
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex, HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();

        if (requestUrl.contains("favicon.ico")) {
            return null;
        }

        // 浏览器插件产生的未知路径请求，静默忽略
        if (requestUrl.contains("hybridaction") || requestUrl.contains("zybTracker")) {
            return null;
        }

        // 只对项目内的请求打印错误信息
        System.err.println("全局异常处理器捕获到异常: " + ex.getMessage());
        System.err.println("异常请求URL: " + requestUrl);

        if (isAjaxRequest(request)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "系统错误: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ex.printStackTrace();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("error", "系统错误: " + ex.getMessage());
        modelAndView.addObject("errorTitle", "系统错误");
        modelAndView.setViewName("error");
        return modelAndView;
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String contentType = request.getHeader("Content-Type");
        String accept = request.getHeader("Accept");
        String xRequestedWith = request.getHeader("X-Requested-With");
        return (contentType != null && contentType.contains("application/x-www-form-urlencoded"))
            || (accept != null && accept.contains("application/json"))
            || "XMLHttpRequest".equals(xRequestedWith);
    }
}
