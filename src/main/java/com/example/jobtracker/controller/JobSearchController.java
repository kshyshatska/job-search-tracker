package com.example.jobtracker.controller;

import com.example.jobtracker.dto.JobSearchRequestDto;
import com.example.jobtracker.dto.JobSearchResultDto;
import com.example.jobtracker.service.JobApplicationService;
import com.example.jobtracker.service.JoobleApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class JobSearchController {

    private final JoobleApiService joobleApiService;
    private final JobApplicationService jobApplicationService;

    public JobSearchController(JoobleApiService joobleApiService, JobApplicationService jobApplicationService) {
        this.joobleApiService = joobleApiService;
        this.jobApplicationService = jobApplicationService;
    }

    @GetMapping("/jobs/search")
    public String searchPage(Model model) {
        if (!model.containsAttribute("searchRequest")) {
            model.addAttribute("searchRequest", new JobSearchRequestDto());
        }
        if (!model.containsAttribute("results")) {
            model.addAttribute("results", List.of());
        }
        model.addAttribute("savedSourceUrls", jobApplicationService.savedSourceUrlsForCurrentUser());
        return "search";
    }

    @PostMapping("/jobs/search")
    public String search(@ModelAttribute("searchRequest") JobSearchRequestDto searchRequest, Model model) {
        model.addAttribute("results", joobleApiService.searchJobs(searchRequest));
        model.addAttribute("savedSourceUrls", jobApplicationService.savedSourceUrlsForCurrentUser());
        return "search";
    }

    @PostMapping("/jobs/save")
    public String saveJob(@ModelAttribute JobSearchResultDto result, RedirectAttributes redirectAttributes) {
        if (jobApplicationService.isSavedSourceUrl(result.getSourceUrl())) {
            redirectAttributes.addFlashAttribute("message", "Ця вакансія вже є у трекері.");
            return "redirect:/applications";
        }
        jobApplicationService.saveFromSearchResult(result);
        redirectAttributes.addFlashAttribute("message", "Вакансію збережено у трекер.");
        return "redirect:/applications";
    }
}
