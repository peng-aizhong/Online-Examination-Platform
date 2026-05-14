package com.exam.service.impl;

import com.exam.entity.LearningResource;
import com.exam.repository.LearningResourceRepository;
import com.exam.service.LearningResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class LearningResourceServiceImpl implements LearningResourceService {
    
    @Autowired
    private LearningResourceRepository learningResourceRepository;
    
    @Override
    public LearningResource uploadResource(LearningResource resource, MultipartFile file, MultipartFile coverFile) {
        // 设置上传时间
        resource.setUploadTime(LocalDateTime.now());
        
        // 设置默认值
        if (resource.getStatus() == null) {
            resource.setStatus(LearningResource.ResourceStatus.ACTIVE);
        }
        if (resource.getDownloadCount() == null) {
            resource.setDownloadCount(0);
        }
        if (resource.getViewCount() == null) {
            resource.setViewCount(0);
        }
        
        // 处理主文件信息
        if (file != null && !file.isEmpty()) {
            resource.setFileSize(file.getSize());
            try {
                // 保存文件到项目目录并获取文件路径
                String filePath = saveFile(file);
                resource.setFilePath(filePath);
                System.out.println("保存主文件: " + file.getOriginalFilename() + ", 大小: " + file.getSize() + ", 保存路径: " + filePath);
            } catch (Exception e) {
                System.err.println("保存主文件失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 处理封面图片
        if (coverFile != null && !coverFile.isEmpty()) {
            try {
                String coverPath = saveCoverFile(coverFile);
                resource.setCoverPath(coverPath);
                System.out.println("保存封面图片: " + coverFile.getOriginalFilename() + ", 大小: " + coverFile.getSize() + ", 路径: " + coverPath);
            } catch (Exception e) {
                System.err.println("保存封面图片失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return learningResourceRepository.save(resource);
    }
    
    @Override
    public LearningResource getResourceById(String id) {
        return learningResourceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("学习资源不存在"));
    }
    
    @Override
    public LearningResource updateResource(String id, LearningResource resource) {
        LearningResource existingResource = getResourceById(id);
        
        // 更新字段
        existingResource.setTitle(resource.getTitle());
        existingResource.setDescription(resource.getDescription());
        existingResource.setType(resource.getType());
        existingResource.setCategory(resource.getCategory());
        existingResource.setTags(resource.getTags());
        existingResource.setSubjectId(resource.getSubjectId());
        existingResource.setStatus(resource.getStatus());
        
        return learningResourceRepository.save(existingResource);
    }
    
    @Override
    public LearningResource updateResource(String id, LearningResource resource, MultipartFile coverFile) {
        LearningResource existingResource = getResourceById(id);
        
        // 更新字段
        existingResource.setTitle(resource.getTitle());
        existingResource.setDescription(resource.getDescription());
        existingResource.setType(resource.getType());
        existingResource.setCategory(resource.getCategory());
        existingResource.setTags(resource.getTags());
        existingResource.setSubjectId(resource.getSubjectId());
        existingResource.setStatus(resource.getStatus());
        
        // 处理封面图片更新
        if (coverFile != null && !coverFile.isEmpty()) {
            try {
                String coverPath = saveCoverFile(coverFile);
                existingResource.setCoverPath(coverPath);
                System.out.println("更新封面图片: " + coverFile.getOriginalFilename() + ", 大小: " + coverFile.getSize() + ", 路径: " + coverPath);
            } catch (Exception e) {
                System.err.println("更新封面图片失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return learningResourceRepository.save(existingResource);
    }
    
    @Override
    public void deleteResource(String id) {
        if (!learningResourceRepository.existsById(id)) {
            throw new RuntimeException("学习资源不存在");
        }
        
        // 这里应该实现文件删除逻辑
        // deleteFile(resource.getFilePath());
        
        learningResourceRepository.deleteById(id);
    }
    
    @Override
    public Page<LearningResource> searchResources(String type, String subjectId, 
                                                String category, String status, 
                                                String keyword, Pageable pageable) {
        return learningResourceRepository.findByComplexCriteria(type, subjectId, category, status, keyword, pageable);
    }
    
    @Override
    public byte[] downloadResource(String id) {
        LearningResource resource = getResourceById(id);
        
        // 增加下载次数
        incrementDownloadCount(id);
        
        // 这里应该实现文件读取逻辑
        // return readFile(resource.getFilePath());
        
        return new byte[0]; // 简化实现
    }
    
    @Override
    public void incrementDownloadCount(String id) {
        LearningResource resource = getResourceById(id);
        resource.setDownloadCount(resource.getDownloadCount() + 1);
        learningResourceRepository.save(resource);
    }
    
    @Override
    public void incrementViewCount(String id) {
        LearningResource resource = getResourceById(id);
        resource.setViewCount(resource.getViewCount() + 1);
        learningResourceRepository.save(resource);
    }
    
    @Override
    public long getResourceCount() {
        return learningResourceRepository.count();
    }
    
    @Override
    public List<LearningResource> getAllResources() {
        return learningResourceRepository.findAll();
    }
    
    @Override
    public List<LearningResource> getResourcesByUploader(String uploaderId) {
        return learningResourceRepository.findByUploaderId(uploaderId);
    }
    
    /**
     * 保存主文件
     */
    private String saveFile(MultipartFile file) throws Exception {
        // 获取项目根目录的绝对路径
        String projectRoot = System.getProperty("user.dir");
        String uploadDir = projectRoot + File.separator + "uploads" + File.separator + "files" + File.separator;
        
        // 创建文件目录
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("无法创建目录: " + uploadDir);
            }
        }
        
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IOException("文件名不能为空");
        }
        
        // 创建目标文件，使用原始文件名
        File targetFile = new File(dir, originalFilename);
        
        // 如果文件已存在，添加时间戳避免冲突
        if (targetFile.exists()) {
            String nameWithoutExt = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String timestampedFilename = nameWithoutExt + "_" + System.currentTimeMillis() + extension;
            targetFile = new File(dir, timestampedFilename);
        }
        
        // 保存文件
        file.transferTo(targetFile);
        
        // 返回相对路径用于数据库存储（file_path字段存储相对路径）
        return "uploads/files/" + targetFile.getName();
    }
    
    /**
     * 保存封面图片文件
     */
    private String saveCoverFile(MultipartFile coverFile) throws Exception {
        // 获取项目根目录的绝对路径
        String projectRoot = System.getProperty("user.dir");
        String uploadDir = projectRoot + File.separator + "uploads" + File.separator + "covers" + File.separator;
        
        // 创建封面图片目录
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("无法创建目录: " + uploadDir);
            }
        }
        
        // 生成唯一的文件名
        String originalFilename = coverFile.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = "cover_" + System.currentTimeMillis() + extension;
        
        // 创建目标文件
        File targetFile = new File(dir, filename);
        
        // 保存文件
        coverFile.transferTo(targetFile);
        
        // 返回相对路径（用于数据库存储和URL访问）
        return "covers/" + filename;
    }
} 