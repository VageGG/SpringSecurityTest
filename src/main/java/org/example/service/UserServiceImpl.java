package org.example.service;

import jakarta.persistence.EntityNotFoundException;
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
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id=" + id + " not found"));
    }

    @Transactional
    @Override
    public User create(User user, Set<String> roleNames) {
        validateAndEncodePassword(user, null);
        setUserRoles(user, roleNames);
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public User update(Long id, User updatedUser, Set<String> roleNames) {
        User existing = findById(id);
        existing.setName(updatedUser.getName());
        existing.setAge(updatedUser.getAge());
        existing.setEmail(updatedUser.getEmail());
        validateAndEncodePassword(updatedUser, existing);
        setUserRoles(existing, roleNames);
        return userRepository.save(existing);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        findById(id);
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private void validateAndEncodePassword(User incoming, User existing) {
        String rawPassword = incoming.getPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            if (existing != null) {
                incoming.setPassword(existing.getPassword());
            } else {
                throw new IllegalArgumentException("Password is required for new user");
            }
        } else {
            incoming.setPassword(passwordEncoder.encode(rawPassword));
        }
    }

    private void setUserRoles(User user, Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            throw new IllegalArgumentException("At least one role must be selected");
        }

        Set<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new EntityNotFoundException("Role '" + name + "' not found")))
                .collect(Collectors.toSet());

        user.setRoles(roles);
    }
}