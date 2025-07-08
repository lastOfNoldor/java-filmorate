package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
public class UserService {
    private final UserStorage users;

    @Autowired
    public UserService(UserStorage users) {
        this.users = users;
    }

    public User addFriend(Long userId, Long friendId) {
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));
        User friend = users.findById(friendId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + friendId + " не найден"));
        if (user.getFriends().contains(friendId)) {
            throw new ValidationException("Пользователи уже являются друзьями");
        }
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        users.update(user);
        users.update(friend);
        return user;
    }

    public User deleteFriend(Long userId, Long friendId) {
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));
        User friend = users.findById(friendId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + friendId + " не найден"));
        if (user.getFriends().contains(friendId)) {
            user.getFriends().remove(friendId);
            friend.getFriends().remove(userId);
            users.update(user);
            users.update(friend);
        }
        return user;
    }

    public Collection<User> getFriends(Long userId) {
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));
        return user.getFriends().stream().map(users::findById).flatMap(Optional::stream).toList();
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));
        User other = users.findById(otherId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + otherId + " не найден"));
        return user.getFriends().stream().filter(other.getFriends()::contains).map(users::findById).flatMap(Optional::stream).toList();
    }


}
