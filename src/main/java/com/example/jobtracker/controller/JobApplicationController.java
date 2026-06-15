package com.example.jobtracker.controller;

import com.example.jobtracker.dto.JobApplicationDto;
import com.example.jobtracker.enums.ApplicationPriority;
import com.example.jobtracker.enums.ApplicationStatus;
import com.example.jobtracker.service.JobApplicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    public JobApplicationController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @GetMapping("/applications")
    public String applications(
            @RequestParam(defaultValue = "createdAt") String sortBy,
            Model model) {
        model.addAttribute("statuses", ApplicationStatus.values());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("applicationsByStatus", jobApplicationService.groupedByStatusForCurrentUser(sortBy));
        return "applications/list";
    }

    @GetMapping("/applications/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("jobApplication", jobApplicationService.findOneForCurrentUser(id));
        return "applications/details";
    }

    @GetMapping("/applications/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("jobApplication", jobApplicationService.findOneForCurrentUser(id));
        model.addAttribute("statuses", ApplicationStatus.values());
        model.addAttribute("priorities", ApplicationPriority.values());
        return "applications/edit";
    }

    @PostMapping("/applications/{id}/edit")
    public String update(
            @PathVariable Long id,
            @ModelAttribute("jobApplication") JobApplicationDto jobApplication,
            @RequestParam(required = false) String newNoteText,
            RedirectAttributes redirectAttributes) {
        jobApplicationService.updateApplication(id, jobApplication, newNoteText);
        redirectAttributes.addFlashAttribute("message", "Application updated.");
        return "redirect:/applications/" + id;
    }

    @PostMapping("/applications/{applicationId}/notes/{noteId}/delete")
    public String deleteNote(
            @PathVariable Long applicationId,
            @PathVariable Long noteId,
            RedirectAttributes redirectAttributes) {
        jobApplicationService.deleteNote(applicationId, noteId);
        redirectAttributes.addFlashAttribute("message", "Note deleted.");
        return "redirect:/applications/" + applicationId;
    }

    @PostMapping("/applications/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        jobApplicationService.deleteApplication(id);
        redirectAttributes.addFlashAttribute("message", "Application deleted.");
        return "redirect:/applications";
    }
}
