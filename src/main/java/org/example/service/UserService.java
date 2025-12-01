package org.example.service;

import org.example.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserService {

    List<User> findAll();

    User findById(Long id);

    User create(User user, Set<String> roleNames);

    User update(Long id, User user, Set<String> roleNames);

    void deleteById(Long id);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}