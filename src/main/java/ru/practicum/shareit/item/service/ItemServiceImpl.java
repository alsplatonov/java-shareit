package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

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

        Item item = ItemMapper.mapToItem(request, owner);
        Item savedItem = itemRepository.create(item);

        log.info("Вещь успешно создана id={} ownerId={}", savedItem.getId(), userId);

        return ItemMapper.mapToItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long userId, UpdateItemRequest request) {
        log.info("Обновление вещи id={} пользователем id={}", request.getId(), userId);

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

        Item updatedItem = itemRepository.update(item);

        log.info("Вещь id={} успешно обновлена", updatedItem.getId());

        return ItemMapper.mapToItemDto(updatedItem);
    }

    @Override
    public ItemDto findById(Long id) {
        log.info("Поиск вещи id={}", id);

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Вещь id={} не найдена", id);
                    return new NotFoundException("Вещь не найдена");
                });

        return ItemMapper.mapToItemDto(item);
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

        List<ItemDto> items = itemRepository.findByOwnerId(userId).stream()
                .map(ItemMapper::mapToItemDto)
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

        List<ItemDto> result = itemRepository.search(text).stream()
                .map(ItemMapper::mapToItemDto)
                .toList();

        log.info("Поиск завершён, найдено {} вещей по запросу='{}'", result.size(), text);

        return result;
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
}