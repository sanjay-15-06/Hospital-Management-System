package com.hospital.repo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.model.User;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class FileUserRepository implements UserRepository {
    private final JsonFileStore<User> store;
    public FileUserRepository(ObjectMapper mapper) {
        store = new JsonFileStore<>("users.json", mapper, new TypeReference<List<User>>() {}, User::getId);
    }
    public List<User> findAll() { return store.findAll(); }
    public Optional<User> findByUsername(String username) {
        return findAll().stream().filter(user -> user.getUsername().equalsIgnoreCase(username)).findFirst();
    }
    public User save(User user) { return store.save(user); }
}
