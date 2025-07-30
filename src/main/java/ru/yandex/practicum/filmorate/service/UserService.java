package ru.yandex.practicum.filmorate.service;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.JdbcUserRepostiory;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final JdbcUserRepostiory users;
    private final Set<String> emailSet = new HashSet<>();
    private final Set<String> loginSet = new HashSet<>();

    @PostConstruct
    public void initCredentialsSet() {
        users.findAll().forEach(user -> emailSet.add(user.getEmail()));
        users.findAll().forEach(user -> loginSet.add(user.getLogin()));
    }


    public User addFriend(Long userId, Long friendId) {
        // Получаем пользователей (с проверкой существования)
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);
        // Проверяем существующие статусы
        FriendshipStatus userToFriendStatus = user.getFriendship().get(friendId);
        FriendshipStatus friendToUserStatus = friend.getFriendship().get(userId);
        // Обрабатываем все возможные кейсы
        if (userToFriendStatus == null && friendToUserStatus == null) {
            // Нет существующей связи - создаем новую заявку
            if (!users.saveFriendshipRequest(userId, friendId, FriendshipStatus.UNCONFIRMED)) {
                throw new InternalServerException("Не удалось сохранить данные");
            }
            user.getFriendship().put(friendId, FriendshipStatus.UNCONFIRMED);
        } else if (userToFriendStatus == FriendshipStatus.UNCONFIRMED && friendToUserStatus == null) {
            throw new ValidationException("Заявка в друзья уже отправлена");
        } else if (userToFriendStatus == FriendshipStatus.CONFIRMED || friendToUserStatus == FriendshipStatus.CONFIRMED) {
            throw new ValidationException("Пользователи уже являются друзьями");
        } else if (friendToUserStatus == FriendshipStatus.UNCONFIRMED && userToFriendStatus == null) {
            // Встречная заявка - подтверждаем дружбу
            if (!users.updateFriendshipStatus(userId, friendId, FriendshipStatus.CONFIRMED)) {
                throw new InternalServerException("Не удалось сохранить данные");
            }
            user.getFriendship().put(friendId, FriendshipStatus.CONFIRMED);
            friend.getFriendship().put(userId, FriendshipStatus.CONFIRMED);
        } else {
            throw new ValidationException("Некорректное состояние дружбы");
        }
        return user;
    }

    private User getUserOrThrow(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("Пользователь с id: " + id + " не найден"));
    }

    public User deleteFriend(Long userId, Long friendId) {
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));
        User friend = users.findById(friendId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + friendId + " не найден"));
        if (user.getFriendship().get(friendId) == FriendshipStatus.CONFIRMED) {
            users.makeConfirmedFriendshipUnconfirmed(userId,friendId,FriendshipStatus.UNCONFIRMED);
            friend.getFriendship().put(userId, FriendshipStatus.UNCONFIRMED);
        } else if (user.getFriendship().get(friendId) == FriendshipStatus.UNCONFIRMED) {
            users.deleteUnconfirmedFriendship(userId,friendId);
        } else {
            throw new NotFoundException("Дружба с этим пользователем не существует");
        }
        user.getFriendship().remove(friendId);
        return user;
    }

    public List<User> getFriends(Long userId) {
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));
        return user.getFriendship().keySet().stream().map(users::findById).flatMap(Optional::stream).toList();
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));
        User other = users.findById(otherId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + otherId + " не найден"));
        return user.getFriendship().keySet().stream().filter(other.getFriendship().keySet()::contains).map(users::findById).flatMap(Optional::stream).toList();
    }


    public List<User> findAll() {
        return users.findAll();
    }


    public User findById(Long id) {
        log.info("Выполнение запроса поиска пользователя с id: {} в хранилище", id);
        Optional<User> byId = users.findById(id);
        if (byId.isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }
        log.info("Выполнение запроса поиска пользователя с id: {} в хранилище", id);
        return byId.get();
    }


    public User create(@Valid User createdUser) {
        if (createdUser.getId() != null) {
            log.error("В запросе на создание присутствует ID пользователя");
            throw new ValidationException("Новый пользователь не должен иметь Id до регистрации");
        }
        emailAlreadyExists(createdUser.getEmail());
        loginAlreadyExists(createdUser.getLogin());
        User result = users.create(createdUser);
        log.info("Успешное создание пользователя: {}. ID: {}", result.getLogin(), result.getId());
        emailSet.add(createdUser.getEmail());
        loginSet.add(createdUser.getLogin());
        return result;
    }

    public User update(@Valid User newUser) {
        if (newUser.getId() == null) {
            log.error("В запросе отсутствует ID пользователя");
            throw new ValidationException("ID пользователя не указан");
        }
        Optional<User> byId = users.findById(newUser.getId());
        if (byId.isEmpty()) {
            log.error("Не найден фильм для обновления, ID: {}", newUser.getId());
            throw new NotFoundException("Пользователь не найден");
        }
        User oldUser = byId.get();
        if (!Objects.equals(newUser.getEmail(), oldUser.getEmail())) {
            emailAlreadyExists(newUser.getEmail());
        }
        if (!Objects.equals(newUser.getLogin(), oldUser.getLogin())) {
            loginAlreadyExists(newUser.getLogin());
        }
        emailSet.remove(oldUser.getEmail());
        emailSet.add(newUser.getEmail());
        loginSet.remove(oldUser.getEmail());
        loginSet.add(newUser.getLogin());
        oldUser.setLogin(newUser.getLogin());
        oldUser.setEmail(newUser.getEmail());
        Optional.ofNullable(newUser.getName()).ifPresent(oldUser::setName);
        Optional.ofNullable(newUser.getBirthday()).ifPresent(oldUser::setBirthday);
        log.info("Данные пользователя успешно обновлены. ID: {}", newUser.getId());
        return users.update(oldUser);
    }

    private void emailAlreadyExists(String email) {
        if (emailSet.contains(email)) {
            log.warn("Обнаружен уже используемый имейл: {}", email);
            throw new ValidationException("Этот имейл уже используется!");
        }
    }

    private void loginAlreadyExists(String login) {
        if (loginSet.contains(login)) {
            log.warn("Обнаружен уже используемый логин: {}", login);
            throw new ValidationException("Этот логин уже используется");
        }
    }

    public void deleteAll() {   //метод для тестов
        users.deleteAll();
    }

    public void clearCredentialsSets() { //метод для тестов
        emailSet.clear();
        loginSet.clear();
    }
}
