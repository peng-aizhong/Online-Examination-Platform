package com.exam.service.impl;
import com.exam.dto.ImportResult;
import com.exam.entity.Question;
import com.exam.repository.QuestionRepository;
import com.exam.repository.PaperQuestionRepository;
import com.exam.repository.ExamSessionAnswerRepository;
import com.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import java.io.IOException;
import java.io.InputStream;

@Service
@Transactional
public class QuestionServiceImpl implements QuestionService {
    
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private PaperQuestionRepository paperQuestionRepository;

    @Autowired
    private ExamSessionAnswerRepository examSessionAnswerRepository;
    
    @Override
    public Question addQuestion(Question question) {
        System.out.println("=== QuestionService.addQuestion 开始 ===");
        System.out.println("接收到的题目对象: " + question);
        
        // 设置创建时间和更新时间
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        System.out.println("设置时间完成");
        
        // 自动生成题目ID
        if (question.getId() == null || question.getId().trim().isEmpty()) {
            String questionId = generateQuestionId(question.getQuestionType());
            question.setId(questionId);
            System.out.println("生成题目ID: " + questionId);
        }
        
        // 不手动设置时间，让数据库自动处理
        if (question.getStatus() == null) {
            question.setStatus("启用");
        }
        System.out.println("状态设置: " + question.getStatus());
        
        // 保持手动录入的代码格式
        if (question.getContent() != null) {
            question.setContent(preserveCodeFormat(question.getContent()));
            System.out.println("内容格式化完成，长度: " + question.getContent().length());
        }
        if (question.getAnswer() != null) {
            question.setAnswer(preserveCodeFormat(question.getAnswer()));
            System.out.println("答案格式化完成，长度: " + question.getAnswer().length());
        }
        
        // 处理选择题选项字段
        if (question.getQuestionType() != null && question.getQuestionType().equals("C")) {
            if (question.getOptionA() == null) question.setOptionA(null);
            if (question.getOptionB() == null) question.setOptionB(null);
            if (question.getOptionC() == null) question.setOptionC(null);
            if (question.getOptionD() == null) question.setOptionD(null);
            System.out.println("选择题，选项字段已设置");
        } else {
            question.setOptionA(null);
            question.setOptionB(null);
            question.setOptionC(null);
            question.setOptionD(null);
            System.out.println("非选择题，选项设置为null");
        }
        
        System.out.println("开始验证必填字段...");
        
        // 验证必填字段
        if (question.getSubjectId() == null || question.getSubjectId().trim().isEmpty()) {
            throw new RuntimeException("科目ID不能为空");
        }
        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
            throw new RuntimeException("题目内容不能为空");
        }
        if (question.getQuestionType() == null || question.getQuestionType().trim().isEmpty()) {
            throw new RuntimeException("题目类型不能为空");
        }
        if (question.getDifficulty() == null || question.getDifficulty().trim().isEmpty()) {
            throw new RuntimeException("难度不能为空");
        }
        
        System.out.println("必填字段验证通过");
        
        validateQuestionType(question.getQuestionType());
        System.out.println("题目类型验证通过: " + question.getQuestionType());
        
        validateDifficulty(question.getDifficulty());
        System.out.println("难度验证通过: " + question.getDifficulty());
        
        System.out.println("准备保存到数据库...");
        Question savedQuestion = questionRepository.save(question);
        System.out.println("保存成功，返回题目ID: " + savedQuestion.getId());
        
