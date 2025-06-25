package ru.yandex.practicum.filmorate.controller;

import ch.qos.logback.classic.Logger;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;


import java.time.Instant;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private static final Logger logger = (Logger) LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public Collection<User> findAll(){
        return users.values();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody User user) {
        try {
            if (user.getId() == null) {
                logger.info("Запрос на создание нового пользователя: {}", user.getLogin());
                credentialsAlreadyExists(user.getEmail(), user.getLogin());
                userDataValidate(user);
                user.setId(getNextId());
                users.put(user.getId(), user);
                logger.info("Успешное создание пользователя: {}. ID: {}", user.getLogin(), user.getId());
                return user;
            }
            throw new ValidationException("Новый пользователь не должен иметь Id до регистрации");
        } catch (ValidationException e) {
            logger.error("Ошибка при создании нового пользователя: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            logger.error("В запросе отсутствует ID пользователя");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID пользователя не указан");
        }
        logger.info("Запрос на обновление данных пользователя с ID: {}", newUser.getId());
        try {
            userDataValidate(newUser);
            if (users.containsKey(newUser.getId())) {
                User oldUser = users.get(newUser.getId());
                if (!Objects.equals(newUser.getEmail(), oldUser.getEmail())) {
                    credentialsAlreadyExists(newUser.getEmail(), newUser.getLogin());
                }
                oldUser.setLogin(newUser.getLogin());
                oldUser.setEmail(newUser.getEmail());
                Optional.ofNullable(newUser.getName()).ifPresent(oldUser::setName);
                Optional.ofNullable(newUser.getBirthday()).ifPresent(oldUser::setBirthday);
                logger.info("Данные пользователя успешно обновлены. ID: {}", newUser.getId());
                users.put(oldUser.getId(),oldUser);
                return oldUser;
            }
            logger.error("Не найден пользователь для обновления, ID: {}", newUser.getId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        } catch (ValidationException e) {
            logger.error("Ошибка обновления данных: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

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

    private void credentialsAlreadyExists(String email,String login) {
        for (User userFromList : users.values()) {
            if (Objects.equals(userFromList.getEmail(), email)) {
                logger.warn("Обнаружен уже используемый имейл: {}", email);
                throw new ValidationException("Этот имейл уже используется");
            }
            if (Objects.equals(userFromList.getLogin(), login)) {
                logger.warn("Обнаружен уже используемый логин: {}", login);
                throw new ValidationException("Этот логин уже используется");
            }

        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;

    }

    // метод для изоляции тестов, чтобы каждый тест в UserControllerTest не зависил от предыдущего
    @DeleteMapping("/reset")
    public void reset() {
        users.clear();
    }


}

