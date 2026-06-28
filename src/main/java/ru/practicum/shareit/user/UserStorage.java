package ru.practicum.shareit.user;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    UserDto create(NewUserRequest user);

    UserDto update(UpdateUserRequest user);

    Collection<UserDto> findAll();

    UserDto findById(Long id);

    Optional<UserDto> findByEmail(String email);

    Optional<UserDto> remove(Long id);

}
