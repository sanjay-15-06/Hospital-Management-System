package com.hospital.repo;

import com.hospital.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();
    Optional<User> findByUsername(String username);
    User save(User user);
}
