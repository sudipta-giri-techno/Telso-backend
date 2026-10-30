package com.example.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result response after processing an Excel bulk upload for students.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentBulkUploadResponse {

    private int totalProcessed;
    private int successCount;
    private int skippedCount;
    private List<String> skippedRollNumbers;
    private List<StudentResponse> students;
    private String message;
}

