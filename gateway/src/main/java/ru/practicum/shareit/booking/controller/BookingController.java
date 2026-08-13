package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.RequestUtil;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.client.BookingClient;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @Valid @RequestBody NewBookingRequest request
    ) {
        log.info("Gateway: создание бронирования пользователем id={} на вещь id={}", userId, request.getItemId());
        return bookingClient.create(userId, request);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> update(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved
    ) {
        log.info("Gateway: изменение статуса бронирования id={} пользователем id={}", bookingId, userId);
        return bookingClient.update(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @PathVariable Long bookingId
    ) {
        log.info("Gateway: получение бронирования id={} пользователем id={}", bookingId, userId);
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByCurrentUser(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @RequestParam(required = false, defaultValue = "ALL") State state
    ) {
        log.info("Gateway: получение бронирований пользователя id={}, state={}", userId, state);
        return bookingClient.getBookingsByCurrentUser(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwner(
            @RequestHeader(RequestUtil.USER_HEADER_ID) Long userId,
            @RequestParam(required = false, defaultValue = "ALL") State state
    ) {
        log.info("Gateway: получение бронирований владельца id={}, state={}", userId, state);
        return bookingClient.getBookingsByOwner(userId, state);
    }
}