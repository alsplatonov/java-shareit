package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingBaseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto create(Long userId, NewItemRequest request) {
        log.info("Создание вещи пользователем id={} name={}", userId, request.getName());

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Создание вещи невозможно: пользователь id={} не найден", userId);
                    return new NotFoundException(
                            "Пользователь с id " + userId + " не найден"
                    );
                });

        ItemRequest itemRequest = null;
        if (request.getRequestId() != null) {
            itemRequest = itemRequestRepository.findById(request.getRequestId())
                    .orElseThrow(() -> {
                        log.warn("Создание вещи невозможно: запрос id={} не найден", request.getRequestId());
                        return new NotFoundException("Запрос с id " + request.getRequestId() + " не найден");
                    });
        }

        Item item = ItemMapper.mapToItem(request, owner, itemRequest);
        Item savedItem = itemRepository.save(item);

        log.info("Вещь успешно создана id={} ownerId={}", savedItem.getId(), userId);

        return ItemMapper.mapToItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, UpdateItemRequest request) {
        log.info("Обновление вещи id={} пользователем id={}", itemId, userId);

        request.setId(itemId);
        Item item = itemRepository.findById(request.getId())
                .orElseThrow(() -> {
                    log.warn("Вещь id={} не найдена для обновления", request.getId());
                    return new NotFoundException("Вещь не найдена");
                });

        if (!item.getOwner().getId().equals(userId)) {
            log.warn("Доступ запрещён: пользователь id={} не владелец вещи id={}",
                    userId, item.getId());
            throw new NotFoundException("Редактировать может только владелец");
        }

        updateItemFields(item, request);

        Item updatedItem = itemRepository.save(item);

        log.info("Вещь id={} успешно обновлена", updatedItem.getId());

        return ItemMapper.mapToItemDto(updatedItem);
    }

    @Override
    public ItemDto findById(Long userId, Long itemId) {
        log.info("Поиск вещи id={} пользователем id={}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь id={} не найдена", itemId);
                    return new NotFoundException("Вещь не найдена");
                });

        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::mapToCommentDto)
                .toList();

        BookingBaseDto lastBooking = null;
        BookingBaseDto nextBooking = null;

        // бронирования видит только владелец вещи
        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            lastBooking = bookingRepository
                    .findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(itemId, now, Status.APPROVED)
                    .map(BookingMapper::mapToBookingBaseDto)
                    .orElse(null);

            nextBooking = bookingRepository
                    .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(itemId, now, Status.APPROVED)
                    .map(BookingMapper::mapToBookingBaseDto)
                    .orElse(null);
        }

        return ItemMapper.mapToItemDto(item, comments, lastBooking, nextBooking);
    }

    @Override
    public List<ItemDto> findAll() {
        log.info("Получение списка всех вещей");

        return itemRepository.findAll().stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> findByOwner(Long userId) {
        log.info("Получение вещей пользователя id={}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь id={} не найден при запросе вещей", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        List<Item> ownerItems = itemRepository.findByOwnerId(userId);

        List<Long> itemIds = ownerItems.stream()
                .map(Item::getId)
                .toList();

        if (itemIds.isEmpty()) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();

        // 1 запрос на все комментарии сразу
        Map<Long, List<CommentDto>> commentsByItemId = commentRepository
                .findByItemIdInOrderByItemIdAscCreatedDesc(itemIds).stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::mapToCommentDto, Collectors.toList())
                ));

        // 1 запрос на все "последние" бронирования сразу
        Map<Long, BookingBaseDto> lastBookingByItemId = toBookingMap(
                bookingRepository
                        .findByItemIdInAndStartBeforeAndStatusOrderByItemIdAscStartDesc(itemIds, now, Status.APPROVED)
                        .stream()
        );

        // 1 запрос на все "следующие" бронирования сразу
        Map<Long, BookingBaseDto> nextBookingByItemId = toBookingMap(
                bookingRepository
                        .findByItemIdInAndStartAfterAndStatusOrderByItemIdAscStartAsc(itemIds, now, Status.APPROVED)
                        .stream()
        );

        List<ItemDto> items = ownerItems.stream()
                .map(item -> ItemMapper.mapToItemDto(
                        item,
                        commentsByItemId.getOrDefault(item.getId(), List.of()),
                        lastBookingByItemId.get(item.getId()),
                        nextBookingByItemId.get(item.getId())
                ))
                .collect(Collectors.toList());

        log.info("Найдено {} вещей у пользователя id={}", items.size(), userId);

        return items;
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("Поиск вещей по тексту='{}'", text);

        if (text == null || text.isBlank()) {
            log.info("Пустой запрос поиска, возвращается пустой список");
            return List.of();
        }

        List<ItemDto> result = itemRepository
                .searchAvailableByText(text)
                .stream()
                .map(ItemMapper::mapToItemDto)
                .toList();

        log.info("Поиск завершён, найдено {} вещей по запросу='{}'", result.size(), text);

        return result;
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, NewCommentRequest request) {
        log.info("Добавление отзыва к вещи id={} пользователем id={}", itemId, userId);

        User author = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Добавление отзыва невозможно: пользователь id={} не найден", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Добавление отзыва невозможно: вещь id={} не найдена", itemId);
                    return new NotFoundException("Вещь не найдена");
                });

        boolean hasBooked = bookingRepository
                .existsByBookerIdAndItemIdAndStatusAndEndBefore(
                        userId, itemId, Status.APPROVED, LocalDateTime.now()
                );

        if (!hasBooked) {
            log.warn("Пользователь id={} не может оставить отзыв на вещь id={}: аренда не найдена",
                    userId, itemId);
            throw new ValidationException(
                    "Оставить отзыв может только пользователь, ранее арендовавший эту вещь"
            );
        }

        Comment comment = CommentMapper.mapToComment(request, item, author);
        Comment saved = commentRepository.save(comment);

        log.info("Отзыв id={} успешно добавлен к вещи id={}", saved.getId(), itemId);

        return CommentMapper.mapToCommentDto(saved);
    }

    public static Item updateItemFields(Item item, UpdateItemRequest request) {
        if (request.hasName()) {
            item.setName(request.getName());
        }

        if (request.hasDescription()) {
            item.setDescription(request.getDescription());
        }

        if (request.hasAvailable()) {
            item.setAvailable(request.getAvailable());
        }
        return item;
    }

    private Map<Long, BookingBaseDto> toBookingMap(Stream<Booking> stream) {
        return stream.collect(Collectors.toMap(
                booking -> booking.getItem().getId(),
                BookingMapper::mapToBookingBaseDto,
                (first, second) -> first
        ));
    }
}