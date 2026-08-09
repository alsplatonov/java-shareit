package ru.practicum.shareit.booking.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingBaseDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static ru.practicum.shareit.item.mapper.ItemMapper.mapToItemBaseDto;
import static ru.practicum.shareit.user.mapper.UserMapper.mapToUserBaseDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingMapper {

    public static Booking mapToBooking(NewBookingRequest request, Item item, User booker) {
        Booking booking = new Booking();
        booking.setStart(request.getStart());
        booking.setEnd(request.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        return booking;
    }

    public static BookingDto mapToBookingDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());
        dto.setItem(mapToItemBaseDto(booking.getItem()));
        dto.setBooker(mapToUserBaseDto(booking.getBooker()));
        return dto;
    }

    public static BookingBaseDto mapToBookingBaseDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingBaseDto dto = new BookingBaseDto();
        dto.setId(booking.getId());
        dto.setBookerId(booking.getBooker().getId());
        return dto;
    }
}