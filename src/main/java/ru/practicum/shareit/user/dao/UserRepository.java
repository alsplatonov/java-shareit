package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.model.User;
import java.util.Collection;
import java.util.Optional;

public interface UserRepository {

    User create(User user);

    User update(User user);

    Collection<User> findAll();

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> remove(Long id);

    void clear();
}
