package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // booker
    List<Booking> findByBookerIdOrderByStartDesc(Long userId);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long userId, LocalDateTime now1, LocalDateTime now2
    ); // CURRENT

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(
            Long userId, LocalDateTime now
    ); // PAST

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(
            Long userId, LocalDateTime now
    ); // FUTURE

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(
            Long userId, Status status
    ); // WAITING / REJECTED


    // owner
    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, LocalDateTime now1, LocalDateTime now2
    );

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(
            Long ownerId, LocalDateTime now
    );

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(
            Long ownerId, LocalDateTime now
    );

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(
            Long ownerId, Status status
    );


    List<Booking> findByBookerIdAndItemIdAndStatusAndEndBefore(
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