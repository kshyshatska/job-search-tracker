package com.example.jobtracker.service;

import com.example.jobtracker.dto.JobApplicationDto;
import com.example.jobtracker.dto.JobSearchResultDto;
import com.example.jobtracker.entity.JobApplication;
import com.example.jobtracker.entity.User;
import com.example.jobtracker.enums.ApplicationStatus;
import com.example.jobtracker.repository.JobApplicationRepository;
import com.example.jobtracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "student@example.com")
class JobApplicationServiceTests {

    @Autowired
    private JobApplicationService jobApplicationService;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesVacancyFromSearchResult() {
        JobApplicationDto saved = jobApplicationService.saveFromSearchResult(result("https://jooble.org/job/one"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Java Developer");
        assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.SAVED);
        assertThat(jobApplicationService.countAllForCurrentUser()).isEqualTo(1);
    }

    @Test
    void duplicateSourceUrlDoesNotCreateSecondApplication() {
        JobApplicationDto first = jobApplicationService.saveFromSearchResult(result("https://jooble.org/job/duplicate"));
        JobApplicationDto second = jobApplicationService.saveFromSearchResult(result("https://jooble.org/job/duplicate"));

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(jobApplicationService.countAllForCurrentUser()).isEqualTo(1);
    }

    @Test
    void userCannotReadAnotherUsersApplication() {
        User otherUser = new User();
        otherUser.setUsername("other@example.com");
        otherUser.setPassword("password");
        otherUser.setRole("ROLE_USER");
        userRepository.save(otherUser);

        JobApplication otherApplication = new JobApplication();
        otherApplication.setTitle("Private vacancy");
        otherApplication.setStatus(ApplicationStatus.SAVED);
        otherApplication.setUser(otherUser);
        jobApplicationRepository.save(otherApplication);

        assertThatThrownBy(() -> jobApplicationService.findOneForCurrentUser(otherApplication.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Заявку не знайдено");
    }

    @Test
    void statusUpdateChangesOnlyRequestedApplication() {
        JobApplicationDto first = jobApplicationService.saveFromSearchResult(result("https://jooble.org/job/status-one"));
        JobApplicationDto second = jobApplicationService.saveFromSearchResult(result("https://jooble.org/job/status-two"));

        jobApplicationService.updateStatusForCurrentUser(first.getId(), ApplicationStatus.INTERVIEW);

        assertThat(jobApplicationService.findOneForCurrentUser(first.getId()).getStatus()).isEqualTo(ApplicationStatus.INTERVIEW);
        assertThat(jobApplicationService.findOneForCurrentUser(second.getId()).getStatus()).isEqualTo(ApplicationStatus.SAVED);
    }

    @Test
    void noteCreationAndDeletionWorkThroughService() {
        JobApplicationDto saved = jobApplicationService.saveFromSearchResult(result("https://jooble.org/job/note"));
        JobApplicationDto withNote = jobApplicationService.addNoteForCurrentUser(saved.getId(), "Підготувати відповіді");

        assertThat(withNote.getNotes()).hasSize(1);

        Long noteId = withNote.getNotes().get(0).getId();
        jobApplicationService.deleteNote(saved.getId(), noteId);

        assertThat(jobApplicationService.findOneForCurrentUser(saved.getId()).getNotes()).isEmpty();
    }

    private JobSearchResultDto result(String sourceUrl) {
        return new JobSearchResultDto(
                "Java Developer",
                "Jooble Company",
                "Germany",
                "Spring Boot role",
                sourceUrl,
                "50000 EUR",
                "Full-time"
        );
    }
}
