package com.examination.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassStatisticsResponse {
    private Integer totalStudents;
    private Integer totalAssignments;
    private Double averageScore;
    private Double highestScore;
    private Double lowestScore;
    private Double passRate;
    private List<AssignmentStat> assignmentStats;
    private Map<String, Double> scoreDistribution;
    private List<KnowledgePointStat> weakKnowledgePoints;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignmentStat {
        private String assignmentId;
        private String assignmentName;
        private String paperName;
        private Double averageScore;
        private Double highestScore;
        private Double lowestScore;
        private Double passRate;
        private Integer participantCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KnowledgePointStat {
        private String knowledgeTag;
        private Double accuracy;
        private Integer totalQuestions;
        private Integer correctCount;
        private Boolean isWeak;
    }
}
