package com.example.jobtracker.controller;

import com.example.jobtracker.dto.JobApplicationDto;
import com.example.jobtracker.dto.JobSearchResultDto;
import com.example.jobtracker.enums.ApplicationStatus;
import com.example.jobtracker.service.JobApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JobApplicationRestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobApplicationService jobApplicationService;

    @Test
    void unauthenticatedUserIsRedirectedFromApi() throws Exception {
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void authenticatedUserCanReadOwnApplications() throws Exception {
        jobApplicationService.saveFromSearchResult(result("https://jooble.org/job/api-list"));

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Java Developer"));
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void patchStatusReturnsUpdatedDto() throws Exception {
        JobApplicationDto saved = jobApplicationService.saveFromSearchResult(result("https://jooble.org/job/api-status"));

        mockMvc.perform(patch("/api/applications/{id}/status", saved.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "APPLIED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.statusLabel").value("Подано"));
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void postNoteReturnsUpdatedApplicationDto() throws Exception {
        JobApplicationDto saved = jobApplicationService.saveFromSearchResult(result("https://jooble.org/job/api-note"));

        mockMvc.perform(post("/api/applications/{id}/notes", saved.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("text", "Написати рекрутеру"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes", hasSize(1)))
                .andExpect(jsonPath("$.notes[0].text").value("Написати рекрутеру"));
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
