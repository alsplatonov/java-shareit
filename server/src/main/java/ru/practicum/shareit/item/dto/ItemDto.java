package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingBaseDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Data
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private boolean available;
    private User owner;
    private Long requestId;
    private List<CommentDto> comments;
    private BookingBaseDto lastBooking;
    private BookingBaseDto nextBooking;
}
