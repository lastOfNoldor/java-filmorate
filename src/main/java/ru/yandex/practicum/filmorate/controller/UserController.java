package ru.yandex.practicum.filmorate.controller;

import ch.qos.logback.classic.Logger;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserStorage users;
    private final UserService userService;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserStorage users, UserService userService) {
        this.users = users;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        logger.info("Запрос на получение данных о всех пользователях");
        return users.findAll();
    }

    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Long id) {
        logger.info("Запрос на получение данных о пользователе с Id: {}", id);
        return users.findById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        logger.info("Запрос на добавление в друзья пользователем с Id: {} пользователя c Id: {}", id, friendId);
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        logger.info("Запрос на удаление из друзей пользователем с Id: {} пользователя c Id: {}", id, friendId);
        return userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable Long id) {
        logger.info("Запрос на получение списка друзей пользователя с Id: {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriend(@PathVariable Long id, @PathVariable Long otherId) {
        logger.info("Запрос на получение списка обших друзей пользователя с Id: {} с пользователем с Id: {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody User user) {
        if (user.getId() != null) {
            logger.error("В запросе на создание присутствует ID пользователя");
            throw new ValidationException("Новый пользователь не должен иметь Id до регистрации");
        }
        logger.info("Запрос на создание нового пользователя: {}", user.getLogin());
        userDataValidate(user);
        return users.create(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            logger.error("В запросе отсутствует ID пользователя");
            throw new ValidationException("ID пользователя не указан");
        }
        logger.info("Запрос на обновление данных пользователя с ID: {}", newUser.getId());
        userDataValidate(newUser);
        return users.update(newUser);
    }


    private void userDataValidate(User user) {
        if (!user.getLogin().matches("^[a-zA-Z0-9_]+$")) {
            logger.error("Недопустимый логин пользователя: {}", user.getLogin());
            throw new ValidationException("Недопустимый логин пользователя.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            logger.info("Графа имени отсутствует, в качестве имени будет установлен логин для пользователя: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        if (user.getBirthday() != null) {
            if (user.getBirthday().isAfter(LocalDate.now())) {
                logger.error("Критическая ошибка в дате рождения пользователя {} . Дата рождения не может быть в будущем", user.getLogin());
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
        }
    }

    // метод для изоляции тестов, чтобы каждый тест в UserControllerTest не зависил от предыдущего
    @DeleteMapping("/reset")
    public void reset() {
        users.deleteAll();
    }


}

