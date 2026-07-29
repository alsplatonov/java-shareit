package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDto create(Long userId, NewBookingRequest request) {
        log.info("Создание бронирования пользователем id={} на вещь id={}", userId, request.getItemId());

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Создание бронирования невозможно: пользователь id={} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> {
                    log.warn("Создание бронирования невозможно: вещь id={} не найдена", request.getItemId());
                    return new NotFoundException("Вещь не найдена");
                });

        //валидация
        validateNotOwnItem(userId, item);
        validateItemAvailable(item);
        validateBookingDates(request);

        // создаём через маппер (status из request игнорируем)
        Booking booking = BookingMapper.mapToBooking(request, item, booker);

        // принудительно устанавливаем корректный статус
        booking.setStatus(Status.WAITING);

        Booking saved = bookingRepository.save(booking);

        log.info("Бронирование id={} успешно создано пользователем id={} на вещь id={}",
                saved.getId(), userId, item.getId());

        return BookingMapper.mapToBookingDto(saved);
    }

    // approve/reject
    @Override
    public BookingDto update(Long userId, Long bookingId, Boolean approved) {
        log.info("Изменение статуса бронирования id={} пользователем id={}, approved={}",
                bookingId, userId, approved);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Изменение статуса невозможно: бронирование id={} не найдено", bookingId);
                    return new NotFoundException("Бронирование не найдено");
                });

        // только владелец вещи может подтверждать
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("Доступ запрещён: пользователь id={} не владелец вещи бронирования id={}",
                    userId, bookingId);
            throw new ValidationException("Нет прав на изменение");
        }

        // нельзя менять уже подтверждённый/отклонённый
        if (!booking.getStatus().equals(Status.WAITING)) {
            log.warn("Изменение статуса невозможно: бронирование id={} уже имеет статус={}",
                    bookingId, booking.getStatus());
            throw new ConflictException("Статус уже установлен");
        }

        booking.setStatus(
                approved ? Status.APPROVED : Status.REJECTED
        );

        Booking saved = bookingRepository.save(booking);

        log.info("Статус бронирования id={} изменён на {}", saved.getId(), saved.getStatus());

        return BookingMapper.mapToBookingDto(saved);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        log.info("Поиск бронирования id={} пользователем id={}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Бронирование id={} не найдено", bookingId);
                    return new NotFoundException("Бронирование не найдено");
                });

        // только владелец или автор
        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("Доступ запрещён: пользователь id={} не автор и не владелец вещи бронирования id={}",
                    userId, bookingId);
            throw new NotFoundException("Нет доступа");
        }

        return BookingMapper.mapToBookingDto(booking);
    }

    // booker
    @Override
    public List<BookingDto> findByCurrentUser(Long userId, State state) {
        log.info("Получение бронирований пользователя id={} со статусом выборки={}", userId, state);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь id={} не найден при запросе его бронирований", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        List<BookingDto> bookings = getByState(userId, state).stream()
                .map(BookingMapper::mapToBookingDto)
                .toList();

        log.info("Найдено {} бронирований пользователя id={}", bookings.size(), userId);

        return bookings;
    }

    // owner
    @Override
    public List<BookingDto> findByOwner(Long userId, State state) {
        log.info("Получение бронирований вещей владельца id={} со статусом выборки={}", userId, state);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь id={} не найден при запросе бронирований его вещей", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        List<BookingDto> bookings = getByOwnerState(userId, state).stream()
                .map(BookingMapper::mapToBookingDto)
                .toList();

        log.info("Найдено {} бронирований вещей владельца id={}", bookings.size(), userId);

        return bookings;
    }


    private List<Booking> getByState(Long userId, State state) {
        LocalDateTime now = LocalDateTime.now();

        return switch (state) {
            case CURRENT -> bookingRepository
                    .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);

            case PAST -> bookingRepository
                    .findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);

            case FUTURE -> bookingRepository
                    .findByBookerIdAndStartAfterOrderByStartDesc(userId, now);

            case WAITING -> bookingRepository
                    .findByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING);

            case REJECTED -> bookingRepository
                    .findByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);

            default -> bookingRepository
                    .findByBookerIdOrderByStartDesc(userId);
        };
    }

    private List<Booking> getByOwnerState(Long userId, State state) {
        LocalDateTime now = LocalDateTime.now();

        return switch (state) {
            case CURRENT -> bookingRepository
                    .findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);

            case PAST -> bookingRepository
                    .findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);

            case FUTURE -> bookingRepository
                    .findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);

            case WAITING -> bookingRepository
                    .findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING);

            case REJECTED -> bookingRepository
                    .findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);

            default -> bookingRepository
                    .findByItemOwnerIdOrderByStartDesc(userId);
        };
    }

    private void validateNotOwnItem(Long userId, Item item) {
        if (item.getOwner().getId().equals(userId)) {
            log.warn("Пользователь id={} попытался забронировать свою же вещь id={}", userId, item.getId());
            throw new ConflictException("Нельзя бронировать свою вещь");
        }
    }

    private void validateItemAvailable(Item item) {
        if (!item.isAvailable()) {
            log.warn("Вещь id={} недоступна для бронирования", item.getId());
            throw new ValidationException("Вещь недоступна для бронирования");
        }
    }

    private void validateBookingDates(NewBookingRequest request) {
        if (!request.getEnd().isAfter(request.getStart())) {
            log.warn("Некорректные даты бронирования: start={}, end={}", request.getStart(), request.getEnd());
            throw new ConflictException("Дата окончания должна быть позже даты начала");
        }
    }
}