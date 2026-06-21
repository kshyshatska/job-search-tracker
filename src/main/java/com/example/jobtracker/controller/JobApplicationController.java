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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    public JobApplicationController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @GetMapping("/applications")
    public String applications(
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean showArchived,
            Model model) {
        model.addAttribute("visibleStatuses", visibleStatuses(showArchived));
        model.addAttribute("allStatuses", ApplicationStatus.values());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("showArchived", showArchived);
        model.addAttribute("applicationsByStatus", jobApplicationService.groupedByStatusForCurrentUser(sortBy));
        model.addAttribute("statusLabels", statusLabels());
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
        model.addAttribute("statusLabels", statusLabels());
        model.addAttribute("priorityLabels", priorityLabels());
        return "applications/edit";
    }

    @PostMapping("/applications/{id}/edit")
    public String update(
            @PathVariable Long id,
            @ModelAttribute("jobApplication") JobApplicationDto jobApplication,
            @RequestParam(required = false) String newNoteText,
            RedirectAttributes redirectAttributes) {
        jobApplicationService.updateApplication(id, jobApplication, newNoteText);
        redirectAttributes.addFlashAttribute("message", "Заявку оновлено.");
        return "redirect:/applications/" + id;
    }

    @PostMapping("/applications/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status,
            RedirectAttributes redirectAttributes) {
        jobApplicationService.updateStatusForCurrentUser(id, status);
        redirectAttributes.addFlashAttribute("message", "Статус оновлено.");
        return "redirect:/applications";
    }

    @PostMapping("/applications/{id}/notes")
    public String addNote(
            @PathVariable Long id,
            @RequestParam String text,
            RedirectAttributes redirectAttributes) {
        jobApplicationService.addNoteForCurrentUser(id, text);
        redirectAttributes.addFlashAttribute("message", "Нотатку додано.");
        return "redirect:/applications/" + id;
    }

    @PostMapping("/applications/{applicationId}/notes/{noteId}/delete")
    public String deleteNote(
            @PathVariable Long applicationId,
            @PathVariable Long noteId,
            RedirectAttributes redirectAttributes) {
        jobApplicationService.deleteNote(applicationId, noteId);
        redirectAttributes.addFlashAttribute("message", "Нотатку видалено.");
        return "redirect:/applications/" + applicationId;
    }

    @PostMapping("/applications/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        jobApplicationService.deleteApplication(id);
        redirectAttributes.addFlashAttribute("message", "Заявку видалено.");
        return "redirect:/applications";
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

    private Map<ApplicationPriority, String> priorityLabels() {
        return Map.of(
                ApplicationPriority.LOW, "Низький",
                ApplicationPriority.MEDIUM, "Середній",
                ApplicationPriority.HIGH, "Високий"
        );
    }

    private List<ApplicationStatus> visibleStatuses(boolean showArchived) {
        return Arrays.stream(ApplicationStatus.values())
                .filter(status -> showArchived || status != ApplicationStatus.REJECTED)
                .toList();
    }
}
