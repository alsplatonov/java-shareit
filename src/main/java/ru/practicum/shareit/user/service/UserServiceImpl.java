package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(NewUserRequest request) {
        log.info("Создание пользователя с email={}", request.getEmail());

        userRepository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    log.warn("Конфликт: пользователь с email={} уже существует", request.getEmail());
                    throw new ConflictException(
                            "Пользователь с email = " + request.getEmail() + " уже существует!"
                    );
                });

        User user = UserMapper.mapToUser(request);
        User savedUser = userRepository.create(user);

        log.info("Пользователь успешно создан с id={}", savedUser.getId());

        return UserMapper.mapToUserDto(savedUser);
    }

    @Override
    public UserDto update(UpdateUserRequest request) {
        log.info("Обновление пользователя id={}", request.getId());

        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> {
                    log.warn("Пользователь id={} не найден", request.getId());
                    return new NotFoundException(
                            "Пользователь с id=" + request.getId() + " не найден"
                    );
                });

        if (request.getEmail() != null && request.hasEmail()) {
            userRepository.findByEmail(request.getEmail())
                    .filter(u -> !u.getId().equals(request.getId()))
                    .ifPresent(u -> {
                        log.warn("Конфликт email={} при обновлении пользователя id={}",
                                request.getEmail(), request.getId());
                        throw new ConflictException("Email уже существует");
                    });
        }

        updateUserFields(user, request);

        User updatedUser = userRepository.update(user);

        log.info("Пользователь id={} успешно обновлён", updatedUser.getId());

        return UserMapper.mapToUserDto(updatedUser);
    }

    @Override
    public Collection<UserDto> findAll() {
        log.info("Запрос на получение всех пользователей");

        return userRepository.findAll().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto findById(Long id) {
        log.info("Поиск пользователя id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь id={} не найден", id);
                    return new NotFoundException(
                            "Пользователь с id=" + id + " не найден"
                    );
                });

        return UserMapper.mapToUserDto(user);
    }

    @Override
    public Optional<UserDto> findByEmail(String email) {
        log.info("Поиск пользователя по email={}", email);

        return userRepository.findByEmail(email)
                .map(UserMapper::mapToUserDto);
    }

    @Override
    public Optional<UserDto> remove(Long id) {
        log.info("Удаление пользователя id={}", id);

        Optional<UserDto> result = userRepository.remove(id)
                .map(UserMapper::mapToUserDto);

        if (result.isPresent()) {
            log.info("Пользователь id={} успешно удалён", id);
        } else {
            log.warn("Попытка удаления несуществующего пользователя id={}", id);
        }

        return result;
    }

    public static User updateUserFields(User user, UpdateUserRequest request) {
        if (request.hasName()) {
            user.setName(request.getName());
        }

        user.setEmail(request.getEmail());

        if (request.hasLogin()) {
            user.setLogin(request.getLogin());
        }

        if (request.hasBirthday()) {
            user.setBirthday(request.getBirthday());
        }

        return user;
    }
}