package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.client.BookingClient;

import java.time.LocalDateTime;
import java.util.Map;

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
    private BookingClient bookingClient;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Test
    void create_ValidRequest_DelegatesToClient() throws Exception {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingClient.create(eq(1L), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status", "WAITING")));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(bookingClient).create(eq(1L), any());
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
    void update_MissingApprovedParam_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/{bookingId}", 2L).header(USER_HEADER, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_ValidRequest_DelegatesToClient() throws Exception {
        when(bookingClient.update(1L, 2L, true))
                .thenReturn(ResponseEntity.ok(
                        Map.of("id", 2L, "status", "APPROVED")
                ));

        mockMvc.perform(patch("/bookings/{bookingId}", 2L)
                        .header(USER_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingClient).update(1L, 2L, true);
    }

    @Test
    void getBookingById_DelegatesToClient() throws Exception {
        when(bookingClient.getBookingById(1L, 2L)).thenReturn(ResponseEntity.ok(Map.of("id", 2)));

        mockMvc.perform(get("/bookings/{bookingId}", 2L).header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingClient).getBookingById(1L, 2L);
    }

    @Test
    void getBookingsByCurrentUser_ValidState_DelegatesToClient() throws Exception {
        when(bookingClient.getBookingsByCurrentUser(1L, State.ALL))
                .thenReturn(ResponseEntity.ok(Map.of()));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk());

        verify(bookingClient).getBookingsByCurrentUser(1L, State.ALL);
    }

    @Test
    void getBookingsByCurrentUser_InvalidState_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings").header(USER_HEADER, 1L).param("state", "NOT_A_REAL_STATE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingsByOwner_DelegatesToClient() throws Exception {
        when(bookingClient.getBookingsByOwner(1L, State.ALL)).thenReturn(ResponseEntity.ok(Map.of()));

        mockMvc.perform(get("/bookings/owner").header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingClient).getBookingsByOwner(1L, State.ALL);
    }
}