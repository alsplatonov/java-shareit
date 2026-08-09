package ru.practicum.shareit.request.dto;

import lombok.Data;

@Data
public class RequestedItemDto {
    private Long id;
    private String name;
    private Long ownerId;
}