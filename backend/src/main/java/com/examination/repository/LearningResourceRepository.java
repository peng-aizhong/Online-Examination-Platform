package com.examination.repository;

import com.examination.entity.LearningResource;
import com.examination.entity.LearningResource.ResourceStatus;
import com.examination.entity.LearningResource.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningResourceRepository extends JpaRepository<LearningResource, String> {
    List<LearningResource> findByUploader_UserId(String uploaderId);

    List<LearningResource> findBySubject_SubjectId(String subjectId);

    List<LearningResource> findByStatus(ResourceStatus status);

    List<LearningResource> findByType(ResourceType type);

    List<LearningResource> findByStatusAndType(ResourceStatus status, ResourceType type);

    List<LearningResource> findByStatusOrderByUploadTimeDesc(ResourceStatus status);

    List<LearningResource> findByUploader_UserIdOrderByUploadTimeDesc(String uploaderId);
}
