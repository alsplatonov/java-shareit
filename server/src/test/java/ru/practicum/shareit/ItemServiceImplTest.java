package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private User otherPerson;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(newUser("owner@test.com", "Owner"));
        booker = userRepository.save(newUser("booker@test.com", "Booker"));
        otherPerson = userRepository.save(newUser("other@test.com", "Other"));

        item = new Item();
        item.setName("Дрель");
        item.setDescription("Новая дрель");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);
    }

    @Test
    void create_ValidRequest_ReturnsCreatedItem() {
        NewItemRequest request = newItemRequest("Дрель", "Новая", true, null);

        ItemDto result = itemService.create(owner.getId(), request);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getRequestId()).isNull();
    }

    @Test
    void create_WithRequestId_LinksItemToRequest() {
        ItemRequestDto itemRequest = itemRequestService.create(owner.getId(), newItemRequestDto("Нужна дрель"));

        NewItemRequest request = newItemRequest("Дрель", "Аккумуляторная", true, itemRequest.getId());

        ItemDto result = itemService.create(owner.getId(), request);

        assertThat(result.getRequestId()).isEqualTo(itemRequest.getId());
    }

    @Test
    void create_UnknownRequestId_ThrowsNotFoundException() {
        NewItemRequest request = newItemRequest("Дрель", "Аккумуляторная", true, 9999L);

        assertThatThrownBy(() -> itemService.create(owner.getId(), request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_UnknownOwner_ThrowsNotFoundException() {
        NewItemRequest request = newItemRequest("Дрель", "Аккумуляторная", true, null);

        assertThatThrownBy(() -> itemService.create(9999L, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_ByOwner_UpdatesFields() {
        ItemDto created = itemService.create(owner.getId(),
                newItemRequest("Дрель", "Старое описание", true, null));

        UpdateItemRequest update = new UpdateItemRequest();
        update.setDescription("Новое описание");

        ItemDto result = itemService.update(owner.getId(), created.getId(), update);

        assertThat(result.getDescription()).isEqualTo("Новое описание");
        assertThat(result.getName()).isEqualTo("Дрель");
    }

    @Test
    void search_MatchesTextInNameOrDescription_ReturnsOnlyAvailable() {
        itemService.create(owner.getId(),
                newItemRequest("Дрель Bosch", "Новая", true, null));
        itemService.create(owner.getId(),
                newItemRequest("Молоток", "Дрель-молоток", true, null));
        itemService.create(owner.getId(),
                newItemRequest("Дрель старая", "Сломана", false, null)); // недоступна

        List<ItemDto> result = itemService.search("Дрель");

        assertThat(result).hasSize(3);  //+ 1 объект из setUp
        assertThat(result).allMatch(ItemDto::isAvailable);
    }

    @Test
    void search_BlankText_ReturnsEmptyList() {
        itemService.create(owner.getId(), newItemRequest("Дрель", "Описание", true, null));

        List<ItemDto> result = itemService.search("   ");

        assertThat(result).isEmpty();
    }

    // findById: комментарии и lastBooking/nextBooking
    @Test
    void findById_ByOwner_ExposesLastAndNextBooking() {
        Booking past = approvedBooking(item, booker,
                LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(8));
        Booking future = approvedBooking(item, booker,
                LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(5));

        ItemDto result = itemService.findById(owner.getId(), item.getId());

        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getLastBooking().getId()).isEqualTo(past.getId());
        assertThat(result.getNextBooking()).isNotNull();
        assertThat(result.getNextBooking().getId()).isEqualTo(future.getId());
    }

    @Test
    void findById_UnknownItem_ThrowsNotFoundException() {
        assertThatThrownBy(() -> itemService.findById(owner.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findById_WithComments_ReturnsThemSortedByCreatedDesc() {
        addApprovedPastBookingAndComment(item, booker, "Отличная дрель", LocalDateTime.now().minusDays(1));
        addApprovedPastBookingAndComment(item, booker, "Великолепная вещь!", LocalDateTime.now());

        ItemDto result = itemService.findById(otherPerson.getId(), item.getId());

        assertThat(result.getComments()).hasSize(2);
        assertThat(result.getComments().get(0).getText()).isEqualTo("Великолепная вещь!");
    }

    // addComment
    @Test
    void addComment_UserHasPastApprovedBooking_Success() {
        approvedBooking(item, booker, LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(1));

        NewCommentRequest request = new NewCommentRequest();
        request.setText("Всё отлично!");

        CommentDto result = itemService.addComment(booker.getId(), item.getId(), request);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getText()).isEqualTo("Всё отлично!");
    }

    @Test
    void addComment_BookingNotFinishedYet_ThrowsValidationException() {
        // бронирование ещё не завершилось (end в будущем)
        approvedBooking(item, booker, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        NewCommentRequest request = new NewCommentRequest();
        request.setText("Аренда ещё идёт");

        assertThatThrownBy(() -> itemService.addComment(booker.getId(), item.getId(), request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void addComment_UnknownItem_ThrowsNotFoundException() {
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Текст отзыва");

        assertThatThrownBy(() -> itemService.addComment(booker.getId(), 999L, request))
                .isInstanceOf(NotFoundException.class);
    }

    //FindByOwner
    @Test
    void findByOwner_MultipleItemsWithMixedData_ReturnsAllCorrectlyAssembled() {
        Item secondItem = new Item();
        secondItem.setName("Дрель");
        secondItem.setDescription("Новая дрель");
        secondItem.setAvailable(true);
        secondItem.setOwner(owner);
        secondItem = itemRepository.save(secondItem);

        approvedBooking(item, booker, LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(3));
        addApprovedPastBookingAndComment(item, booker, "Комментарий к первой вещи",
                LocalDateTime.now().minusDays(2));

        // у второй вещи нет ни бронирований, ни отзывов
        List<ItemDto> result = itemService.findByOwner(owner.getId());

        assertThat(result).hasSize(2);

        ItemDto firstItemDto = result.stream()
                .filter(dto -> dto.getId().equals(item.getId()))
                .findFirst().orElseThrow();
        assertThat(firstItemDto.getLastBooking()).isNotNull();
        assertThat(firstItemDto.getComments()).hasSize(1);

        Item finalSecondItem = secondItem;
        ItemDto secondItemDto = result.stream()
                .filter(dto -> dto.getId().equals(finalSecondItem.getId()))
                .findFirst().orElseThrow();
        assertThat(secondItemDto.getLastBooking()).isNull();
        assertThat(secondItemDto.getComments()).isEmpty();
    }

    @Test
    void findByOwner_NoItems_ReturnsEmptyList() {
        List<ItemDto> result = itemService.findByOwner(otherPerson.getId());

        assertThat(result).isEmpty();
    }

    private User newUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    private Booking approvedBooking(Item item, User booker, LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(Status.APPROVED);
        return bookingRepository.save(booking);
    }

    private NewItemRequest newItemRequest(String name, String description, boolean available, Long requestId) {
        NewItemRequest request = new NewItemRequest();
        request.setName(name);
        request.setDescription(description);
        request.setAvailable(available);
        request.setRequestId(requestId);
        return request;
    }

    private NewItemRequestDto newItemRequestDto(String description) {
        NewItemRequestDto dto = new NewItemRequestDto();
        dto.setDescription(description);
        return dto;
    }

    private void addApprovedPastBookingAndComment(Item item, User booker, String text, LocalDateTime created) {
        approvedBooking(item, booker, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(9));

        NewCommentRequest request = new NewCommentRequest();
        request.setText(text);
        itemService.addComment(booker.getId(), item.getId(), request);
    }
}