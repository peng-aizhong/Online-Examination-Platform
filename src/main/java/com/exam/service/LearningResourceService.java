package com.exam.service;

import com.exam.entity.LearningResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface LearningResourceService {
    
    /**
     * 上传资源
     */
    LearningResource uploadResource(LearningResource resource, MultipartFile file, MultipartFile coverFile);
    
    /**
     * 根据ID获取资源
     */
    LearningResource getResourceById(String id);
    
    /**
     * 更新资源
     */
    LearningResource updateResource(String id, LearningResource resource);
    
    LearningResource updateResource(String id, LearningResource resource, MultipartFile coverFile);
    
    /**
     * 删除资源
     */
    void deleteResource(String id);
    
    /**
     * 搜索资源
     */
    Page<LearningResource> searchResources(String type, String subjectId, 
                                         String category, String status, 
                                         String keyword, Pageable pageable);
    
    /**
     * 下载资源
     */
    byte[] downloadResource(String id);
    
    /**
     * 增加下载次数
     */
    void incrementDownloadCount(String id);
    
    /**
     * 增加查看次数
     */
    void incrementViewCount(String id);
    
    /**
     * 获取资源总数
     */
    long getResourceCount();
    
    /**
     * 获取所有资源
     */
    List<LearningResource> getAllResources();
    
    /**
     * 根据上传者获取资源
     */
    List<LearningResource> getResourcesByUploader(String uploaderId);
} 