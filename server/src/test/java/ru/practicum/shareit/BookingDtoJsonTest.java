package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemBaseDto;
import ru.practicum.shareit.user.dto.UserBaseDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void serialize_WritesDates() throws Exception {
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStart(LocalDateTime.of(2026, 8, 12, 10, 0, 0));
        dto.setEnd(LocalDateTime.of(2026, 8, 15, 19, 0, 0));
        dto.setStatus(Status.APPROVED);

        ItemBaseDto item = new ItemBaseDto();
        item.setId(8L);
        item.setName("Дрель");
        dto.setItem(item);

        UserBaseDto booker = new UserBaseDto();
        booker.setId(2L);
        dto.setBooker(booker);

        var result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo("2026-08-12T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo("2026-08-15T19:00:00");
        assertThat(result).extractingJsonPathStringValue("$.status")
                .isEqualTo("APPROVED");
        assertThat(result).extractingJsonPathNumberValue("$.item.id")
                .isEqualTo(8);
        assertThat(result).extractingJsonPathNumberValue("$.booker.id")
                .isEqualTo(2);
    }

    @Test
    void deserialize_ParsesDatesCorrectly() throws Exception {
        String content = """
            {"id": 1, "start": "2026-08-12T10:00:00", "end": "2026-08-15T19:00:00", "status": "WAITING"}
            """;

        BookingDto result = json.parseObject(content);

        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2026, 8, 12, 10, 0, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2026, 8, 15, 19, 0, 0));
        assertThat(result.getStatus()).isEqualTo(Status.WAITING);
    }
}