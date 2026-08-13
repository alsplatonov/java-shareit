package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.UserClient;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody NewUserRequest request) {
        log.info("Gateway: создание пользователя email={}", request.getEmail());
        return userClient.create(request);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        log.info("Gateway: обновление пользователя id={}", id);
        return userClient.update(id, request);
    }

    @GetMapping
    public ResponseEntity<Object> findAll() {
        log.info("Gateway: получение списка пользователей");
        return userClient.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable Long id) {
        log.info("Gateway: получение пользователя id={}", id);
        return userClient.findById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> remove(@PathVariable Long id) {
        log.info("Gateway: удаление пользователя id={}", id);
        return userClient.remove(id);
    }
}