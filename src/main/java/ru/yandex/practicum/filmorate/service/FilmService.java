package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage films;
    private final UserStorage users;

    @Autowired
    public FilmService(FilmStorage films, UserStorage users) {
        this.films = films;
        this.users = users;
    }

    public int addLike(Long filmId, Long userId) {
        Film film = films.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм с id: " + filmId + " не найден."));
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден."));
        if (film.getLikedUserIds().contains(userId)) {
            return film.getLikes();
        }
        film.setLikes(film.getLikes() + 1);
        film.getLikedUserIds().add(user.getId());
        films.update(film);
        return film.getLikes();
    }

    public int removeLike(Long filmId, Long userId) {
        Film film = films.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм с id: " + filmId + " не найден."));
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден."));
        if (film.getLikedUserIds().contains(userId)) {
            film.setLikes(film.getLikes() - 1);
            film.getLikedUserIds().remove(user.getId());
            films.update(film);
        }
        return film.getLikes();
    }

    public Collection<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Парметр count должен быть больше нуля");
        }
        return films.findAll().stream().sorted(Comparator.comparingInt(Film::getLikes).reversed()).limit(count).toList();
    }


}
