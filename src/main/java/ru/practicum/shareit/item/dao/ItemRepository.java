package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;
import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {

    Item create(Item item);

    Optional<Item> findById(Long id);

    Collection<Item> findAll();

    Collection<Item> findByOwnerId(Long ownerId);

    Item update(Item item);

    Optional<Item> remove(Long id);

    void clear();
}