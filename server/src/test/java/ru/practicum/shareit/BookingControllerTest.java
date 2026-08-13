package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.ConflictException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingServiceImpl bookingService;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Test
    void create_ValidRequest_ReturnsCreated() throws Exception {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingService.create(eq(1L), any())).thenReturn(bookingDto(1L, Status.WAITING));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void create_MissingItemId_ReturnsBadRequest() throws Exception {
        NewBookingRequest request = new NewBookingRequest();
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_StartInPast_ReturnsBadRequest() throws Exception {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().minusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_OwnItem_ReturnsConflict() throws Exception {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingService.create(anyLong(), any())).thenThrow(new ConflictException("Нельзя бронировать свою вещь"));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void update_ApproveBooking_ReturnsUpdated() throws Exception {
        when(bookingService.update(1L, 2L, true)).thenReturn(bookingDto(2L, Status.APPROVED));

        mockMvc.perform(patch("/bookings/{bookingId}", 2L)
                        .header(USER_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingById_ReturnsBookingFromService() throws Exception {
        when(bookingService.getBookingById(1L, 2L)).thenReturn(bookingDto(2L, Status.WAITING));

        mockMvc.perform(get("/bookings/{bookingId}", 2L).header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    void getBookingsByOwner_ReturnsListFromService() throws Exception {
        when(bookingService.findByOwner(1L, State.ALL)).thenReturn(List.of(bookingDto(1L, Status.WAITING)));

        mockMvc.perform(get("/bookings/owner").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getBookingsByCurrentUser_ReturnsListFromService() throws Exception {
        when(bookingService.findByCurrentUser(1L, State.ALL))
                .thenReturn(List.of(bookingDto(1L, Status.WAITING)));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("WAITING"));

        verify(bookingService).findByCurrentUser(1L, State.ALL);
    }

    private BookingDto bookingDto(Long id, Status status) {
        BookingDto dto = new BookingDto();
        dto.setId(id);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        dto.setStatus(status);
        return dto;
    }
}