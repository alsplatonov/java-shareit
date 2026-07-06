package ru.practicum.shareit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.NewUserRequest;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private NewUserRequest user;
    private NewItemRequest item;

    @BeforeEach
    void setUp() {
        userRepository.clear();
        itemRepository.clear();

        user = new NewUserRequest();
        user.setEmail("owner@test.com");
        user.setLogin("owner");
        user.setName("Owner");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        item = new NewItemRequest();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
    }

    private Long createUser() throws Exception {
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        return node.get("id").asLong();
    }

    private Long createItem(Long userId) throws Exception {
        String response = mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        return node.get("id").asLong();
    }

    @Test
    void createItem_ReturnsCreatedItem() throws Exception {
        Long userId = createUser();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void getItemById_ReturnsItem() throws Exception {
        Long userId = createUser();
        Long itemId = createItem(userId);

        mockMvc.perform(get("/items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId));
    }

    @Test
    void updateItem_ReturnsUpdatedItem() throws Exception {
        Long userId = createUser();
        Long itemId = createItem(userId);

        UpdateItemRequest update = new UpdateItemRequest();
        update.setName("Updated Drill");

        mockMvc.perform(patch("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Drill"));
    }

    @Test
    void getItemsByOwner_ReturnsList() throws Exception {
        Long userId = createUser();
        createItem(userId);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void searchItems_ReturnsResults() throws Exception {
        Long userId = createUser();
        createItem(userId);

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void searchItems_EmptyText_ReturnsEmpty() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}