package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(newUser("owner@test.com", "Owner"));
        booker = userRepository.save(newUser("booker@test.com", "Booker"));

        item = new Item();
        item.setName("Дрель");
        item.setDescription("Описание");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);
    }

    @Test
    void create_ValidRequest_ReturnsWaitingBooking() {
        NewBookingRequest request = newBookingRequest(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        BookingDto result = bookingService.create(booker.getId(), request);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Status.WAITING);
        assertThat(result.getItem().getId()).isEqualTo(item.getId());
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void create_OwnItem_ThrowsConflictException() {
        NewBookingRequest request = newBookingRequest(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(owner.getId(), request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void create_UnavailableItem_ThrowsValidationException() {
        item.setAvailable(false);
        itemRepository.save(item);

        NewBookingRequest request = newBookingRequest(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_EndBeforeStart_ThrowsConflictException() {
        NewBookingRequest request = newBookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1)
        );

        assertThatThrownBy(() -> bookingService.create(booker.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Дата окончания должна быть позже даты начала");
    }

    @Test
    void create_EndEqualsStart_ThrowsConflictException() {
        LocalDateTime date = LocalDateTime.now().plusDays(1);

        NewBookingRequest request = newBookingRequest(
                item.getId(),
                date,
                date
        );

        assertThatThrownBy(() -> bookingService.create(booker.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Дата окончания должна быть позже даты начала");
    }

    @Test
    void update_ApproveByOwner_SetsApprovedStatus() {
        BookingDto created = bookingService.create(booker.getId(), newBookingRequest(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        BookingDto result = bookingService.update(owner.getId(), created.getId(), true);

        assertThat(result.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    void update_RejectByOwner_SetsRejectedStatus() {
        BookingDto created = bookingService.create(booker.getId(), newBookingRequest(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        BookingDto result = bookingService.update(owner.getId(), created.getId(), false);

        assertThat(result.getStatus()).isEqualTo(Status.REJECTED);
    }

    @Test
    void update_ByNonOwner_ThrowsValidationException() {
        BookingDto created = bookingService.create(booker.getId(), newBookingRequest(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        assertThatThrownBy(() -> bookingService.update(booker.getId(), created.getId(), true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void update_AlreadyApproved_ThrowsConflictException() {
        BookingDto created = bookingService.create(booker.getId(), newBookingRequest(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));
        bookingService.update(owner.getId(), created.getId(), true);

        assertThatThrownBy(() -> bookingService.update(owner.getId(), created.getId(), false))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void getBookingById_ByBookerOrOwner_ReturnsBooking() {
        BookingDto created = bookingService.create(booker.getId(), newBookingRequest(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        assertThat(bookingService.getBookingById(booker.getId(), created.getId()).getId())
                .isEqualTo(created.getId());
        assertThat(bookingService.getBookingById(owner.getId(), created.getId()).getId())
                .isEqualTo(created.getId());
    }

    @Test
    void findByCurrentUser_FutureState_ReturnsOnlyFutureBookings() {
        bookingService.create(booker.getId(), newBookingRequest(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        List<BookingDto> result = bookingService.findByCurrentUser(booker.getId(), State.FUTURE);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByCurrentUser_WaitingState_ReturnsOnlyWaitingBookings() {
        BookingDto created = bookingService.create(booker.getId(), newBookingRequest(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));
        bookingService.update(owner.getId(), created.getId(), true); // теперь APPROVED

        List<BookingDto> result = bookingService.findByCurrentUser(booker.getId(), State.WAITING);

        assertThat(result).isEmpty();
    }

    @Test
    void findByOwner_AllState_ReturnsAllBookingsForOwnerItems() {
        bookingService.create(booker.getId(), newBookingRequest(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        List<BookingDto> result = bookingService.findByOwner(owner.getId(), State.ALL);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByOwner_CurrentState_ReturnsCurrentBookings() {
        bookingService.create(
                booker.getId(),
                newBookingRequest(
                        item.getId(),
                        LocalDateTime.now().minusHours(1),
                        LocalDateTime.now().plusHours(1)
                )
        );

        List<BookingDto> result = bookingService.findByOwner(
                owner.getId(), State.CURRENT
        );

        assertThat(result).hasSize(1);
    }

    @Test
    void findByOwner_PastState_ReturnsPastBookings() {
        bookingService.create(
                booker.getId(),
                newBookingRequest(
                        item.getId(),
                        LocalDateTime.now().minusDays(2),
                        LocalDateTime.now().minusDays(1)
                )
        );

        List<BookingDto> result = bookingService.findByOwner(
                owner.getId(), State.PAST
        );

        assertThat(result).hasSize(1);
    }

    @Test
    void findByOwner_FutureState_ReturnsFutureBookings() {
        bookingService.create(
                booker.getId(),
                newBookingRequest(
                        item.getId(),
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2)
                )
        );

        List<BookingDto> result = bookingService.findByOwner(
                owner.getId(), State.FUTURE
        );

        assertThat(result).hasSize(1);
    }

    @Test
    void findByOwner_WaitingState_ReturnsWaitingBookings() {
        bookingService.create(
                booker.getId(),
                newBookingRequest(
                        item.getId(),
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2)
                )
        );

        List<BookingDto> result = bookingService.findByOwner(
                owner.getId(), State.WAITING
        );

        assertThat(result).hasSize(1);
    }

    @Test
    void findByOwner_RejectedState_ReturnsRejectedBookings() {
        BookingDto created = bookingService.create(
                booker.getId(),
                newBookingRequest(
                        item.getId(),
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2)
                )
        );

        bookingService.update(owner.getId(), created.getId(), false);

        List<BookingDto> result = bookingService.findByOwner(
                owner.getId(), State.REJECTED
        );

        assertThat(result).hasSize(1);
    }

    //getByState()
    @Test
    void findByCurrentUser_CurrentState_ReturnsCurrentBookings() {
        bookingService.create(booker.getId(), newBookingRequest(
                item.getId(),
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1)
        ));

        List<BookingDto> result = bookingService.findByCurrentUser(
                booker.getId(), State.CURRENT
        );

        assertThat(result).hasSize(1);
    }

    @Test
    void findByCurrentUser_PastState_ReturnsPastBookings() {
        bookingService.create(booker.getId(), newBookingRequest(
                item.getId(),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
        ));

        List<BookingDto> result = bookingService.findByCurrentUser(
                booker.getId(), State.PAST
        );

        assertThat(result).hasSize(1);
    }

    @Test
    void findByCurrentUser_RejectedState_ReturnsRejectedBookings() {
        BookingDto created = bookingService.create(
                booker.getId(),
                newBookingRequest(
                        item.getId(),
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2)
                )
        );

        bookingService.update(owner.getId(), created.getId(), false);

        List<BookingDto> result = bookingService.findByCurrentUser(
                booker.getId(), State.REJECTED
        );

        assertThat(result).hasSize(1);
    }

    @Test
    void findByCurrentUser_AllState_ReturnsAllBookings() {
        bookingService.create(
                booker.getId(),
                newBookingRequest(
                        item.getId(),
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2)
                )
        );

        List<BookingDto> result = bookingService.findByCurrentUser(
                booker.getId(), State.ALL
        );

        assertThat(result).hasSize(1);
    }

    private User newUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    private NewBookingRequest newBookingRequest(Long itemId, LocalDateTime start, LocalDateTime end) {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(itemId);
        request.setStart(start);
        request.setEnd(end);
        return request;
    }
}