package com.exam.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex, HttpServletRequest request) {
        // 获取请求URL
        String requestUrl = request.getRequestURL().toString();
        
        // 忽略 favicon.ico 的错误
        if (requestUrl.contains("favicon.ico")) {
            return null; // 返回null表示不处理这个异常
        }
        
        System.err.println("全局异常处理器捕获到异常: " + ex.getMessage());
        ex.printStackTrace();
        System.err.println("异常请求URL: " + requestUrl);
        
        // 检查是否是AJAX请求
        String contentType = request.getHeader("Content-Type");
        String accept = request.getHeader("Accept");
        String xRequestedWith = request.getHeader("X-Requested-With");
        
        boolean isAjax = (contentType != null && contentType.contains("application/x-www-form-urlencoded")) ||
                        (accept != null && accept.contains("application/json")) ||
                        "XMLHttpRequest".equals(xRequestedWith);
        
        System.err.println("是否为AJAX请求: " + isAjax);
        System.err.println("Content-Type: " + contentType);
        System.err.println("Accept: " + accept);
        System.err.println("X-Requested-With: " + xRequestedWith);
        
        if (isAjax) {
            // 对于AJAX请求，直接返回JSON响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "系统错误: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        // 对于普通请求，重定向到错误页面
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("error", "系统错误: " + ex.getMessage());
        modelAndView.addObject("exception", ex);
        modelAndView.setViewName("error");
        return modelAndView;
    }
}
