package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage films;
    private final UserStorage users;

    public List<Film> findAll() {
        return films.findAll();
    }

    public Film findById(Long id) {
        log.info("Выполнение запроса поиска фильма с id: {} в хранилище", id);
        Optional<Film> byId = films.findById(id);
        if (byId.isEmpty()) {
            throw new NotFoundException("Фильм не найден");
        }
        return byId.get();
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.error("В запросе отсутствует ID фильма");
            throw new ValidationException("ID фильма не указан");
        }
        Optional<Film> byId = films.findById(newFilm.getId());
        if (byId.isEmpty()) {
            log.error("Не найден фильм для обновления, ID: {}", newFilm.getId());
            throw new NotFoundException("Фильм не найден");
        }
        Film oldFilm = byId.get();
        oldFilm.setName(newFilm.getName());
        Optional.ofNullable(newFilm.getDescription()).ifPresent(oldFilm::setDescription);
        Optional.ofNullable(newFilm.getFilmGenre()).ifPresent(oldFilm::setFilmGenre);
        Optional.ofNullable(newFilm.getMpaRate()).ifPresent(oldFilm::setMpaRate);
        Optional.ofNullable(newFilm.getReleaseDate()).ifPresent(oldFilm::setReleaseDate);
        Optional.ofNullable(newFilm.getDuration()).ifPresent(oldFilm::setDuration);
        log.info("Данные фильма успешно обновлены. ID: {}", newFilm.getId());
        return films.update(oldFilm);
    }

    public int addLike(Long filmId, Long userId) {
        Film film = films.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм с id: " + filmId + " не найден."));
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден."));
        if (film.getLikedUserIds().contains(userId)) {
            return film.getLikesCount();
        }
        film.setLikesCount(film.getLikesCount() + 1);
        film.getLikedUserIds().add(user.getId());
        films.update(film);
        return film.getLikesCount();
    }

    public int removeLike(Long filmId, Long userId) {
        Film film = films.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм с id: " + filmId + " не найден."));
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден."));
        if (film.getLikedUserIds().contains(userId)) {
            film.setLikesCount(film.getLikesCount() - 1);
            film.getLikedUserIds().remove(user.getId());
            films.update(film);
        }
        return film.getLikesCount();
    }

    public Film create(Film createdFilm) {
        if (createdFilm.getId() != null) {
            log.error("В запросе на создание присутствует ID фильма");
            throw new ValidationException("Фильм с id: " + createdFilm.getId() + " уже существует");
        }
        return films.create(createdFilm);
    }

    public void deleteAll() {
        films.deleteAll();
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть больше нуля");
        }
        return films.findAll().stream().sorted(Comparator.comparingInt(Film::getLikesCount).reversed()).limit(count).toList();
    }


}
