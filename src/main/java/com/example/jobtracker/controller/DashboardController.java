package com.example.jobtracker.controller;

import com.example.jobtracker.service.JobApplicationService;
import com.example.jobtracker.enums.ApplicationStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class DashboardController {

    private final JobApplicationService jobApplicationService;

    public DashboardController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalApplications", jobApplicationService.countAllForCurrentUser());
        model.addAttribute("statusCounts", jobApplicationService.countByStatusForCurrentUser());
        model.addAttribute("recentApplications", jobApplicationService.recentApplications());
        model.addAttribute("actionNeeded", jobApplicationService.countActionNeededForCurrentUser());
        model.addAttribute("statusLabels", statusLabels());
        return "dashboard";
    }

    private Map<ApplicationStatus, String> statusLabels() {
        return Map.of(
                ApplicationStatus.SAVED, "Збережено",
                ApplicationStatus.APPLIED, "Подано",
                ApplicationStatus.INTERVIEW, "Співбесіда",
                ApplicationStatus.OFFER, "Офер",
                ApplicationStatus.REJECTED, "Відмова"
        );
    }
}
