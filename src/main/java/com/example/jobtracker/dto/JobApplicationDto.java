package com.example.jobtracker.dto;

import com.example.jobtracker.enums.ApplicationStatus;
import com.example.jobtracker.enums.ApplicationPriority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JobApplicationDto {

    private Long id;
    private String title;
    private String company;
    private String location;
    private String description;
    private String sourceUrl;
    private String salary;
    private String jobType;
    private ApplicationStatus status = ApplicationStatus.SAVED;
    private ApplicationPriority priority = ApplicationPriority.MEDIUM;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ApplicationNoteDto> notes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getStatusLabel() {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case SAVED -> "Збережено";
            case APPLIED -> "Подано";
            case INTERVIEW -> "Співбесіда";
            case OFFER -> "Офер";
            case REJECTED -> "Відмова";
        };
    }

    public ApplicationPriority getPriority() {
        return priority;
    }

    public void setPriority(ApplicationPriority priority) {
        this.priority = priority;
    }

    public String getPriorityLabel() {
        if (priority == null) {
            return "";
        }
        return switch (priority) {
            case LOW -> "Низький";
            case MEDIUM -> "Середній";
            case HIGH -> "Високий";
        };
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ApplicationNoteDto> getNotes() {
        return notes;
    }

    public void setNotes(List<ApplicationNoteDto> notes) {
        this.notes = notes;
    }
}
