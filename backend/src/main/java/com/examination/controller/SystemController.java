package com.examination.controller;

import com.examination.common.ApiResponse;
import com.examination.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/system")
public class SystemController {
    @Autowired
    private SubjectRepository subjectRepository;

    @GetMapping({"", "/"})
    public ApiResponse<Map<String, Object>> root() {
        return ApiResponse.success(Map.of(
                "name", "online-examination-platform-api",
                "status", "ok",
                "message", "后端服务运行正常"
        ));
    }

    @GetMapping("/subjects")
    public ApiResponse<List<Map<String, String>>> getSubjects() {
        List<Map<String, String>> subjects = subjectRepository.findAll().stream()
                .map(s -> Map.of(
                        "subjectId", s.getSubjectId(),
                        "subjectName", s.getSubjectName()
                ))
                .collect(Collectors.toList());
        return ApiResponse.success(subjects);
    }
}
