package com.exam.dto;

import java.util.List;
import java.util.ArrayList;

public class ImportResult {
    private boolean success;
    private int successCount;
    private int errorCount;
    private String message;
    private List<String> errorDetails;

    // 构造函数
    public ImportResult() {
        this.errorDetails = new ArrayList<>();
    }

    public ImportResult(boolean success, String message, int successCount, int errorCount) {
        this.success = success;
        this.message = message;
        this.successCount = successCount;
        this.errorCount = errorCount;
        this.errorDetails = new ArrayList<>();
    }

    // Getter和Setter方法
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(List<String> errorDetails) {
        this.errorDetails = errorDetails;
    }

    public void addErrorDetail(String error) {
        if (this.errorDetails == null) {
            this.errorDetails = new ArrayList<>();
        }
        this.errorDetails.add(error);
    }

    // 静态工厂方法
    public static ImportResult success(int count) {
        return new ImportResult(true, "导入成功", count, 0);
    }
    
    public static ImportResult success(int successCount, int failureCount) {
        String message = "导入完成";
        if (failureCount > 0) {
            message += "，成功 " + successCount + " 道题目，失败 " + failureCount + " 道题目";
        } else {
            message += "，成功导入 " + successCount + " 道题目";
        }
        return new ImportResult(true, message, successCount, failureCount);
    }

    public static ImportResult failure(String error) {
        return new ImportResult(false, error, 0, 1);
    }
} 