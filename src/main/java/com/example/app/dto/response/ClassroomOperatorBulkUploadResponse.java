package com.example.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result response after processing an Excel bulk upload for classroom operators.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomOperatorBulkUploadResponse {

    private int totalProcessed;
    private int successCount;
    private int skippedCount;
    private List<String> skippedEmails;
    private List<ClassroomOperatorResponse> operators;
    private String message;
}

