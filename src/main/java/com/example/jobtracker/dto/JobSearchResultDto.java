package com.example.jobtracker.dto;

public class JobSearchResultDto {

    private String title;
    private String company;
    private String location;
    private String description;
    private String sourceUrl;
    private String salary;
    private String jobType;

    public JobSearchResultDto() {
    }

    public JobSearchResultDto(String title, String company, String location, String description, String sourceUrl) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.sourceUrl = sourceUrl;
    }

    public JobSearchResultDto(String title, String company, String location, String description, String sourceUrl, String salary, String jobType) {
        this(title, company, location, description, sourceUrl);
        this.salary = salary;
        this.jobType = jobType;
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
}
