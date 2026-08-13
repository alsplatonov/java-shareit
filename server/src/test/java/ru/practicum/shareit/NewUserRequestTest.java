package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class NewUserRequestTest {

    @Test
    void hasNameReturnsTrueWhenNameIsNotEmpty() {
        NewUserRequest request = new NewUserRequest();
        request.setName("Ivan");

        assertThat(request.hasName()).isTrue();
    }

    @Test
    void hasNameReturnsFalseWhenNameIsNull() {
        NewUserRequest request = new NewUserRequest();

        assertThat(request.hasName()).isFalse();
    }

    @Test
    void hasNameReturnsFalseWhenNameIsEmpty() {
        NewUserRequest request = new NewUserRequest();
        request.setName("");

        assertThat(request.hasName()).isFalse();
    }

    @Test
    void hasLoginReturnsTrueWhenLoginIsNotBlank() {
        NewUserRequest request = new NewUserRequest();
        request.setLogin("ivan");

        assertThat(request.hasLogin()).isTrue();
    }

    @Test
    void hasLoginReturnsFalseWhenLoginIsNull() {
        NewUserRequest request = new NewUserRequest();

        assertThat(request.hasLogin()).isFalse();
    }

    @Test
    void hasLoginReturnsFalseWhenLoginIsBlank() {
        NewUserRequest request = new NewUserRequest();
        request.setLogin("   ");

        assertThat(request.hasLogin()).isFalse();
    }

    @Test
    void hasBirthdayReturnsTrueWhenBirthdayIsSet() {
        NewUserRequest request = new NewUserRequest();
        request.setBirthday(LocalDate.of(1990, 1, 1));

        assertThat(request.hasBirthday()).isTrue();
    }

    @Test
    void hasBirthdayReturnsFalseWhenBirthdayIsNull() {
        NewUserRequest request = new NewUserRequest();

        assertThat(request.hasBirthday()).isFalse();
    }
}