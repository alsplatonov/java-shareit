package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.RequestUtil;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @Valid @RequestBody NewItemRequestDto request
    ) {
        return itemRequestService.create(userId, request);
    }

    @GetMapping
    public List<ItemRequestDto> findOwn(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId
    ) {
        return itemRequestService.findOwn(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> findAll(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId
    ) {
        return itemRequestService.findAll(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto findById(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @PathVariable Long requestId
    ) {
        return itemRequestService.findById(userId, requestId);
    }
}