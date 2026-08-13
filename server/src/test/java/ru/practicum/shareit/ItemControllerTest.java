package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Test
    void create_ValidRequest_ReturnsCreated() throws Exception {
        NewItemRequest request = new NewItemRequest();
        request.setName("Дрель");
        request.setDescription("Аккумуляторная");
        request.setAvailable(true);

        when(itemService.create(eq(1L), any())).thenReturn(itemDto(1L, "Дрель"));

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void create_BlankName_ReturnsBadRequest() throws Exception {
        NewItemRequest request = new NewItemRequest();
        request.setName("");
        request.setDescription("Описание");
        request.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_ValidRequest_ReturnsUpdatedItem() throws Exception {
        UpdateItemRequest request = new UpdateItemRequest();
        request.setDescription("Новое описание");

        when(itemService.update(eq(1L), eq(2L), any())).thenReturn(itemDto(2L, "Дрель"));

        mockMvc.perform(patch("/items/{itemId}", 2L)
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    void findById_ReturnsItemFromService() throws Exception {
        when(itemService.findById(1L, 2L)).thenReturn(itemDto(2L, "Дрель"));

        mockMvc.perform(get("/items/{itemId}", 2L).header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    void findById_UnknownItem_ReturnsNotFound() throws Exception {
        when(itemService.findById(anyLong(), anyLong())).thenThrow(new NotFoundException("Вещь не найдена"));

        mockMvc.perform(get("/items/{itemId}", 999L).header(USER_HEADER, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByOwner_ReturnsListFromService() throws Exception {
        when(itemService.findByOwner(1L)).thenReturn(List.of(itemDto(1L, "Дрель")));

        mockMvc.perform(get("/items").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void search_ReturnsMatchingItems() throws Exception {
        when(itemService.search("дрель")).thenReturn(List.of(itemDto(1L, "Дрель")));

        mockMvc.perform(get("/items/search").param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void addComment_ValidRequest_ReturnsComment() throws Exception {
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Отличная вещь");

        CommentDto response = new CommentDto();
        response.setId(1L);
        response.setText("Отличная вещь");
        response.setAuthorName("Booker");
        response.setCreated(LocalDateTime.now());

        when(itemService.addComment(eq(1L), eq(2L), any())).thenReturn(response);

        mockMvc.perform(post("/items/{itemId}/comment", 2L)
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Отличная вещь"))
                .andExpect(jsonPath("$.authorName").value("Booker"));
    }

    @Test
    void addComment_UserNeverBooked_ReturnsBadRequest() throws Exception {
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Отзыв без аренды");

        when(itemService.addComment(anyLong(), anyLong(), any()))
                .thenThrow(new ValidationException("Оставить отзыв может только пользователь, ранее арендовавший эту вещь"));

        mockMvc.perform(post("/items/{itemId}/comment", 2L)
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private ItemDto itemDto(Long id, String name) {
        ItemDto dto = new ItemDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription("Описание");
        dto.setAvailable(true);
        dto.setComments(List.of());
        return dto;
    }
}