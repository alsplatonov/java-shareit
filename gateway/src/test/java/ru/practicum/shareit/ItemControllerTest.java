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
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    private ItemClient itemClient;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Test
    void create_ValidRequest_DelegatesToClient() throws Exception {
        NewItemRequest request = new NewItemRequest();
        request.setName("Дрель");
        request.setDescription("Аккумуляторная");
        request.setAvailable(true);

        when(itemClient.create(eq(1L), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", 1, "name", "Дрель")));

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Дрель"));

        verify(itemClient).create(eq(1L), any());
    }

    @Test
    void create_MissingUserHeader_ReturnsBadRequest() throws Exception {
        NewItemRequest request = new NewItemRequest();
        request.setName("Дрель");
        request.setDescription("Описание");
        request.setAvailable(true);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void create_BlankDescription_ReturnsBadRequest() throws Exception {
        NewItemRequest request = new NewItemRequest();
        request.setName("Дрель");
        request.setDescription("");
        request.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_DelegatesToClientWithBothIds() throws Exception {
        when(itemClient.findById(1L, 2L)).thenReturn(ResponseEntity.ok(Map.of("id", 2)));

        mockMvc.perform(get("/items/{itemId}", 2L).header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemClient).findById(1L, 2L);
    }

    @Test
    void findByOwner_DelegatesToClient() throws Exception {
        when(itemClient.findByOwner(1L)).thenReturn(ResponseEntity.ok(Map.of()));

        mockMvc.perform(get("/items").header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemClient).findByOwner(1L);
    }

    @Test
    void search_MissingTextParam_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_ValidRequest_DelegatesToClient() throws Exception {
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Отличная вещь");

        when(itemClient.addComment(eq(1L), eq(2L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("text", "Отличная вещь")));

        mockMvc.perform(post("/items/{itemId}/comment", 2L)
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_BlankText_ReturnsBadRequest() throws Exception {
        NewCommentRequest request = new NewCommentRequest();
        request.setText("   ");

        mockMvc.perform(post("/items/{itemId}/comment", 2L)
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }
}