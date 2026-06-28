package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class UserRepository implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private Long idCounter = 1L;

    @Override
    public UserDto create(NewUserRequest request) {
        User user = UserMapper.mapToUser(request);
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public UserDto update(UpdateUserRequest request) {
        User user = users.get(request.getId());
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        UserMapper.updateUserFields(user, request);
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public Collection<UserDto> findAll() {
        return users.values().stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    @Override
    public UserDto findById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public Optional<UserDto> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        return users.values().stream()
                .filter(user -> email.equals(user.getEmail()))
                .findFirst()
                .map(UserMapper::mapToUserDto);
    }

    @Override
    public Optional<UserDto> remove(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        users.remove(user.getId());
        return Optional.of(UserMapper.mapToUserDto(user));
    }
}
