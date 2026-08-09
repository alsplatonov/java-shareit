package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewBookingRequest {
    @NotNull(message = "itemId обязателен")
    private Long itemId;

    @NotNull(message = "Дата начала обязательна")
    @Future(message = "Дата должна быть в будущем")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания обязательна")
    @Future(message = "Дата должна быть в будущем")
    private LocalDateTime end;
}