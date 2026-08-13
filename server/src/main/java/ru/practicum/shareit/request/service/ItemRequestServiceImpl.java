package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.dto.RequestedItemDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto create(Long userId, NewItemRequestDto request) {
        log.info("Создание запроса вещи пользователем id={}", userId);

        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Создание запроса невозможно: пользователь id={} не найден", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });

        ItemRequest itemRequest = ItemRequestMapper.mapToItemRequest(request, requestor);
        ItemRequest saved = itemRequestRepository.save(itemRequest);

        log.info("Запрос id={} успешно создан пользователем id={}", saved.getId(), userId);

        return ItemRequestMapper.mapToItemRequestDto(saved, List.of());
    }

    @Override
    public List<ItemRequestDto> findOwn(Long userId) {
        log.info("Получение своих запросов пользователем id={}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь id={} не найден при запросе своих запросов", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);

        List<ItemRequestDto> result = attachItems(requests);

        log.info("Найдено {} своих запросов пользователя id={}", result.size(), userId);

        return result;
    }

    @Override
    public List<ItemRequestDto> findAll(Long userId) {
        log.info("Получение чужих запросов, инициатор id={}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь id={} не найден при запросе чужих запросов", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId);

        List<ItemRequestDto> result = attachItems(requests);

        log.info("Найдено {} чужих запросов для пользователя id={}", result.size(), userId);

        return result;
    }

    @Override
    public ItemRequestDto findById(Long userId, Long requestId) {
        log.info("Поиск запроса id={} пользователем id={}", requestId, userId);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь id={} не найден при запросе данных о запросе id={}", userId, requestId);
                    return new NotFoundException("Пользователь не найден");
                });

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Запрос id={} не найден", requestId);
                    return new NotFoundException("Запрос не найден");
                });

        List<RequestedItemDto> items = itemRepository.findByRequestId(requestId).stream()
                .map(ItemRequestMapper::mapToRequestedItemDto)
                .toList();

        return ItemRequestMapper.mapToItemRequestDto(itemRequest, items);
    }

     //Подмешивает к списку запросов ответы (вещи) на них одним batch-запросом — без N+1.
    private List<ItemRequestDto> attachItems(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        if (requestIds.isEmpty()) {
            return List.of();
        }

        Map<Long, List<RequestedItemDto>> itemsByRequestId = itemRepository
                .findByRequestIdInOrderByRequestIdAsc(requestIds).stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(ItemRequestMapper::mapToRequestedItemDto, Collectors.toList())
                ));

        return requests.stream()
                .map(request -> ItemRequestMapper.mapToItemRequestDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), List.of())
                ))
                .toList();
    }
}