package com.example.jobtracker.controller;

import com.example.jobtracker.service.JobApplicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
        return "dashboard";
    }
}
