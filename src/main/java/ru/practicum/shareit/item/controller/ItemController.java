package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.RequestUtil;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    //Создание вещи
    @PostMapping
    public ItemDto create(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @Valid @RequestBody NewItemRequest request
    ) {
        return itemService.create(userId, request);
    }

    //Редактирование вещи (только ее владельцем)
    @PatchMapping("/{itemId}")
    public ItemDto update(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemRequest request
    ) {
        return itemService.update(userId, itemId, request);
    }

    //Получить вещь по id
    @GetMapping("/{itemId}")
    public ItemDto findById(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @PathVariable Long itemId
    ) {
        return itemService.findById(userId, itemId);
    }

    //Получить все вещи владельца
    @GetMapping
    public List<ItemDto> findByOwner(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId
    ) {
        return itemService.findByOwner(userId);
    }

    //Поиск
    @GetMapping("/search")
    public List<ItemDto> search(
            @RequestParam String text
    ) {
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody NewCommentRequest request
    ) {
        return itemService.addComment(userId, itemId, request);
    }
}