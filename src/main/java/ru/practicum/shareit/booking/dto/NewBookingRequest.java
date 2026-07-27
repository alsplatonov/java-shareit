package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NewBookingRequest {
    @NotNull(message = "itemId обязателен")
    private Long itemId;

    @Future(message = "Дата должна быть в будущем")
    private LocalDateTime start;

    @Future(message = "Дата должна быть в будущем")
    private LocalDateTime end;
}
