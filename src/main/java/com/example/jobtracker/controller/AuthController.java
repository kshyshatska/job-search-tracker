package com.example.jobtracker.controller;

import com.example.jobtracker.dto.RegistrationDto;
import com.example.jobtracker.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registration", new RegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("registration") RegistrationDto registration, RedirectAttributes redirectAttributes) {
        if (!userService.register(registration)) {
            redirectAttributes.addFlashAttribute("error", "Користувач уже існує або форма заповнена не повністю.");
            return "redirect:/register";
        }
        redirectAttributes.addFlashAttribute("message", "Акаунт створено. Тепер можна увійти.");
        return "redirect:/login";
    }
}
