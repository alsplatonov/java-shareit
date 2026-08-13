package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.dto.RequestedItemDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
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
    private ItemRequestService itemRequestService;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Test
    void create_ValidRequest_ReturnsCreatedRequest() throws Exception {
        NewItemRequestDto request = new NewItemRequestDto();
        request.setDescription("Нужна дрель");

        ItemRequestDto response = itemRequestDto(1L, "Нужна дрель", List.of());
        when(itemRequestService.create(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Нужна дрель"))
                .andExpect(jsonPath("$.items").isArray());
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
    }

    @Test
    void findOwn_ReturnsListFromService() throws Exception {
        when(itemRequestService.findOwn(1L))
                .thenReturn(List.of(itemRequestDto(1L, "Запрос 1", List.of())));

        mockMvc.perform(get("/requests").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(itemRequestService).findOwn(1L);
    }

    @Test
    void findAll_ReturnsListFromService() throws Exception {
        when(itemRequestService.findAll(1L))
                .thenReturn(List.of(itemRequestDto(2L, "Чужой запрос", List.of())));

        mockMvc.perform(get("/requests/all").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L));

        verify(itemRequestService).findAll(1L);
    }

    @Test
    void findById_ReturnsRequestWithAnsweringItems() throws Exception {
        RequestedItemDto answer = new RequestedItemDto();
        answer.setId(10L);
        answer.setName("Дрель Bosch");
        answer.setOwnerId(5L);

        when(itemRequestService.findById(1L, 2L))
                .thenReturn(itemRequestDto(2L, "Нужна дрель", List.of(answer)));

        mockMvc.perform(get("/requests/{requestId}", 2L).header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(10L))
                .andExpect(jsonPath("$.items[0].name").value("Дрель Bosch"))
                .andExpect(jsonPath("$.items[0].ownerId").value(5L));
    }

    @Test
    void findById_UnknownRequest_ReturnsNotFound() throws Exception {
        when(itemRequestService.findById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Запрос не найден"));

        mockMvc.perform(get("/requests/{requestId}", 999L).header(USER_HEADER, 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Запрос не найден"));
    }

    private ItemRequestDto itemRequestDto(Long id, String description, List<RequestedItemDto> items) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(id);
        dto.setDescription(description);
        dto.setCreated(LocalDateTime.now());
        dto.setItems(items);
        return dto;
    }
}