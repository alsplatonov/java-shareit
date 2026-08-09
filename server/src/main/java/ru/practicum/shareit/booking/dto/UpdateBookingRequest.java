package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.practicum.shareit.booking.model.Status;

@Data
public class UpdateBookingRequest {
    @Positive(message = "id должен быть положительным")
    private Long id;

    private Status status;
}

