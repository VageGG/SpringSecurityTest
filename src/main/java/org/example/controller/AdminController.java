package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.entity.Role;
import org.example.entity.User;
import org.example.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping
    public String adminPage(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("currentPath", "/admin");
        return "admin";
    }

    @GetMapping("/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        return "new-user";
    }

    @PostMapping("/new")
    public String createUser(@ModelAttribute User user,
                             @RequestParam(value = "selectedRoles", required = false) Set<String> selectedRoles) {

        // Устанавливаем роли из параметров
        if (selectedRoles != null && !selectedRoles.isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : selectedRoles) {
                Role role = new Role();
                role.setName(roleName);
                roles.add(role);
            }
            user.setRoles(roles);
        } else {
            // Роль по умолчанию - USER
            Role defaultRole = new Role();
            defaultRole.setName("ROLE_USER");
            user.setRoles(Set.of(defaultRole));
        }

        userService.save(user);
        return "redirect:/admin";
    }

    @PostMapping("/edit")
    public String updateUser(@ModelAttribute User user,
                             @RequestParam(value = "selectedRoles", required = false) Set<String> selectedRoles) {

        Long userId = user.getId();

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }


        // Устанавливаем роли из параметров
        if (selectedRoles != null && !selectedRoles.isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : selectedRoles) {
                Role role = new Role();
                role.setName(roleName);
                roles.add(role);
            }
            user.setRoles(roles);
        }

        userService.save(user);
        return "redirect:/admin";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/admin";
    }
}