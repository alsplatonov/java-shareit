package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerId(Long ownerId);

    List<Item> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name,
            String description
    );

    @Query("""
    SELECT i FROM Item i
    WHERE (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%'))
       OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))
      AND i.available = true
""")
    List<Item> searchAvailableByText(@Param("text") String text);
}