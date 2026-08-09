package ru.practicum.shareit.user.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UserBaseDto;
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

        return user;
    }

    public static UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        return dto;
    }

    public static UserBaseDto mapToUserBaseDto(User user) {
        if (user == null) {
            return null;
        }
        UserBaseDto dto = new UserBaseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        return dto;
    }
}
