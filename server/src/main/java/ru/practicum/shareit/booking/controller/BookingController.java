package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.RequestUtil;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingServiceImpl bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto create(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @Valid @RequestBody NewBookingRequest request
    ) {
        return bookingService.create(userId, request);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto update(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved
    ) {
        return bookingService.update(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @PathVariable Long bookingId
    ) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getBookingsByCurrentUser(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @RequestParam(required = false, defaultValue = "ALL") State state
    ) {
        return bookingService.findByCurrentUser(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsByOwner(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @RequestParam(required = false, defaultValue = "ALL") State state
    ) {
        return bookingService.findByOwner(userId, state);
    }
}