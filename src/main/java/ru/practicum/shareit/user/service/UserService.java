package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import java.util.Collection;
import java.util.Optional;

public interface UserService {
    UserDto create(NewUserRequest user);

    UserDto update(UpdateUserRequest user);

    Collection<UserDto> findAll();

    UserDto findById(Long id);

    Optional<UserDto> findByEmail(String email);

    Optional<UserDto> remove(Long id);
}