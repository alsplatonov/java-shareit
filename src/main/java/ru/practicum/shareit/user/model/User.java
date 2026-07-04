package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class User {
    private Long id;

    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный email")
    private String email;

    private String login;

    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}