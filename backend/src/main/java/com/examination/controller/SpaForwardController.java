package com.examination.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 将前端（Vue Router history 模式）的页面路由转发到 index.html，
 * 让用户只启动 Spring Boot 即可通过同一个端口访问前端界面。
 *
 * 注意：后端 API 已统一在 /api/** 下，因此不会与前端 /student/** 等路由冲突。
 */
@Controller
public class SpaForwardController {

    @RequestMapping({
            "/",
            "/login",
            "/register",
            "/dashboard",
            "/student/**",
            "/teacher/**",
            "/teacher/questions",
            "/teacher/papers",
            "/teacher/resources",
            "/student/resources"
    })
    public String forwardSpa() {
        return "forward:/index.html";
    }
}

