package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.entity.User;
import org.example.service.UserService;
import org.example.repository.RoleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    @GetMapping
    public String adminPage(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("currentPath", "/admin");
        return "admin";
    }

    @GetMapping("/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleRepository.findAll());
        return "new-user";
    }

    @PostMapping("/new")
    public String createUser(
            @Valid @ModelAttribute User user,
            BindingResult bindingResult,
            @RequestParam(value = "role", required = false) Set<String> roleNames,
            Model model
    ) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleRepository.findAll());
            return "new-user";
        }

        if (roleNames == null || roleNames.isEmpty()) {
            bindingResult.rejectValue("roles", "required", "At least one role must be selected");
            model.addAttribute("allRoles", roleRepository.findAll());
            return "new-user";
        }

        if (userService.existsByEmail(user.getEmail())) {
            bindingResult.rejectValue("email", "duplicate", "Email already exists");
            model.addAttribute("allRoles", roleRepository.findAll());
            return "new-user";
        }

        userService.create(user, roleNames);
        return "redirect:/admin";
    }

    @PostMapping("/edit")
    public String updateUser(
            @Valid @ModelAttribute User user,
            BindingResult bindingResult,
            @RequestParam(value = "role", required = false) Set<String> roleNames,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleRepository.findAll());
            return "edit-user";
        }

        if (roleNames == null || roleNames.isEmpty()) {
            bindingResult.rejectValue("roles", "required", "At least one role must be selected");
            model.addAttribute("allRoles", roleRepository.findAll());
            return "edit-user";
        }

        Optional<User> existingOpt = userService.findByEmail(user.getEmail());
        if (existingOpt.isPresent() && !existingOpt.get().getId().equals(user.getId())){
            bindingResult.rejectValue("email", "duplicate", "Email already in use");
            model.addAttribute("allRoles", roleRepository.findAll());
            return "edit-user";
        }

        userService.update(user.getId(), user, roleNames);
        return "redirect:/admin";
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleRepository.findAll());
        return "edit-user";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/admin";
    }
}