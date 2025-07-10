package ru.yandex.practicum.filmorate.controller;

import ch.qos.logback.classic.Logger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(UserController.class);


    @GetMapping
    public List<User> findAll() {
        logger.info("Запрос на получение данных о всех пользователях");
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        logger.info("Запрос на получение данных о пользователе с Id: {}", id);
        return userService.findById(id);
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
    public List<User> getFriends(@PathVariable Long id) {
        logger.info("Запрос на получение списка друзей пользователя с Id: {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriend(@PathVariable Long id, @PathVariable Long otherId) {
        logger.info("Запрос на получение списка общих друзей пользователя с Id: {} с пользователем с Id: {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody User user) {
        logger.info("Запрос на создание нового пользователя: {}", user.getLogin());
        userDataValidate(user);
        return userService.create(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        logger.info("Запрос на обновление данных пользователя с ID: {}", newUser.getId());
        userDataValidate(newUser);
        return userService.update(newUser);
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
        userService.deleteAll();
    }


}

