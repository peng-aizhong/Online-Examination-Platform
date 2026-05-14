package com.exam.repository;

import com.exam.entity.LearningResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningResourceRepository extends JpaRepository<LearningResource, String> {

    // 根据类型查找资源
    List<LearningResource> findByType(String type);

    // 根据科目查找资源
    List<LearningResource> findBySubjectId(String subjectId);

    // 根据上传者查找资源
    List<LearningResource> findByUploaderId(String uploaderId);

    // 根据状态查找资源
    List<LearningResource> findByStatus(String status);

    // 根据类型和科目查找资源
    List<LearningResource> findByTypeAndSubjectId(String type, String subjectId);

    // 根据类型和状态查找资源
    List<LearningResource> findByTypeAndStatus(String type, String status);

    // 根据科目和状态查找资源
    List<LearningResource> findBySubjectIdAndStatus(String subjectId, String status);

    // 根据标题模糊查找资源
    List<LearningResource> findByTitleContaining(String title);

    // 根据描述模糊查找资源
    List<LearningResource> findByDescriptionContaining(String description);

    // 根据标签模糊查找资源
    List<LearningResource> findByTagsContaining(String tags);

    // 复杂查询：根据多个条件查找资源
    @Query("SELECT lr FROM LearningResource lr WHERE " +
           "(:type IS NULL OR lr.type = :type) AND " +
           "(:subjectId IS NULL OR lr.subjectId = :subjectId) AND " +
           "(:category IS NULL OR lr.category = :category) AND " +
           "(:status IS NULL OR lr.status = :status) AND " +
           "(:keyword IS NULL OR lr.title LIKE %:keyword% OR lr.description LIKE %:keyword% OR lr.tags LIKE %:keyword%)")
    Page<LearningResource> findByComplexCriteria(@Param("type") String type,
                                               @Param("subjectId") String subjectId,
                                               @Param("category") String category,
                                               @Param("status") String status,
                                               @Param("keyword") String keyword,
                                               Pageable pageable);

    // 根据类型统计资源数量
    long countByType(String type);

    // 根据科目统计资源数量
    long countBySubjectId(String subjectId);

    // 根据上传者统计资源数量
    long countByUploaderId(String uploaderId);

    // 根据状态统计资源数量
    long countByStatus(String status);

    // 获取所有资源类型
    @Query("SELECT DISTINCT lr.type FROM LearningResource lr")
    List<String> findAllTypes();

    // 获取所有资源分类
    @Query("SELECT DISTINCT lr.category FROM LearningResource lr WHERE lr.category IS NOT NULL")
    List<String> findAllCategories();
    
    /**
     * 查找最大的resource_id（用于生成递增ID）
     */
    @Query("SELECT MAX(lr.id) FROM LearningResource lr WHERE lr.id LIKE 'LS%'")
    String findMaxResourceId();
} 