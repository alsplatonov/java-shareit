package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceImpl userService;

    @Test
    void create_ValidRequest_ReturnsCreated() throws Exception {
        NewUserRequest request = new NewUserRequest();
        request.setEmail("test@test.com");
        request.setName("User");

        UserDto response = userDto(1L, "test@test.com", "User");
        when(userService.create(any())).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void create_InvalidEmail_ReturnsBadRequest() throws Exception {
        NewUserRequest request = new NewUserRequest();
        request.setEmail("test@");
        request.setName("User");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_EmptyBody_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_DuplicateEmail_ReturnsException() throws Exception {
        NewUserRequest request = new NewUserRequest();
        request.setEmail("test@test.com");
        request.setName("User");

        when(userService.create(any())).thenThrow(new ConflictException("Email уже существует"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void update_ValidRequest_ReturnsUpdatedUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");

        when(userService.update(any())).thenReturn(userDto(1L, "test@test.com", "Updated Name"));

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void findAll_ReturnsList() throws Exception {
        when(userService.findAll()).thenReturn(List.of(userDto(1L, "user@test.com", "User")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void findById_ReturnsExistingUser() throws Exception {
        when(userService.findById(1L)).thenReturn(userDto(1L, "user@test.com", "User"));

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void findById_UnknownUser_ReturnsNotFound() throws Exception {
        when(userService.findById(anyLong())).thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }

    @Test
    void remove_ExistingUser_ReturnsOk() throws Exception {
        when(userService.remove(1L)).thenReturn(Optional.of(userDto(1L, "user@test.com", "User")));

        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isOk());
    }

    private UserDto userDto(Long id, String email, String name) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setEmail(email);
        dto.setName(name);
        return dto;
    }
}