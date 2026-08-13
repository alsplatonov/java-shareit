package ru.practicum.shareit.item.dto;

import lombok.Data;

@Data
public class ItemBaseDto {
    private Long id;
    private String name;
    private String description;
}