package com.exam.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.MultipartConfigElement;
import java.io.File;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
    
    /**
     * 配置文件上传大小限制
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // 设置单个文件最大大小为50GB
        factory.setMaxFileSize(DataSize.ofGigabytes(50));
        
        // 设置总请求最大大小为50GB
        factory.setMaxRequestSize(DataSize.ofGigabytes(50));
        
        // 设置文件写入磁盘的阈值（当文件大小超过此值时，文件将被写入磁盘）
        factory.setFileSizeThreshold(DataSize.ofKilobytes(2));
        
        // 设置临时文件存储位置
        factory.setLocation(System.getProperty("java.io.tmpdir"));
        
        return factory.createMultipartConfig();
    }
    
    /**
     * 配置静态资源访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录
        String projectRoot = System.getProperty("user.dir");
        
        // 配置uploads目录访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + projectRoot + File.separator + "uploads" + File.separator)
                .setCachePeriod(3600); // 缓存1小时
        
        // 配置头像文件访问路径
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:" + projectRoot + File.separator + "uploads" + File.separator + "avatars" + File.separator)
                .setCachePeriod(3600); // 缓存1小时
        
        // 配置图片文件访问路径 - 支持任意路径
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/", "file:./images/", "file:/")
                .setCachePeriod(3600); // 缓存1小时
        
        // 配置通用文件访问路径 - 支持项目根目录下的uploads文件夹
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + projectRoot + File.separator + "uploads" + File.separator + "files" + File.separator)
                .setCachePeriod(3600); // 缓存1小时
        
        // 配置封面图片访问路径
        registry.addResourceHandler("/covers/**")
                .addResourceLocations("file:" + projectRoot + File.separator + "uploads" + File.separator + "covers" + File.separator)
                .setCachePeriod(3600); // 缓存1小时
    }
} 