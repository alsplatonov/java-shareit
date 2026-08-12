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
import ru.practicum.shareit.client.UserClient;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void create_ValidRequest_DelegatesToClientAndReturnsItsResponse() throws Exception {
        NewUserRequest request = new NewUserRequest();
        request.setEmail("test@test.com");
        request.setName("Test User");

        when(userClient.create(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", 1, "email", "test@test.com")));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@test.com"));

        verify(userClient).create(any());
    }

    @Test
    void create_BlankEmail_ReturnsBadRequest_WithoutCallingClient() throws Exception {
        NewUserRequest request = new NewUserRequest();
        request.setEmail("");
        request.setName("Test User");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void create_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        NewUserRequest request = new NewUserRequest();
        request.setEmail("test@");
        request.setName("Test User");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_ValidRequest_DelegatesToClientWithCorrectId() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");

        when(userClient.update(eq(1L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "name", "Updated Name")));

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void findAll_DelegatesToClient() throws Exception {
        when(userClient.findAll()).thenReturn(ResponseEntity.ok(Map.of()));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userClient).findAll();
    }

    @Test
    void findById_DelegatesToClientWithCorrectId() throws Exception {
        when(userClient.findById(1L)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk());

        verify(userClient).findById(1L);
    }

    @Test
    void findById_ServerReturnsNotFound_GatewayPropagatesIt() throws Exception {
        when(userClient.findById(999L))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Пользователь не найден")));

        mockMvc.perform(get("/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }

    @Test
    void remove_DelegatesToClientWithCorrectId() throws Exception {
        when(userClient.remove(1L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isOk());

        verify(userClient).remove(1L);
    }
}