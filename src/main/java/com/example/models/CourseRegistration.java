package com.example.models;

import java.time.Instant;
import java.util.UUID;

public class CourseRegistration {
    private String id;
    private String studentId;
    private String courseId;
    private String status; // "pending", "approved", "rejected"
    private String createdAt;
    private String updatedAt;

    public CourseRegistration() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now().toString();
        this.updatedAt = Instant.now().toString();
        this.status = "pending";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
} 