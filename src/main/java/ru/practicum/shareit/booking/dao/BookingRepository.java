package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // booker
    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBookerIdOrderByStartDesc(Long userId);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long userId, LocalDateTime now1, LocalDateTime now2
    ); // CURRENT

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(
            Long userId, LocalDateTime now
    ); // PAST

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(
            Long userId, LocalDateTime now
    ); // FUTURE

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(
            Long userId, Status status
    ); // WAITING / REJECTED


    // owner
    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, LocalDateTime now1, LocalDateTime now2
    );

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(
            Long ownerId, LocalDateTime now
    );

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(
            Long ownerId, LocalDateTime now
    );

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(
            Long ownerId, Status status
    );


    List<Booking> findByBookerIdAndItemIdAndStatusAndEndBefore(
            Long bookerId, Long itemId, Status status, LocalDateTime now
    );

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(
            Long bookerId, Long itemId, Status status, LocalDateTime now
    );

    Optional<Booking> findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
            Long itemId, LocalDateTime now, Status status
    ); // lastBooking

    Optional<Booking> findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
            Long itemId, LocalDateTime now, Status status
    ); // nextBooking

    List<Booking> findByItemIdInAndStartBeforeAndStatusOrderByItemIdAscStartDesc(
            List<Long> itemIds, LocalDateTime now, Status status
    ); // для last — по каждому item_id первая строка после сортировки и есть последняя бронь

    List<Booking> findByItemIdInAndStartAfterAndStatusOrderByItemIdAscStartAsc(
            List<Long> itemIds, LocalDateTime now, Status status
    ); // для next — аналогично, первая строка на item_id — ближайшая будущая бронь
}