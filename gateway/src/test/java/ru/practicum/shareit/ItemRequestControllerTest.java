package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.ItemRequestClient;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Test
    void create_ValidRequest_DelegatesToClient() throws Exception {
        NewItemRequestDto request = new NewItemRequestDto();
        request.setDescription("Нужна дрель");

        when(itemRequestClient.create(eq(1L), any(NewItemRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("description", "Нужна дрель")));

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        ArgumentCaptor<NewItemRequestDto> captor =
                ArgumentCaptor.forClass(NewItemRequestDto.class);

        verify(itemRequestClient).create(eq(1L), captor.capture());

        assertThat(captor.getValue().getDescription())
                .isEqualTo("Нужна дрель");
    }

    @Test
    void create_BlankDescription_ReturnsBadRequest() throws Exception {
        NewItemRequestDto request = new NewItemRequestDto();
        request.setDescription("");

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void findById_ServerReturnsNotFound_GatewayPropagatesIt() throws Exception {
        when(itemRequestClient.findById(1L, 999L))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Запрос не найден")));

        mockMvc.perform(get("/requests/{requestId}", 999L).header(USER_HEADER, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void findOwn_ReturnsOk_DelegatesToClient() throws Exception {
        when(itemRequestClient.findOwn(1L))
                .thenReturn(ResponseEntity.ok(
                        Map.of("requests", List.of())
                ));

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemRequestClient).findOwn(1L);
    }

    @Test
    void findAll_ReturnsOk_DelegatesToClient() throws Exception {
        when(itemRequestClient.findAll(1L))
                .thenReturn(ResponseEntity.ok(
                        Map.of("requests", List.of())
                ));

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemRequestClient).findAll(1L);
    }

    @Test
    void findById_ReturnsOk_DelegatesToClient() throws Exception {
        when(itemRequestClient.findById(1L, 10L))
                .thenReturn(ResponseEntity.ok(
                        Map.of(
                                "id", 10L,
                                "description", "Нужна дрель"
                        )
                ));

        mockMvc.perform(get("/requests/{requestId}", 10L)
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemRequestClient).findById(1L, 10L);
    }

}