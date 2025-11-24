package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.Role;
import org.example.entity.User;
import org.example.repository.RoleRepository;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        // Шифруем пароль для новых пользователей
        if (user.getId() == null && user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Для обновления - если пароль пустой, сохраняем старый
        if (user.getId() != null && (user.getPassword() == null || user.getPassword().isEmpty())) {
            User existingUser = userRepository.findById(user.getId()).orElse(null);
            if (existingUser != null) {
                user.setPassword(existingUser.getPassword());
            }
        } else if (user.getId() != null && user.getPassword() != null && !user.getPassword().isEmpty()) {
            // Если пароль изменился, шифруем новый
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Сохраняем роли (находим существующие или создаем новые)
        if (user.getRoles() != null) {
            Set<Role> managedRoles = user.getRoles().stream()
                    .map(role -> roleRepository.findByName(role.getName())
                            .orElseGet(() -> {
                                // Если роли нет в базе, создаем новую
                                Role newRole = new Role();
                                newRole.setName(role.getName());
                                return roleRepository.save(newRole);
                            }))
                    .collect(Collectors.toSet());
            user.setRoles(managedRoles);
        }

        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}