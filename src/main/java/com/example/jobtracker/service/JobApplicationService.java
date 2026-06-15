package com.example.jobtracker.service;

import com.example.jobtracker.dto.ApplicationNoteDto;
import com.example.jobtracker.dto.JobApplicationDto;
import com.example.jobtracker.dto.JobSearchResultDto;
import com.example.jobtracker.entity.ApplicationNote;
import com.example.jobtracker.entity.JobApplication;
import com.example.jobtracker.entity.User;
import com.example.jobtracker.enums.ApplicationPriority;
import com.example.jobtracker.enums.ApplicationStatus;
import com.example.jobtracker.repository.ApplicationNoteRepository;
import com.example.jobtracker.repository.JobApplicationRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final ApplicationNoteRepository applicationNoteRepository;
    private final CurrentUserService currentUserService;

    public JobApplicationService(
            JobApplicationRepository jobApplicationRepository,
            ApplicationNoteRepository applicationNoteRepository,
            CurrentUserService currentUserService) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.applicationNoteRepository = applicationNoteRepository;
        this.currentUserService = currentUserService;
    }

    public JobApplicationDto saveFromSearchResult(JobSearchResultDto result) {
        User user = currentUserService.getCurrentUser();
        String sourceUrl = defaultText(result.getSourceUrl(), "");
        if (hasText(sourceUrl)) {
            Optional<JobApplication> existingApplication = jobApplicationRepository.findByUserAndSourceUrl(user, sourceUrl);
            if (existingApplication.isPresent()) {
                return toDto(existingApplication.get());
            }
        }

        JobApplication application = new JobApplication();
        application.setTitle(defaultText(result.getTitle(), "Вакансія без назви"));
        application.setCompany(defaultText(result.getCompany(), "Невідома компанія"));
        application.setLocation(defaultText(result.getLocation(), "Віддалено або не вказано"));
        application.setDescription(result.getDescription());
        application.setSourceUrl(sourceUrl);
        application.setSalary(result.getSalary());
        application.setJobType(result.getJobType());
        application.setStatus(ApplicationStatus.SAVED);
        application.setPriority(ApplicationPriority.MEDIUM);
        application.setUser(user);

        return toDto(jobApplicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public boolean isSavedSourceUrl(String sourceUrl) {
        if (!hasText(sourceUrl)) {
            return false;
        }
        return jobApplicationRepository.findByUserAndSourceUrl(currentUserService.getCurrentUser(), sourceUrl.trim())
                .isPresent();
    }

    @Transactional(readOnly = true)
    public Set<String> savedSourceUrlsForCurrentUser() {
        return new HashSet<>(jobApplicationRepository.findSourceUrlsByUser(currentUserService.getCurrentUser()));
    }

    @Transactional(readOnly = true)
    public List<JobApplicationDto> findAllForCurrentUser() {
        return jobApplicationRepository.findByUserOrderByCreatedAtDesc(currentUserService.getCurrentUser())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JobApplicationDto> findForCurrentUser(ApplicationStatus status, String sortBy) {
        User user = currentUserService.getCurrentUser();
        Sort sort = toSort(sortBy);
        List<JobApplication> applications = status == null
                ? jobApplicationRepository.findByUser(user, sort)
                : jobApplicationRepository.findByUserAndStatus(user, status, sort);
        return applications
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<ApplicationStatus, List<JobApplicationDto>> groupedByStatusForCurrentUser(String sortBy) {
        Map<ApplicationStatus, List<JobApplicationDto>> grouped = new EnumMap<>(ApplicationStatus.class);
        Arrays.stream(ApplicationStatus.values())
                .forEach(status -> grouped.put(status, findForCurrentUser(status, sortBy)));
        return grouped;
    }

    @Transactional(readOnly = true)
    public JobApplicationDto findOneForCurrentUser(Long id) {
        return toDto(findEntityForCurrentUser(id));
    }

    public JobApplicationDto updateApplication(Long id, JobApplicationDto dto, String newNoteText) {
        JobApplication application = findEntityForCurrentUser(id);
        application.setStatus(dto.getStatus());
        application.setPriority(dto.getPriority());
        application.setSalary(dto.getSalary());
        application.setJobType(dto.getJobType());

        if (hasText(newNoteText)) {
            ApplicationNote note = new ApplicationNote();
            note.setText(newNoteText.trim());
            note.setApplication(application);
            application.getNotes().add(note);
        }

        return toDto(jobApplicationRepository.save(application));
    }

    public JobApplicationDto updateStatus(Long id, ApplicationStatus status) {
        JobApplication application = findEntityForCurrentUser(id);
        application.setStatus(status);
        return toDto(jobApplicationRepository.save(application));
    }

    public void deleteApplication(Long id) {
        jobApplicationRepository.delete(findEntityForCurrentUser(id));
    }

    public void deleteNote(Long applicationId, Long noteId) {
        JobApplication application = findEntityForCurrentUser(applicationId);
        ApplicationNote note = applicationNoteRepository.findByIdAndApplication(noteId, application)
                .orElseThrow(() -> new IllegalArgumentException("Нотатку не знайдено"));
        application.getNotes().remove(note);
        applicationNoteRepository.delete(note);
    }

    @Transactional(readOnly = true)
    public long countAllForCurrentUser() {
        return jobApplicationRepository.countByUser(currentUserService.getCurrentUser());
    }

    @Transactional(readOnly = true)
    public Map<ApplicationStatus, Long> countByStatusForCurrentUser() {
        User user = currentUserService.getCurrentUser();
        Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
        Arrays.stream(ApplicationStatus.values())
                .forEach(status -> counts.put(status, jobApplicationRepository.countByUserAndStatus(user, status)));
        return counts;
    }

    @Transactional(readOnly = true)
    public List<JobApplicationDto> recentApplications() {
        return jobApplicationRepository.findTop5ByUserOrderByCreatedAtDesc(currentUserService.getCurrentUser())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countActionNeededForCurrentUser() {
        User user = currentUserService.getCurrentUser();
        return jobApplicationRepository.countByUserAndStatus(user, ApplicationStatus.SAVED)
                + jobApplicationRepository.countByUserAndStatus(user, ApplicationStatus.INTERVIEW);
    }

    private JobApplication findEntityForCurrentUser(Long id) {
        return jobApplicationRepository.findByIdAndUser(id, currentUserService.getCurrentUser())
                .orElseThrow(() -> new IllegalArgumentException("Заявку не знайдено"));
    }

    public JobApplicationDto toDto(JobApplication application) {
        JobApplicationDto dto = new JobApplicationDto();
        dto.setId(application.getId());
        dto.setTitle(application.getTitle());
        dto.setCompany(application.getCompany());
        dto.setLocation(application.getLocation());
        dto.setDescription(application.getDescription());
        dto.setSourceUrl(application.getSourceUrl());
        dto.setSalary(application.getSalary());
        dto.setJobType(application.getJobType());
        dto.setStatus(application.getStatus());
        dto.setPriority(application.getPriority());
        dto.setCreatedAt(application.getCreatedAt());
        dto.setUpdatedAt(application.getUpdatedAt());
        dto.setNotes(application.getNotes().stream()
                .sorted(Comparator.comparing(ApplicationNote::getCreatedAt))
                .map(this::toNoteDto)
                .toList());
        return dto;
    }

    private ApplicationNoteDto toNoteDto(ApplicationNote note) {
        ApplicationNoteDto dto = new ApplicationNoteDto();
        dto.setId(note.getId());
        dto.setText(note.getText());
        dto.setCreatedAt(note.getCreatedAt());
        return dto;
    }

    private String defaultText(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Sort toSort(String sortBy) {
        if ("status".equals(sortBy)) {
            return Sort.by(Sort.Order.asc("status"), Sort.Order.desc("createdAt"));
        }
        if ("company".equals(sortBy)) {
            return Sort.by(Sort.Order.asc("company").ignoreCase(), Sort.Order.desc("createdAt"));
        }
        return Sort.by(Sort.Order.desc("createdAt"));
    }
}
