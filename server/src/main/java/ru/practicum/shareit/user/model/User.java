package ru.practicum.shareit.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный email")
    @Column(name = "email", nullable = false, unique = true)
    private String email;
}