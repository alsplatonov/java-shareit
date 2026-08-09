package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.RequestUtil;
import ru.practicum.shareit.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @Valid @RequestBody NewItemRequestDto request
    ) {
        log.info("Gateway: создание запроса вещи пользователем id={}", userId);
        return itemRequestClient.create(userId, request);
    }

    @GetMapping
    public ResponseEntity<Object> findOwn(@RequestHeader(RequestUtil.USER_HEADER_ID) Long userId) {
        log.info("Gateway: получение своих запросов пользователем id={}", userId);
        return itemRequestClient.findOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAll(@RequestHeader(RequestUtil.USER_HEADER_ID) Long userId) {
        log.info("Gateway: получение чужих запросов, инициатор id={}", userId);
        return itemRequestClient.findAll(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findById(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @PathVariable Long requestId
    ) {
        log.info("Gateway: получение запроса id={} пользователем id={}", requestId, userId);
        return itemRequestClient.findById(userId, requestId);
    }
}