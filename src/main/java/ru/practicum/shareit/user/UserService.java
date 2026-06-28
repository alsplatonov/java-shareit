package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public UserDto create(NewUserRequest user) {

        userStorage.findByEmail(user.getEmail())
                .ifPresent(u -> {
                    throw new ConflictException(
                            "Пользователь с email = " + user.getEmail() + " уже существует!"
                    );
                });

        return userStorage.create(user);
    }

    public UserDto update(UpdateUserRequest user) {
        UserDto existingUser = userStorage.findById(user.getId());

        if (user.getEmail() != null && user.hasEmail()) {
            userStorage.findByEmail(user.getEmail())
                    .filter(u -> !u.getId().equals(user.getId()))
                    .ifPresent(u -> {
                        throw new ConflictException("Email already exists");
                    });
        }

    return userStorage.update(user);
}

    public Collection<UserDto> findAll() {
        return userStorage.findAll();
    }

    public UserDto findById(Long id) {
        return userStorage.findById(id);
    }

    public Optional<UserDto> findByEmail(String email) {
        return userStorage.findByEmail(email);
    }

    public Optional<UserDto> remove(Long id) {
        return userStorage.remove(id);
    }

}
