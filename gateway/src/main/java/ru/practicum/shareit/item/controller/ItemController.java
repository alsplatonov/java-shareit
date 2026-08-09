package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.RequestUtil;
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @Valid @RequestBody NewItemRequest request
    ) {
        log.info("Gateway: создание вещи пользователем id={}", userId);
        return itemClient.create(userId, request);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemRequest request
    ) {
        log.info("Gateway: обновление вещи id={} пользователем id={}", itemId, userId);
        return itemClient.update(userId, itemId, request);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findById(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @PathVariable Long itemId
    ) {
        log.info("Gateway: получение вещи id={} пользователем id={}", itemId, userId);
        return itemClient.findById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> findByOwner(@RequestHeader(RequestUtil.USER_HEADER_ID) Long userId) {
        log.info("Gateway: получение вещей владельца id={}", userId);
        return itemClient.findByOwner(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text) {
        log.info("Gateway: поиск вещей по тексту='{}'", text);
        return itemClient.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody NewCommentRequest request
    ) {
        log.info("Gateway: добавление отзыва к вещи id={} пользователем id={}", itemId, userId);
        return itemClient.addComment(userId, itemId, request);
    }
}