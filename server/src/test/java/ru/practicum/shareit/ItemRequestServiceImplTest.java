package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.dto.RequestedItemDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User requestor;
    private User otherUser;
    private User itemOwner;

    @BeforeEach
    void setUp() {
        requestor = userRepository.save(newUser("requestor@test.com", "Requestor"));
        otherUser = userRepository.save(newUser("otheruser@test.com", "Other"));
        itemOwner = userRepository.save(newUser("itemowner@test.com", "Owner"));
    }

    @Test
    void create_ValidRequest_ReturnsRequestWithEmptyItemsList() {
        ItemRequestDto result = itemRequestService.create(requestor.getId(), newDto("Требуется дрель"));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Требуется дрель");
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void create_UnknownUser_ThrowsNotFoundException() {
        assertThatThrownBy(() -> itemRequestService.create(999L, newDto("Требуется дрель")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findOwn_ReturnsOnlyOwnRequestsSortedByCreatedDesc() {
        ItemRequestDto first = itemRequestService.create(requestor.getId(), newDto("Первый запрос"));
        ItemRequestDto second = itemRequestService.create(requestor.getId(), newDto("Второй запрос"));
        itemRequestService.create(otherUser.getId(), newDto("Чужой запрос"));

        List<ItemRequestDto> result = itemRequestService.findOwn(requestor.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(second.getId()); // более новый первым
        assertThat(result.get(1).getId()).isEqualTo(first.getId());
    }

    @Test
    void findAll_ExcludesOwnRequests() {
        itemRequestService.create(requestor.getId(), newDto("Мой запрос"));
        ItemRequestDto othersRequest = itemRequestService.create(otherUser.getId(), newDto("Чужой запрос"));

        List<ItemRequestDto> result = itemRequestService.findAll(requestor.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(othersRequest.getId());
    }

    @Test
    void findById_ReturnsRequestWithAnsweringItems() {
        ItemRequestDto request = itemRequestService.create(requestor.getId(), newDto("Требуется дрель"));
        ItemRequest requestEntity = itemRequestRepository.findById(request.getId()).orElseThrow();

        Item answer = new Item();
        answer.setName("Дрель Bosch");
        answer.setDescription("Новая дрель");
        answer.setAvailable(true);
        answer.setOwner(itemOwner);
        answer.setRequest(requestEntity);
        itemRepository.save(answer);

        ItemRequestDto result = itemRequestService.findById(otherUser.getId(), request.getId());

        assertThat(result.getItems()).hasSize(1);
        RequestedItemDto answeringItem = result.getItems().get(0);
        assertThat(answeringItem.getId()).isEqualTo(answer.getId());
        assertThat(answeringItem.getName()).isEqualTo("Дрель Bosch");
        assertThat(answeringItem.getOwnerId()).isEqualTo(itemOwner.getId());
    }

    @Test
    void findById_RequestWithoutAnswers_ReturnsEmptyItemsList() {
        ItemRequestDto request = itemRequestService.create(requestor.getId(), newDto("Требуется дрель"));

        ItemRequestDto result = itemRequestService.findById(otherUser.getId(), request.getId());

        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void findById_UnknownUser_ThrowsNotFoundException() {
        ItemRequestDto request = itemRequestService.create(requestor.getId(), newDto("Требуется дрель"));

        assertThatThrownBy(() -> itemRequestService.findById(9999L, request.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    private User newUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    private NewItemRequestDto newDto(String description) {
        NewItemRequestDto dto = new NewItemRequestDto();
        dto.setDescription(description);
        return dto;
    }
}