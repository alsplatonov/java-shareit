package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.State;

import java.util.List;

public interface BookingService {

    BookingDto create(Long userId, NewBookingRequest request);

    BookingDto update(Long userId, Long bookingId, Boolean approved);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> findByCurrentUser(Long userId, State state);

    List<BookingDto> findByOwner(Long userId, State state);
}