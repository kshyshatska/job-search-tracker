package com.example.jobtracker.service;

import com.example.jobtracker.dto.JobSearchRequestDto;
import com.example.jobtracker.dto.JobSearchResultDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JoobleApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiUrl;

    public JoobleApiService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${jooble.api.key:}") String apiKey,
            @Value("${jooble.api.url}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public List<JobSearchResultDto> searchJobs(JobSearchRequestDto request) {
        if (apiKey == null || apiKey.isBlank()) {
            return demoResults(request);
        }

        try {
            Map<String, String> body = new HashMap<>();
            body.put("keywords", buildKeywordQuery(request));
            body.put("location", safe(request.getLocation()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String response = restTemplate.postForObject(
                    apiUrl,
                    new HttpEntity<>(body, headers),
                    String.class,
                    apiKey
            );
            return parseJoobleResponse(response);
        } catch (RestClientException exception) {
            return List.of();
        }
    }

    private List<JobSearchResultDto> parseJoobleResponse(String response) {
        List<JobSearchResultDto> results = new ArrayList<>();
        if (response == null || response.isBlank()) {
            return results;
        }

        try {
            JsonNode jobs = objectMapper.readTree(response).path("jobs");
            if (jobs.isArray()) {
                for (JsonNode job : jobs) {
                    results.add(new JobSearchResultDto(
                            text(job, "title"),
                            text(job, "company"),
                            text(job, "location"),
                            text(job, "snippet"),
                            text(job, "link"),
                            text(job, "salary"),
                            text(job, "type")
                    ));
                }
            }
        } catch (Exception exception) {
            return List.of();
        }
        return results;
    }

    private List<JobSearchResultDto> demoResults(JobSearchRequestDto request) {
        String keyword = safe(request.getKeyword()).isBlank() ? "Java Developer" : request.getKeyword();
        String location = safe(request.getLocation()).isBlank() ? "Hamburg" : request.getLocation();

        return List.of(
                new JobSearchResultDto(
                        keyword + " Intern",
                        "Demo Software GmbH",
                        location,
                        "Build small Spring Boot features, write tests, and learn from a friendly engineering team.",
                        "https://example.com/jobs/java-intern",
                        "1200-1600 EUR",
                        defaultText(request.getJobType(), "Internship")
                ),
                new JobSearchResultDto(
                        "Junior " + keyword,
                        "Campus Tech Lab",
                        location,
                        "Work on backend services, REST controllers, and database features in a training-friendly role.",
                        "https://example.com/jobs/junior-java",
                        "42000-50000 EUR",
                        defaultText(request.getJobType(), "Full-time")
                )
        );
    }

    private String buildKeywordQuery(JobSearchRequestDto request) {
        StringBuilder query = new StringBuilder(safe(request.getKeyword()));
        appendFilter(query, request.getWorkMode());
        appendFilter(query, request.getLevel());
        appendFilter(query, request.getJobType());
        appendFilter(query, request.getDatePosted());
        return query.toString().trim();
    }

    private void appendFilter(StringBuilder query, String value) {
        if (!safe(value).isBlank()) {
            query.append(' ').append(value.trim());
        }
    }

    private String text(JsonNode node, String fieldName) {
        return node.path(fieldName).asText("");
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String defaultText(String value, String fallback) {
        return safe(value).isBlank() ? fallback : value.trim();
    }
}
