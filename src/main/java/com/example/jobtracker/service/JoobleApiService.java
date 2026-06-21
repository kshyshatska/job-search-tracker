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
    private static final int MAX_RESULTS = 20;

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

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public List<JobSearchResultDto> searchJobs(JobSearchRequestDto request) {
        if (!isConfigured()) {
            return List.of();
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
            return parseJoobleResponse(response, request);
        } catch (RestClientException exception) {
            throw new IllegalStateException("Не вдалося отримати вакансії з Jooble.", exception);
        }
    }

    private List<JobSearchResultDto> parseJoobleResponse(String response, JobSearchRequestDto request) {
        List<JobSearchResultDto> results = new ArrayList<>();
        if (response == null || response.isBlank()) {
            return results;
        }

        try {
            JsonNode jobs = objectMapper.readTree(response).path("jobs");
            if (jobs.isArray()) {
                for (JsonNode job : jobs) {
                    JobSearchResultDto dto = new JobSearchResultDto(
                            text(job, "title"),
                            text(job, "company"),
                            text(job, "location"),
                            truncate(stripHtml(text(job, "snippet")), 320),
                            text(job, "link"),
                            text(job, "salary"),
                            text(job, "type")
                    );

                    if (!safe(dto.getSourceUrl()).isBlank() && matchesSearch(dto, request)) {
                        results.add(dto);
                    }
                    if (results.size() >= MAX_RESULTS) {
                        break;
                    }
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Не вдалося прочитати відповідь Jooble.", exception);
        }
        return results;
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

    private boolean matchesSearch(JobSearchResultDto job, JobSearchRequestDto request) {
        String workMode = safe(request.getWorkMode()).toLowerCase();
        String level = safe(request.getLevel()).toLowerCase();
        String type = safe(request.getJobType()).toLowerCase();

        String searchable = String.join(" ",
                safe(job.getTitle()),
                safe(job.getCompany()),
                safe(job.getLocation()),
                safe(job.getDescription()),
                safe(job.getJobType())
        ).toLowerCase();

        return containsAllWords(searchable, workMode)
                && containsAllWords(searchable, level)
                && containsAllWords(searchable, type);
    }


    private boolean containsAllWords(String source, String query) {
        if (query.isBlank()) {
            return true;
        }
        for (String word : query.split("\\s+")) {
            if (!source.contains(word)) {
                return false;
            }
        }
        return true;
    }

    private String stripHtml(String html) {
        return safe(html)
                .replaceAll("<[^>]*>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#039;", "'")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength).trim() + "...";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

}