        return savedQuestion;
    }
    
    @Override
    public Question updateQuestion(String id, Question question) {
        Question existingQuestion = questionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("题目不存在"));
        
        // 验证必填字段
        if (question.getSubjectId() == null || question.getSubjectId().trim().isEmpty()) {
            throw new RuntimeException("科目ID不能为空");
        }
        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
            throw new RuntimeException("题目内容不能为空");
        }
        if (question.getQuestionType() == null || question.getQuestionType().trim().isEmpty()) {
            throw new RuntimeException("题目类型不能为空");
        }
        if (question.getDifficulty() == null || question.getDifficulty().trim().isEmpty()) {
            throw new RuntimeException("难度不能为空");
        }
        
        existingQuestion.setSubjectId(question.getSubjectId());
        existingQuestion.setContent(question.getContent());
        existingQuestion.setQuestionType(question.getQuestionType());
        existingQuestion.setOptionA(question.getOptionA());
        existingQuestion.setOptionB(question.getOptionB());
        existingQuestion.setOptionC(question.getOptionC());
        existingQuestion.setOptionD(question.getOptionD());
        existingQuestion.setAnswer(question.getAnswer());
        existingQuestion.setDifficulty(question.getDifficulty());
        existingQuestion.setKnowledgeTag(question.getKnowledgeTag());
        existingQuestion.setStatus(question.getStatus());
        // 设置更新时间
        existingQuestion.setUpdatedAt(LocalDateTime.now());
        
        validateQuestionType(question.getQuestionType());
        validateDifficulty(question.getDifficulty());
        
        return questionRepository.save(existingQuestion);
    }
    
    @Override
    public void deleteQuestion(String id) {
        if (!questionRepository.existsById(id)) {
            throw new RuntimeException("题目不存在");
        }
        questionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public int clearQuestionsBySubject(String subjectId) {
        if (subjectId == null || subjectId.trim().isEmpty()) {
            throw new RuntimeException("科目ID不能为空");
        }
        List<Question> questions = questionRepository.findBySubjectId(subjectId);
        int count = 0;
        for (Question q : questions) {
            // 先删除依赖关系，避免外键约束
            try {
                paperQuestionRepository.deleteByQuestionId(q.getId());
            } catch (Exception ignored) { }
            try {
                examSessionAnswerRepository.deleteByQuestionId(q.getId());
            } catch (Exception ignored) { }
            questionRepository.deleteById(q.getId());
            count++;
        }
        return count;
    }
    
    @Override
    public Question getQuestionById(String id) {
        return questionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("题目不存在"));
    }
    
    @Override
    public Page<Question> searchQuestions(String subjectId, String questionId, String questionType, String difficulty, String search, Pageable pageable) {
        try {
            // 构建查询条件
            Specification<Question> spec = Specification.where(null);
            
            // 科目条件
            if (subjectId != null && !subjectId.trim().isEmpty()) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("subjectId"), subjectId));
            }
            
            // 题目编号条件
            if (questionId != null && !questionId.trim().isEmpty()) {
                spec = spec.and((root, query, cb) -> cb.like(root.get("id"), "%" + questionId.trim() + "%"));
            }
            
            // 题型条件
            if (questionType != null && !questionType.trim().isEmpty()) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("questionType"), questionType));
            }
            
            // 难度条件
            if (difficulty != null && !difficulty.trim().isEmpty()) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("difficulty"), difficulty));
            }
            
            // 搜索条件（支持内容搜索）
            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.trim() + "%";
                spec = spec.and((root, query, cb) -> 
                    cb.or(
                        cb.like(root.get("content"), searchTerm),
                        cb.like(root.get("knowledgeTag"), searchTerm)
                    )
                );
            }
            
            // 只查询启用的题目
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), "启用"));
            
            // 使用JpaSpecificationExecutor的findAll方法
            return questionRepository.findAll(spec, pageable);
            
        } catch (Exception e) {
            // 如果Specification查询失败，回退到简单查询
            System.err.println("Specification查询失败，使用回退查询: " + e.getMessage());
            return fallbackSearch(subjectId, questionId, questionType, difficulty, search, pageable);
        }
    }
    
    // 回退查询方法
    private Page<Question> fallbackSearch(String subjectId, String questionId, String questionType, String difficulty, String search, Pageable pageable) {
        // 使用现有的复杂查询方法作为回退
        return questionRepository.findByComplexCriteria(subjectId, questionId, questionType, difficulty, "启用", search, pageable);
    }
    
    @Override
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }
    
    @Override
    public Page<Question> getAllQuestionsPageable(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }
    
    @Override
    public ImportResult importQuestionsFromFile(MultipartFile file, String subjectId) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                return ImportResult.failure("文件名不能为空");
            }
            
            // 验证科目ID
            if (subjectId == null || subjectId.trim().isEmpty()) {
                return ImportResult.failure("科目ID不能为空");
            }
            
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            
            switch (fileExtension) {
                case "txt":
                    return importQuestionsFromTxt(file, subjectId);
                case "xlsx":
                case "xls":
                case "csv":
                    return importQuestionsFromExcel(file, subjectId);
                default:
                    return ImportResult.failure("不支持的文件格式，请上传TXT、Excel或CSV文件");
            }
        } catch (Exception e) {
            e.printStackTrace(); // 添加调试信息
            return ImportResult.failure("文件导入失败：" + e.getMessage());
        }
    }
    
    private ImportResult importQuestionsFromTxt(MultipartFile file, String subjectId) {
        try {
            String content = new String(file.getBytes(), "UTF-8");
            String[] lines = content.split("\n");
            int successCount = 0;
            int failureCount = 0;
            List<String> errorMessages = new ArrayList<>();
            
            // 存储当前正在解析的题目信息
            String currentType = "";
            StringBuilder currentContent = new StringBuilder();
            StringBuilder currentAnswer = new StringBuilder();
            String currentDifficulty = "";
            String currentKnowledgeTag = "";
            boolean isReadingContent = false;
            boolean isReadingAnswer = false;
            boolean hasValidQuestion = false;
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                
                // 跳过空行
                if (line.isEmpty()) {
                    continue;
                }
                
                // 检查是否是新的题目开始
                if (line.startsWith("题型：")) {
                    // 保存前一个题目（如果存在）
                    if (hasValidQuestion) {
                        try {
                            insertQuestion(currentType, currentContent.toString(), currentAnswer.toString(), 
                                         currentDifficulty, currentKnowledgeTag, subjectId);
                            successCount++;
                        } catch (Exception e) {
                            failureCount++;
                            errorMessages.add("第" + (i - 1) + "行附近题目导入失败: " + e.getMessage());
                        }
                    }
                    
                    // 开始新题目
                    currentType = line.substring(3).trim();
                    currentContent.setLength(0);
                    currentAnswer.setLength(0);
                    currentDifficulty = "";
                    currentKnowledgeTag = "";
                    isReadingContent = false;
                    isReadingAnswer = false;
                    hasValidQuestion = false;
                    
                    // 验证题型
                    if (!isValidQuestionType(currentType)) {
                        errorMessages.add("第" + (i + 1) + "行：无效的题型 '" + currentType + "'");
                        failureCount++;
                        continue;
                    }
                    
                } else if (line.startsWith("内容：")) {
                    isReadingContent = true;
                    isReadingAnswer = false;
                    String contentText = line.substring(3).trim();
                    currentContent.append(contentText);
                    hasValidQuestion = true;
                    
                } else if (line.startsWith("难度：")) {
                    isReadingContent = false;
                    isReadingAnswer = false;
                    currentDifficulty = line.substring(3).trim();
                    
                    // 验证难度
                    if (!isValidDifficulty(currentDifficulty)) {
                        errorMessages.add("第" + (i + 1) + "行：无效的难度 '" + currentDifficulty + "'");
                    }
                    
                } else if (line.startsWith("标签：")) {
                    isReadingContent = false;
                    isReadingAnswer = false;
                    currentKnowledgeTag = line.substring(3).trim();
                    
                } else if (line.startsWith("答案：")) {
                    isReadingContent = false;
                    isReadingAnswer = true;
                    String answerText = line.substring(3).trim();
                    currentAnswer.append(answerText);
                    
                } else if (isReadingContent) {
                    // 继续读取内容，保持原始格式（不trim，保留缩进）
                    currentContent.append("\n").append(lines[i]);
                    
                } else if (isReadingAnswer) {
                    // 继续读取答案，保持原始格式（不trim，保留缩进）
                    currentAnswer.append("\n").append(lines[i]);
                }
            }
            
            // 处理最后一个题目
            if (hasValidQuestion) {
                try {
                    insertQuestion(currentType, currentContent.toString(), currentAnswer.toString(), 
                                 currentDifficulty, currentKnowledgeTag, subjectId);
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    errorMessages.add("最后一个题目导入失败: " + e.getMessage());
                }
            }
            
            // 构建结果消息
            StringBuilder resultMessage = new StringBuilder();
            resultMessage.append("成功导入 ").append(successCount).append(" 道题目");
            if (failureCount > 0) {
                resultMessage.append("，失败 ").append(failureCount).append(" 道题目");
                if (!errorMessages.isEmpty()) {
                    resultMessage.append("\n错误详情：\n");
                    for (String error : errorMessages) {
                        resultMessage.append(error).append("\n");
                    }
                }
            }
            
            return new ImportResult(true, resultMessage.toString(), successCount, failureCount);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ImportResult.failure("TXT文件解析失败：" + e.getMessage());
        }
    }
    
    private void insertQuestion(String type, String content, String answer, String difficulty, String knowledgeTag, String subjectId) {
        try {
            // 根据题型设置questionType
            String typeCode = convertQuestionType(type);
            
            // 转换难度
            String difficultyCode = convertDifficulty(difficulty);
            
            // 生成题目ID
            String questionId = generateQuestionId(typeCode);
            
            // 创建题目对象
            Question question = new Question();
            question.setId(questionId);
            question.setSubjectId(subjectId);
            // 保持代码格式，只移除首尾空行，不移除缩进
            question.setContent(preserveCodeFormat(content));
            question.setQuestionType(typeCode);
            // 保持答案格式，只移除首尾空行，不移除缩进
            question.setAnswer(preserveCodeFormat(answer));
            question.setDifficulty(difficultyCode);
            question.setKnowledgeTag(knowledgeTag);
            question.setStatus("启用");
            question.setCreatedAt(LocalDateTime.now());
            question.setUpdatedAt(LocalDateTime.now());
            
            // 处理选择题选项
            if ("C".equals(typeCode)) {
                String[] parsedOptions = parseOptionsToArray(content);
                question.setOptionA(parsedOptions[0]);
                question.setOptionB(parsedOptions[1]);
                question.setOptionC(parsedOptions[2]);
                question.setOptionD(parsedOptions[3]);
                
                // 清理题目内容，移除选项部分
                String cleanedContent = cleanContentFromOptions(content);
                question.setContent(cleanedContent);
                
                System.out.println("选择题选项已解析: A=" + parsedOptions[0] + ", B=" + parsedOptions[1] + ", C=" + parsedOptions[2] + ", D=" + parsedOptions[3]);
                System.out.println("清理后的题目内容: " + cleanedContent);
            } else {
                question.setOptionA(null);
                question.setOptionB(null);
                question.setOptionC(null);
                question.setOptionD(null);
            }
            
            // 验证必填字段
            validateQuestionData(question);
            
            // 保存到数据库
            questionRepository.save(question);
            
        } catch (Exception e) {
            System.err.println("插入题目失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    private String convertQuestionType(String type) {
        switch (type) {
            case "选择题":
                return "C";
            case "填空题":
                return "F";
            case "程序运行结果题":
                return "R";
            case "简答题":
                return "S";
            case "编程题":
                return "P";
            default:
                return "S"; // 默认为简答题
        }
    }
    
    private String convertDifficulty(String difficulty) {
        switch (difficulty) {
            case "简单":
                return "easy";
            case "中等":
                return "medium";
            case "困难":
                return "hard";
            default:
                return "medium"; // 默认为中等
        }
    }
    
    private boolean isValidQuestionType(String type) {
        return Arrays.asList("选择题", "填空题", "程序运行结果题", "简答题", "编程题").contains(type);
    }
    
    private boolean isValidDifficulty(String difficulty) {
        return Arrays.asList("简单", "中等", "困难").contains(difficulty);
    }
    
    /**
     * 保持代码格式，只移除首尾空行，保留所有缩进
     */
    private String preserveCodeFormat(String text) {
        if (text == null) {
            return "";
        }
        
        // 按行分割
        String[] lines = text.split("\n");
        
        // 找到第一个非空行和最后一个非空行
        int firstNonEmptyLine = 0;
        int lastNonEmptyLine = lines.length - 1;
        
        // 找到第一个非空行
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].trim().isEmpty()) {
                firstNonEmptyLine = i;
                break;
            }
        }
        
        // 找到最后一个非空行
        for (int i = lines.length - 1; i >= 0; i--) {
            if (!lines[i].trim().isEmpty()) {
                lastNonEmptyLine = i;
                break;
            }
        }
        
        // 如果没有非空行，返回空字符串
        if (firstNonEmptyLine > lastNonEmptyLine) {
            return "";
        }
        
        // 重新组装，保持原始缩进
        StringBuilder result = new StringBuilder();
        for (int i = firstNonEmptyLine; i <= lastNonEmptyLine; i++) {
            if (i > firstNonEmptyLine) {
                result.append("\n");
            }
            result.append(lines[i]); // 保持原始行内容，包括缩进
        }
        
        return result.toString();
    }
    
    private void validateQuestionData(Question question) {
        if (question.getSubjectId() == null || question.getSubjectId().trim().isEmpty()) {
            throw new RuntimeException("科目ID不能为空");
        }
        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
            throw new RuntimeException("题目内容不能为空");
        }
        if (question.getQuestionType() == null || question.getQuestionType().trim().isEmpty()) {
            throw new RuntimeException("题目类型不能为空");
        }
        if (question.getDifficulty() == null || question.getDifficulty().trim().isEmpty()) {
            throw new RuntimeException("难度不能为空");
        }
        
        validateQuestionType(question.getQuestionType());
        validateDifficulty(question.getDifficulty());
    }
    
    private ImportResult importQuestionsFromExcel(MultipartFile file, String subjectId) {
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = null;
            
            // 根据文件扩展名选择合适的工作簿类型
            String fileName = file.getOriginalFilename();
            if (fileName != null) {
                if (fileName.endsWith(".xlsx")) {
                    workbook = new XSSFWorkbook(inputStream);
                } else if (fileName.endsWith(".xls")) {
                    workbook = new HSSFWorkbook(inputStream);
                } else {
                    return ImportResult.failure("不支持的文件格式，请使用.xlsx或.xls文件");
                }
            } else {
                return ImportResult.failure("文件名不能为空");
            }
            
            Sheet sheet = workbook.getSheetAt(0); // 获取第一个工作表
            int successCount = 0;
            int failureCount = 0;
            List<String> errorMessages = new ArrayList<>();
            
            // 检查是否有标题行
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return ImportResult.failure("Excel文件格式错误：缺少标题行");
            }
            
            // 从第二行开始读取数据（假设第一行是标题）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue; // 跳过空行
                }
                
                try {
                    // 解析Excel行数据
                    Question question = parseExcelRow(row, subjectId);
                    if (question != null) {
                        // 验证题目数据
                        validateQuestionData(question);
                        
                        // 保存到数据库
                        questionRepository.save(question);
                        successCount++;
                    } else {
                        failureCount++;
                        errorMessages.add("第" + (i + 1) + "行：题目数据不完整");
                    }
                } catch (Exception e) {
                    failureCount++;
                    errorMessages.add("第" + (i + 1) + "行：导入失败 - " + e.getMessage());
                }
            }
            
            workbook.close();
            inputStream.close();
            
            // 构建结果消息
            StringBuilder resultMessage = new StringBuilder();
            resultMessage.append("成功导入 ").append(successCount).append(" 道题目");
            if (failureCount > 0) {
                resultMessage.append("，失败 ").append(failureCount).append(" 道题目");
                if (!errorMessages.isEmpty()) {
                    resultMessage.append("\n错误详情：\n");
                    for (String error : errorMessages) {
                        resultMessage.append(error).append("\n");
                    }
                }
            }
            
            return new ImportResult(true, resultMessage.toString(), successCount, failureCount);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ImportResult.failure("Excel文件解析失败：" + e.getMessage());
        }
    }
    
    // 解析Excel行数据
    private Question parseExcelRow(Row row, String subjectId) {
        try {
            Question question = new Question();
            
            // 题目ID（可选，如果为空则自动生成）
            Cell idCell = row.getCell(0);
            if (idCell != null && getCellValueAsString(idCell) != null && !getCellValueAsString(idCell).trim().isEmpty()) {
                question.setId(getCellValueAsString(idCell).trim());
            } else {
                // 先设置一个临时ID，稍后生成
                question.setId(null);
            }
            
            // 科目ID（从参数传入，不从Excel读取）
            question.setSubjectId(subjectId);
            
            // 题目内容（第3列，索引2）
            Cell contentCell = row.getCell(2);
            if (contentCell == null || getCellValueAsString(contentCell) == null || getCellValueAsString(contentCell).trim().isEmpty()) {
                return null; // 题目内容不能为空
            }
            question.setContent(getCellValueAsString(contentCell).trim());
            
            // 题型（第4列，索引3）
            Cell typeCell = row.getCell(3);
            if (typeCell == null || getCellValueAsString(typeCell) == null || getCellValueAsString(typeCell).trim().isEmpty()) {
                return null; // 题型不能为空
            }
            String questionType = convertQuestionTypeFromDisplay(getCellValueAsString(typeCell).trim());
            question.setQuestionType(questionType);
            
            // 选项A-D（第5-8列，索引4-7，仅选择题需要）
            if ("C".equals(questionType)) {
                Cell optionACell = row.getCell(4);
                Cell optionBCell = row.getCell(5);
                Cell optionCCell = row.getCell(6);
                Cell optionDCell = row.getCell(7);
                
                question.setOptionA(optionACell != null ? getCellValueAsString(optionACell) : null);
                question.setOptionB(optionBCell != null ? getCellValueAsString(optionBCell) : null);
                question.setOptionC(optionCCell != null ? getCellValueAsString(optionCCell) : null);
                question.setOptionD(optionDCell != null ? getCellValueAsString(optionDCell) : null);
            } else {
                question.setOptionA(null);
                question.setOptionB(null);
                question.setOptionC(null);
                question.setOptionD(null);
            }
            
            // 答案（第9列，索引8）
            Cell answerCell = row.getCell(8);
            question.setAnswer(answerCell != null ? getCellValueAsString(answerCell) : null);
            
            // 难度（第10列，索引9）
            Cell difficultyCell = row.getCell(9);
            if (difficultyCell == null || getCellValueAsString(difficultyCell) == null || getCellValueAsString(difficultyCell).trim().isEmpty()) {
                return null; // 难度不能为空
            }
            String difficulty = convertDifficultyFromDisplay(getCellValueAsString(difficultyCell).trim());
            question.setDifficulty(difficulty);
            
            // 知识点标签（第11列，索引10）
            Cell knowledgeTagCell = row.getCell(10);
            question.setKnowledgeTag(knowledgeTagCell != null ? getCellValueAsString(knowledgeTagCell) : null);
            
            // 状态（第12列，索引11，默认为启用）
            Cell statusCell = row.getCell(11);
            String status = statusCell != null ? getCellValueAsString(statusCell) : "启用";
            question.setStatus(status != null && !status.trim().isEmpty() ? status.trim() : "启用");
            
            // 创建时间和更新时间（第13-14列，索引12-13，可选）
            // 如果Excel中有时间数据，可以解析；否则使用当前时间
            Cell createdAtCell = row.getCell(12);
            Cell updatedAtCell = row.getCell(13);
            
            if (createdAtCell != null && getCellValueAsString(createdAtCell) != null && !getCellValueAsString(createdAtCell).trim().isEmpty()) {
                try {
                    // 尝试解析时间格式
                    String timeStr = getCellValueAsString(createdAtCell).trim();
                    // 这里可以根据实际的时间格式进行解析
                    // 暂时使用当前时间
                    question.setCreatedAt(LocalDateTime.now());
                } catch (Exception e) {
                    question.setCreatedAt(LocalDateTime.now());
                }
            } else {
                question.setCreatedAt(LocalDateTime.now());
            }
            
            if (updatedAtCell != null && getCellValueAsString(updatedAtCell) != null && !getCellValueAsString(updatedAtCell).trim().isEmpty()) {
                try {
                    // 尝试解析时间格式
                    String timeStr = getCellValueAsString(updatedAtCell).trim();
                    // 这里可以根据实际的时间格式进行解析
                    // 暂时使用当前时间
                    question.setUpdatedAt(LocalDateTime.now());
                } catch (Exception e) {
                    question.setUpdatedAt(LocalDateTime.now());
                }
            } else {
                question.setUpdatedAt(LocalDateTime.now());
            }
            
            // 如果ID为空，生成新的ID
            if (question.getId() == null || question.getId().trim().isEmpty()) {
                String questionId = generateQuestionId(question.getQuestionType());
                question.setId(questionId);
            }
            
            return question;
            
        } catch (Exception e) {
            System.err.println("解析Excel行失败: " + e.getMessage());
            return null;
        }
    }
    
    // 获取单元格值作为字符串
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 处理数字，避免科学计数法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    // 从显示名称转换为题型代码
    private String convertQuestionTypeFromDisplay(String displayName) {
        switch (displayName) {
            case "选择题":
                return "C";
            case "填空题":
                return "F";
            case "程序运行结果题":
                return "R";
            case "简答题":
                return "S";
            case "编程题":
                return "P";
            default:
                // 如果已经是代码格式，直接返回
                if (Arrays.asList("C", "F", "R", "S", "P").contains(displayName)) {
                    return displayName;
                }
                return "S"; // 默认为简答题
        }
    }
    
    // 从显示名称转换为难度代码
    private String convertDifficultyFromDisplay(String displayName) {
        switch (displayName) {
            case "简单":
                return "easy";
            case "中等":
                return "medium";
            case "困难":
                return "hard";
            default:
                // 如果已经是代码格式，直接返回
                if (Arrays.asList("easy", "medium", "hard").contains(displayName)) {
                    return displayName;
                }
                return "medium"; // 默认为中等
        }
    }
    

    
    @Override
    public void exportQuestions(String subjectId, String questionType, String difficulty, 
                              String status, HttpServletResponse response) {
        try {
            // 构建查询条件
            Specification<Question> spec = Specification.where(null);
            
            // 科目条件
            if (subjectId != null && !subjectId.trim().isEmpty()) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("subjectId"), subjectId));
            }
            
            // 题型条件
            if (questionType != null && !questionType.trim().isEmpty()) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("questionType"), questionType));
            }
            
            // 难度条件
            if (difficulty != null && !difficulty.trim().isEmpty()) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("difficulty"), difficulty));
            }
            
            // 状态条件
            if (status != null && !status.trim().isEmpty()) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
            } else {
                // 默认只导出启用的题目
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), "启用"));
            }
            
            // 查询题目列表
            List<Question> questions = questionRepository.findAll(spec);
            
            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("题目列表");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"题目ID", "科目ID", "题目内容", "题型", "选项A", "选项B", "选项C", "选项D", "答案", "难度", "知识点标签", "状态", "创建时间", "更新时间"};
            
            // 创建标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // 设置标题行
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 创建内容样式
            CellStyle contentStyle = workbook.createCellStyle();
            contentStyle.setWrapText(true);
            contentStyle.setVerticalAlignment(VerticalAlignment.TOP);
            
            // 填充数据
            int rowNum = 1;
            for (Question question : questions) {
                Row row = sheet.createRow(rowNum++);
                
                // 题目ID
                createCell(row, 0, question.getId(), contentStyle);
                
                // 科目ID
                createCell(row, 1, question.getSubjectId(), contentStyle);
                
                // 题目内容
                createCell(row, 2, question.getContent(), contentStyle);
                
                // 题型
                createCell(row, 3, question.getQuestionTypeDisplayName(), contentStyle);
                
                // 选项A
                createCell(row, 4, question.getOptionA(), contentStyle);
                
                // 选项B
                createCell(row, 5, question.getOptionB(), contentStyle);
                
                // 选项C
                createCell(row, 6, question.getOptionC(), contentStyle);
                
                // 选项D
                createCell(row, 7, question.getOptionD(), contentStyle);
                
                // 答案
                createCell(row, 8, question.getAnswer(), contentStyle);
                
                // 难度
                createCell(row, 9, question.getDifficultyDisplayName(), contentStyle);
                
                // 知识点标签
                createCell(row, 10, question.getKnowledgeTag(), contentStyle);
                
                // 状态
                createCell(row, 11, question.getStatus(), contentStyle);
                
                // 创建时间
                createCell(row, 12, question.getCreatedAt() != null ? question.getCreatedAt().toString() : "", contentStyle);
                
                // 更新时间
                createCell(row, 13, question.getUpdatedAt() != null ? question.getUpdatedAt().toString() : "", contentStyle);
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 设置最大列宽，避免过宽
                if (sheet.getColumnWidth(i) > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            
            // 使用URL编码处理中文文件名，避免Tomcat编码问题
            String fileName = "questions_export.xlsx";
            String encodedFileName = java.net.URLEncoder.encode("题目列表.xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName);
            
            // 写入响应流
            workbook.write(response.getOutputStream());
            workbook.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            // 如果导出失败，返回错误信息
            try {
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("导出失败：" + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    
    // 辅助方法：创建单元格
    private void createCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }
    
    @Override
    public long getQuestionCount() {
        return questionRepository.count();
    }
    
    @Override
    public long getQuestionCount(String subjectId) {
        if (subjectId != null && !subjectId.isEmpty()) {
            return questionRepository.countBySubjectId(subjectId);
        }
        return questionRepository.count();
    }
    
    @Override
    public Map<String, Object> getQuestionStatistics(String subjectId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 题目总数
        long totalCount = getQuestionCount(subjectId);
        statistics.put("totalCount", totalCount);
        
        // 启用题目数量
        long enabledCount = questionRepository.countByStatusAndSubjectId("启用", subjectId);
        statistics.put("enabledCount", enabledCount);
        
        // 待审核题目数量
        long pendingCount = questionRepository.countByStatusAndSubjectId("待审核", subjectId);
        statistics.put("pendingCount", pendingCount);
        
        // 知识点标签数量
        long knowledgeTagCount = questionRepository.countDistinctKnowledgeTagsBySubjectId(subjectId);
        statistics.put("knowledgeTagCount", knowledgeTagCount);
        
        // 题目类型统计
        List<Object[]> typeCounts = questionRepository.countByQuestionTypeAndSubjectId(subjectId);
        Map<String, Long> typeStatistics = new HashMap<>();
        for (Object[] typeCount : typeCounts) {
            typeStatistics.put((String) typeCount[0], (Long) typeCount[1]);
        }
        statistics.put("typeStatistics", typeStatistics);
        
        // 难度统计
        List<Object[]> difficultyCounts = questionRepository.countByDifficultyAndSubjectId(subjectId);
        Map<String, Long> difficultyStatistics = new HashMap<>();
        for (Object[] difficultyCount : difficultyCounts) {
            difficultyStatistics.put((String) difficultyCount[0], (Long) difficultyCount[1]);
        }
        statistics.put("difficultyStatistics", difficultyStatistics);
        
        return statistics;
    }
    
    @Override
    public List<String> getAllKnowledgeTags() {
        return questionRepository.findAllKnowledgeTags();
    }
    
    @Override
    public List<String> getKnowledgeTagsBySubjectId(String subjectId) {
        if (subjectId != null && !subjectId.isEmpty()) {
            return questionRepository.findKnowledgeTagsBySubjectId(subjectId);
        }
        return questionRepository.findAllKnowledgeTags();
    }
    
    @Override
    public List<Question> findRandomQuestionsByTypeAndDifficulty(String subjectId, 
                                                               String questionType, 
                                                               String difficulty, 
                                                               int count) {
        return questionRepository.findRandomQuestionsByTypeAndDifficulty(subjectId, questionType, difficulty, count);
    }
    
    private void validateQuestionType(String questionType) {
        Set<String> validTypes = Set.of("C", "F", "R", "S", "P");
        if (questionType == null || questionType.trim().isEmpty()) {
            throw new RuntimeException("请选择题目类型");
        }
        if (!validTypes.contains(questionType)) {
            throw new RuntimeException("无效的题目类型: " + questionType);
        }
    }
    
    private void validateDifficulty(String difficulty) {
        Set<String> validDifficulties = Set.of("easy", "medium", "hard");
        if (difficulty == null || difficulty.trim().isEmpty()) {
            throw new RuntimeException("请选择难度级别");
        }
        if (!validDifficulties.contains(difficulty)) {
            throw new RuntimeException("无效的难度级别: " + difficulty);
        }
    }
    
    @Override
    public long getQuestionCountByCreator(String creatorId) {
        // 由于Question实体没有creatorId字段，这里返回总数
        // 实际项目中应该添加creatorId字段到Question实体
        return questionRepository.count();
    }
    
    /**
     * 自动生成题目ID
     * 格式：题目类型前缀 + 6位数字序号
     */
    private String generateQuestionId(String questionType) {
        String prefix = "";
        switch (questionType) {
            case "C": prefix = "C"; break; // 选择题
            case "F": prefix = "F"; break; // 填空题
            case "R": prefix = "R"; break; // 判断题
            case "S": prefix = "S"; break; // 简答题
            case "P": prefix = "P"; break; // 编程题
            default: prefix = "Q"; break;  // 其他类型
        }
        
        // 获取当前类型的题目数量，生成下一个序号
        long count = questionRepository.countByQuestionTypeStartingWith(questionType);
        String sequence = String.format("%06d", count + 1);
        
        return prefix + sequence;
    }
    
    // 从题目内容中解析选择题选项到数组
    private String[] parseOptionsToArray(String content) {
        String[] options = new String[4]; // A, B, C, D
        if (content == null || content.trim().isEmpty()) {
            return options;
        }
        
        String[] lines = content.split("\n");
        
        // 首先尝试从多行格式解析（每行一个选项）
        for (String line : lines) {
            line = line.trim();
            // 匹配 A. 选项内容 格式
            if (line.matches("^[A-Z]\\.\\s*.+$")) {
                String letter = line.substring(0, 1);
                String optionContent = line.substring(2).trim();
                
                switch (letter) {
                    case "A": options[0] = optionContent; break;
                    case "B": options[1] = optionContent; break;
                    case "C": options[2] = optionContent; break;
                    case "D": options[3] = optionContent; break;
                }
            }
        }
        
        // 如果没有找到选项，尝试从单行内容中解析
        if (options[0] == null && options[1] == null && options[2] == null && options[3] == null) {
            String pattern = "([A-Z])\\.\\s*([^A-Z]+?)(?=[A-Z]\\.|$)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(content);
            
            while (m.find()) {
                String letter = m.group(1);
                String optionContent = m.group(2).trim();
                
                switch (letter) {
                    case "A": options[0] = optionContent; break;
                    case "B": options[1] = optionContent; break;
                    case "C": options[2] = optionContent; break;
                    case "D": options[3] = optionContent; break;
                }
            }
        }
        
        return options;
    }
    
    // 从题目内容中移除选项部分，只保留题目本身
    private String cleanContentFromOptions(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }
        
        String[] lines = content.split("\n");
        StringBuilder cleanedContent = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            // 如果不是选项行（不以 A. B. C. D. 等开头），则保留
            if (!line.matches("^[A-Z]\\.\\s*.+$")) {
                if (cleanedContent.length() > 0) {
                    cleanedContent.append("\n");
                }
                cleanedContent.append(line);
            }
        }
        
        // 如果多行格式没有找到选项，尝试从单行格式中移除选项
        if (cleanedContent.length() == 0 || cleanedContent.toString().trim().isEmpty()) {
            // 找到第一个选项的位置，只保留选项之前的内容
            String pattern = "([A-Z])\\.\\s*";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(content);
            
            if (m.find()) {
                // 找到第一个选项的位置，截取之前的内容
                int firstOptionIndex = m.start();
                String beforeOptions = content.substring(0, firstOptionIndex).trim();
                return beforeOptions;
            }
        }
        
        return cleanedContent.toString().trim();
    }
} 