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

import java.time.Instant;
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
    private final String arbeitnowApiUrl;
    private static final int ARBEITNOW_PAGES_TO_SCAN = 3;
    private static final int MAX_RESULTS = 20;

    public JoobleApiService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${jooble.api.key:}") String apiKey,
            @Value("${jooble.api.url}") String apiUrl,
            @Value("${arbeitnow.api.url}") String arbeitnowApiUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.arbeitnowApiUrl = arbeitnowApiUrl;
    }

    public List<JobSearchResultDto> searchJobs(JobSearchRequestDto request) {
        if (apiKey == null || apiKey.isBlank()) {
            return searchArbeitnow(request);
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
            List<JobSearchResultDto> results = parseJoobleResponse(response);
            return results.isEmpty() ? searchArbeitnow(request) : results;
        } catch (RestClientException exception) {
            return searchArbeitnow(request);
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

    private List<JobSearchResultDto> searchArbeitnow(JobSearchRequestDto request) {
        List<JobSearchResultDto> results = new ArrayList<>();
        try {
            for (int page = 1; page <= ARBEITNOW_PAGES_TO_SCAN && results.size() < MAX_RESULTS; page++) {
                String response = restTemplate.getForObject(withPage(arbeitnowApiUrl, page), String.class);
                results.addAll(parseArbeitnowResponse(response, request, MAX_RESULTS - results.size()));
            }
            return results;
        } catch (RestClientException exception) {
            return List.of();
        }
    }

    private List<JobSearchResultDto> parseArbeitnowResponse(String response, JobSearchRequestDto request, int remainingLimit) {
        List<JobSearchResultDto> results = new ArrayList<>();
        if (response == null || response.isBlank()) {
            return results;
        }

        try {
            JsonNode jobs = objectMapper.readTree(response).path("data");
            if (jobs.isArray()) {
                for (JsonNode job : jobs) {
                    String title = text(job, "title");
                    String company = text(job, "company_name");
                    String location = text(job, "location");
                    String description = truncate(stripHtml(text(job, "description")), 280);
                    String sourceUrl = text(job, "url");
                    boolean remote = job.path("remote").asBoolean(false);
                    String jobType = joinNonBlank(joinArray(job.path("job_types")), remote ? "Remote" : "");
                    if (sourceUrl.isBlank()) {
                        continue;
                    }

                    JobSearchResultDto dto = new JobSearchResultDto(
                            title,
                            company,
                            location,
                            description,
                            sourceUrl,
                            "",
                            jobType
                    );

                    if (matchesArbeitnowSearch(dto, request, job)) {
                        results.add(dto);
                    }
                    if (results.size() >= remainingLimit) {
                        break;
                    }
                }
            }
        } catch (Exception exception) {
            return List.of();
        }
        return results;
    }

    private String withPage(String url, int page) {
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + "page=" + page;
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
        String keyword = safe(request.getKeyword()).toLowerCase();
        String location = safe(request.getLocation()).toLowerCase();
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

        return containsAllWords(searchable, keyword)
                && containsAllWords(searchable, location)
                && containsAllWords(searchable, workMode)
                && containsAllWords(searchable, level)
                && containsAllWords(searchable, type);
    }

    private boolean matchesArbeitnowSearch(JobSearchResultDto job, JobSearchRequestDto request, JsonNode rawJob) {
        String keyword = safe(request.getKeyword()).toLowerCase();
        String location = safe(request.getLocation()).toLowerCase();
        String workMode = safe(request.getWorkMode()).toLowerCase();
        String level = safe(request.getLevel()).toLowerCase();
        String type = safe(request.getJobType()).toLowerCase();
        String remoteText = rawJob.path("remote").asBoolean(false) ? "remote віддалено" : "onsite office офіс";

        String searchable = String.join(" ",
                safe(job.getTitle()),
                safe(job.getCompany()),
                safe(job.getLocation()),
                safe(job.getDescription()),
                safe(job.getJobType()),
                joinArray(rawJob.path("tags")),
                remoteText
        ).toLowerCase();

        return containsAllWords(searchable, keyword)
                && containsAllWords(searchable, location)
                && containsAllWords(searchable, workMode)
                && containsAllWords(searchable, level)
                && containsAllWords(searchable, type)
                && matchesDatePosted(rawJob.path("created_at").asLong(0), request.getDatePosted());
    }

    private boolean matchesDatePosted(long createdAtEpochSeconds, String datePosted) {
        String value = safe(datePosted);
        if (value.isBlank() || createdAtEpochSeconds <= 0) {
            return true;
        }

        long now = Instant.now().getEpochSecond();
        long ageSeconds = now - createdAtEpochSeconds;
        return switch (value) {
            case "last 24 hours" -> ageSeconds <= 24 * 60 * 60;
            case "last 7 days" -> ageSeconds <= 7 * 24 * 60 * 60;
            case "last 30 days" -> ageSeconds <= 30 * 24 * 60 * 60;
            default -> true;
        };
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

    private String joinArray(JsonNode node) {
        if (!node.isArray()) {
            return "";
        }
        List<String> values = new ArrayList<>();
        for (JsonNode item : node) {
            String value = item.asText("");
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        return String.join(", ", values);
    }

    private String joinNonBlank(String first, String second) {
        List<String> values = new ArrayList<>();
        if (!safe(first).isBlank()) {
            values.add(first.trim());
        }
        if (!safe(second).isBlank()) {
            values.add(second.trim());
        }
        return String.join(", ", values);
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
