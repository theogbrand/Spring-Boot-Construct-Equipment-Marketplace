package com.leafcutters.antbuildz.controllers;

import com.leafcutters.antbuildz.models.RegistrationRequest;
// import com.leafcutters.antbuildz.models.User;
import com.leafcutters.antbuildz.models.Role;
// import com.leafcutters.antbuildz.repositories.UserRepository;
import com.leafcutters.antbuildz.services.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;

import javax.validation.Valid;

@Controller
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @PostMapping("/register/newUser")
    public String createUser(@Valid @ModelAttribute("request") RegistrationRequest request, BindingResult result,
            Model model) {

        model.addAttribute("request", request);

        if (result.hasErrors()) {
            return "registration/register";
        }

        Role newRole = Role.valueOf(request.getAppUserRole());
        registrationService.register(request, newRole);
        return "registration/registration_success";
    }

    @GetMapping(path = "/confirm")
    public String confirm(@RequestParam String email) {
        return registrationService.confirmAccount(email);
    }

    // Shows the Login or Register User page
    @GetMapping("/")
    public String firstView() {
        return "user/firstpage";
    }

    // Login page
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Redirects user to the page where they can register as a customer or partner
    @GetMapping("/register")
    public String registerView(RegistrationRequest request, Model model) {
        model.addAttribute("request", request);
        return "registration/register";
    }

    // Redirects user to their respective homepage based on their account role
    @GetMapping("/home")
    public String homepage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> role = auth.getAuthorities();
        // System.out.println(role.toArray()[0].toString());
        if (role.toArray()[0].toString().equals(String.valueOf(Role.CUSTOMER))) {
            return "redirect:/home/customer";
        } else if (role.toArray()[0].toString().equals(String.valueOf(Role.PARTNER))) {
            return "redirect:/home/partner";
        } else if (role.toArray()[0].toString().equals("ROLE_ADMIN")) {
            return "redirect:/home/admin";
        }
        return "redirect:/error";
    }

    // Default error page if something goes wrong
    @GetMapping("/error")
    public String error() {
        return "user/error";
    }

}
