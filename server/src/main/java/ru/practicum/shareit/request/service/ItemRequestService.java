package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, NewItemRequestDto request);

    List<ItemRequestDto> findOwn(Long userId);

    List<ItemRequestDto> findAll(Long userId);

    ItemRequestDto findById(Long userId, Long requestId);
}