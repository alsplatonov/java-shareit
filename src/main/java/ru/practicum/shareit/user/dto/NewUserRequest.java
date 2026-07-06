package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class NewUserRequest {
    private Long id;

    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный email")
    private String email;

    private String login;

    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    public boolean hasName() {
        return name != null && !name.isEmpty();
    }

    public boolean hasLogin() {
        return login != null && !login.isBlank();
    }

    public boolean hasBirthday() {
        return birthday != null;
    }
}
