package com.example.jobtracker.dto;

import com.example.jobtracker.enums.ApplicationStatus;

public class UpdateStatusRequestDto {

    private ApplicationStatus status;

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}
