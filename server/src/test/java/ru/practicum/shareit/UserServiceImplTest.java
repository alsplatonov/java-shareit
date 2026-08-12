package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void create_ValidRequest_ReturnsCreatedUser() {
        NewUserRequest request = newUserRequest("user@test.com", "Name");

        UserDto result = userService.create(request);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getEmail()).isEqualTo("user@test.com");
        assertThat(result.getName()).isEqualTo("Name");
    }

    @Test
    void create_DuplicateEmail_ThrowsConflictException() {
        userService.create(newUserRequest("user@test.com", "User"));

        assertThatThrownBy(() -> userService.create(newUserRequest("user@test.com", "User2")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void update_ChangeNameOnly_KeepsEmailUnchanged() {
        UserDto created = userService.create(newUserRequest("user@test.com", "User"));

        UpdateUserRequest update = new UpdateUserRequest();
        update.setId(created.getId());
        update.setName("User Updated");

        UserDto result = userService.update(update);

        assertThat(result.getName()).isEqualTo("User Updated");
        assertThat(result.getEmail()).isEqualTo("user@test.com");
    }

    @Test
    void update_UnknownUser_ThrowsNotFoundException() {
        UpdateUserRequest update = new UpdateUserRequest();
        update.setId(9999L);
        update.setName("User");

        assertThatThrownBy(() -> userService.update(update))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findAll_ReturnsAllCreatedUsers() {
        userService.create(newUserRequest("u@test.com", "U1"));
        userService.create(newUserRequest("u2@test.com", "U2"));

        Collection<UserDto> result = userService.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void findById_ExistingUser_ReturnsUser() {
        UserDto created = userService.create(newUserRequest("user@test.com", "User"));

        UserDto result = userService.findById(created.getId());

        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getEmail()).isEqualTo("user@test.com");
    }

    @Test
    void findById_UnknownUser_ThrowsNotFoundException() {
        assertThatThrownBy(() -> userService.findById(9999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findByEmail_ExistingEmail_ReturnsUser() {
        userService.create(newUserRequest("user@test.com", "User"));

        Optional<UserDto> result = userService.findByEmail("user@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("User");
    }

    @Test
    void findByEmail_UnknownEmail_ReturnsEmpty() {
        Optional<UserDto> result = userService.findByEmail("test@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    void remove_UnknownUser_ReturnsEmptyOptional() {
        Optional<UserDto> result = userService.remove(9999L);

        assertThat(result).isEmpty();
    }

    private NewUserRequest newUserRequest(String email, String name) {
        NewUserRequest request = new NewUserRequest();
        request.setEmail(email);
        request.setName(name);
        return request;
    }
}