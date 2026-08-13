package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingBaseDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBaseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {
    public static Item mapToItem(NewItemRequest request, User owner, ItemRequest itemRequest) {
        Item item = new Item();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setAvailable(request.getAvailable());
        item.setOwner(owner);
        item.setRequest(itemRequest);
        return item;
    }

    public static ItemDto mapToItemDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.isAvailable());
        dto.setComments(List.of());
        if (item.getRequest() != null) {
            dto.setRequestId(item.getRequest().getId());
        }
        return dto;
    }

    public static ItemDto mapToItemDto(Item item, List<CommentDto> comments) {
        ItemDto dto = mapToItemDto(item);
        dto.setComments(comments);
        return dto;
    }

    public static ItemDto mapToItemDto(Item item, List<CommentDto> comments,
                                       BookingBaseDto lastBooking, BookingBaseDto nextBooking) {
        ItemDto dto = mapToItemDto(item, comments);
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        return dto;
    }

    public static ItemBaseDto mapToItemBaseDto(Item item) {
        if (item == null) {
            return null;
        }
        ItemBaseDto dto = new ItemBaseDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        return dto;
    }
}
