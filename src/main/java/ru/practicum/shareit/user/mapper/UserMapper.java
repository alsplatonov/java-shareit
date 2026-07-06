package ru.practicum.shareit.user.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {
    public static User mapToUser(NewUserRequest request) {
        User user = new User();
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        user.setName(request.getName());
        if (request.hasLogin()) {
            user.setLogin(request.getLogin());
        }
        if (request.hasBirthday()) {
            user.setBirthday(request.getBirthday());
        }
        return user;
    }

    public static UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setBirthday(user.getBirthday());
        return dto;
    }
}
