package com.example.jobtracker.controller;

import com.example.jobtracker.dto.ApiMessageDto;
import com.example.jobtracker.dto.CreateNoteRequestDto;
import com.example.jobtracker.dto.JobApplicationDto;
import com.example.jobtracker.dto.UpdateStatusRequestDto;
import com.example.jobtracker.service.JobApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationRestController {

    private final JobApplicationService jobApplicationService;

    public JobApplicationRestController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @GetMapping
    public List<JobApplicationDto> all() {
        return jobApplicationService.findAllForCurrentUser();
    }

    @GetMapping("/{id}")
    public JobApplicationDto one(@PathVariable Long id) {
        return jobApplicationService.findOneForCurrentUser(id);
    }

    @PatchMapping("/{id}/status")
    public JobApplicationDto updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequestDto request) {
        return jobApplicationService.updateStatusForCurrentUser(id, request.getStatus());
    }

    @PostMapping("/{id}/notes")
    public JobApplicationDto addNote(
            @PathVariable Long id,
            @RequestBody CreateNoteRequestDto request) {
        return jobApplicationService.addNoteForCurrentUser(id, request.getText());
    }

    @DeleteMapping("/{id}")
    public ApiMessageDto delete(@PathVariable Long id) {
        jobApplicationService.deleteForCurrentUser(id);
        return new ApiMessageDto("Заявку видалено.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiMessageDto> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiMessageDto(exception.getMessage()));
    }
}
