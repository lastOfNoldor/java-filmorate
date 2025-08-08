package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.JdbcUserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@JdbcTest
@Import(JdbcUserRepository.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("JdbcUserRepository")
public class JdbcUserRepositoryTest {
    private static final long TEST_USER_ID = 1L;
    private static final long TEST_FRIEND_ID = 2L;
    private final JdbcUserRepository userRepository;


    static User getTestUser() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setEmail("email@email.com");
        user.setLogin("user");
        user.setName("user");
        user.setBirthday(LocalDate.of(2000, 3, 22));
        return user;
    }

    static User getTestFriend() {
        User friend = new User();
        friend.setId(TEST_FRIEND_ID);
        friend.setEmail("friend@email.com");
        friend.setLogin("friend");
        friend.setName("friend");
        friend.setBirthday(LocalDate.of(1995, 5, 15));
        return friend;
    }


    @Test
    @DisplayName("должен находить пользователя по ИД")
    void shouldReturnUserWhenFindById() {
        Optional<User> result = userRepository.findById(TEST_USER_ID);
        assertThat(result).isPresent().get().usingRecursiveComparison().isEqualTo(getTestUser());
    }


    @Test
    @DisplayName("должен возвращать пустой Optional при поиске несуществующего пользователя")
    void shouldReturnEmptyWhenUserNotFound() {
        Optional<User> result = userRepository.findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("должен создавать нового пользователя")
    void shouldCreateNewUser() {
        User newUser = new User();
        newUser.setEmail("new@email.com");
        newUser.setLogin("newuser");
        newUser.setName("New User");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userRepository.create(newUser);

        assertThat(createdUser.getId()).isNotNull();
        Optional<User> foundUser = userRepository.findById(createdUser.getId());
        assertThat(foundUser).isPresent().get().usingRecursiveComparison().ignoringFields("id").isEqualTo(newUser);
    }

    @Test
    @DisplayName("должен возвращать пустой список друзей")
    void shouldReturnEmptyFriendsList() {
        List<User> friends = userRepository.findFriends(TEST_USER_ID);
        assertThat(friends).isEmpty();
    }

}

