package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        credentialsAlreadyExists(user.getEmail(), user.getLogin());
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Успешное создание пользователя: {}. ID: {}", user.getLogin(), user.getId());
        return user;
    }

    @Override
    public User update(User newUser) {
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (!Objects.equals(newUser.getEmail(), oldUser.getEmail())) {
                credentialsAlreadyExists(newUser.getEmail(), newUser.getLogin());
            }
            oldUser.setLogin(newUser.getLogin());
            oldUser.setEmail(newUser.getEmail());
            Optional.ofNullable(newUser.getName()).ifPresent(oldUser::setName);
            Optional.ofNullable(newUser.getBirthday()).ifPresent(oldUser::setBirthday);
            log.info("Данные пользователя успешно обновлены. ID: {}", newUser.getId());
            users.put(oldUser.getId(), oldUser);
            return oldUser;
        }
        log.error("Не найден пользователь для обновления, ID: {}", newUser.getId());
        throw new NotFoundException("Пользователь не найден");
    }

    @Override
    public Optional<User> findById(Long id) {
        log.info("Выполнение запроса поиска пользователя с id: {} в хранилище", id);
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void deleteAll() {
        users.clear();
    }

    private long getNextId() {
        long currentMaxId = users.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;

    }

    private void credentialsAlreadyExists(String email, String login) {
        for (User userFromList : users.values()) {
            if (Objects.equals(userFromList.getEmail(), email)) {
                log.warn("Обнаружен уже используемый имейл: {}", email);
                throw new ValidationException("Этот имейл уже используется");
            }
            if (Objects.equals(userFromList.getLogin(), login)) {
                log.warn("Обнаружен уже используемый логин: {}", login);
                throw new ValidationException("Этот логин уже используется");
            }

        }
    }
}
