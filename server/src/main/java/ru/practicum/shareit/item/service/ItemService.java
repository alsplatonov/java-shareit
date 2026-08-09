package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    ItemDto create(Long userId, NewItemRequest request);

    ItemDto update(Long userId, Long itemId, UpdateItemRequest request);

    ItemDto findById(Long userId, Long itemId);

    List<ItemDto> findAll();

    List<ItemDto> findByOwner(Long userId);

    List<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, NewCommentRequest request);
}
