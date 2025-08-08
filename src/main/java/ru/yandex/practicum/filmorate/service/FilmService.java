package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.JdbcFilmRepository;
import ru.yandex.practicum.filmorate.storage.JdbcUserRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final JdbcFilmRepository films;
    private final JdbcUserRepository users;

    public List<Film> findAll() {
        return films.findAll();
    }

    public Film findById(Long id) {
        log.info("Выполнение запроса поиска фильма с id: {} в хранилище", id);
        return films.findById(id).orElseThrow(() -> new NotFoundException("Фильм не найден"));
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.error("В запросе отсутствует ID фильма");
            throw new ValidationException("ID фильма не указан");
        }
        Film oldFilm = films.findById(newFilm.getId()).orElseThrow(() -> new NotFoundException("Фильм не найден"));
//        checkMpa(oldFilm);
        checkGenres(oldFilm);
        oldFilm.setName(newFilm.getName());
        Optional.ofNullable(newFilm.getDescription()).ifPresent(oldFilm::setDescription);
        Optional.ofNullable(newFilm.getGenres()).ifPresent(oldFilm::setGenres);
        Optional.ofNullable(newFilm.getMpa()).ifPresent(oldFilm::setMpa);
        Optional.ofNullable(newFilm.getReleaseDate()).ifPresent(oldFilm::setReleaseDate);
        Optional.ofNullable(newFilm.getDuration()).ifPresent(oldFilm::setDuration);
        Film result = films.update(oldFilm);
        log.info("Данные фильма успешно обновлены. ID: {}", result.getId());
        return result;
    }

    public Film create(Film createdFilm) {
        if (createdFilm.getId() != null) {
            log.error("В запросе на создание присутствует ID фильма");
            throw new ValidationException("Фильм с id: " + createdFilm.getId() + " уже существует");
        }
        checkGenres(createdFilm);
        if (createdFilm.getLikedUserIds() == null) {
            createdFilm.setLikedUserIds(Collections.emptySet());
        }
        return films.create(createdFilm);
    }


    private void checkGenres(Film film) {
        if (film.getGenres() != null) {
            Set<FilmGenre> filmGenres = film.getGenres();
            Set<FilmGenre> genres = films.getAllGenres();
            if (!genres.containsAll(filmGenres)) {
                throw new NotFoundException("Жанр фильма не найден в БД");
            }
            if (film.getGenres() != null) {
                List<FilmGenre> sorted = new ArrayList<>(film.getGenres());
                sorted.sort(Comparator.comparingLong(FilmGenre::getId));
                film.setGenres(new LinkedHashSet<>(sorted));
            }
        } else {
            film.setGenres(Collections.emptySet());
        }
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
        Film film = getFilmOrThrow(filmId);
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден."));
        if (film.getLikedUserIds().contains(userId)) {
            film.setLikesCount(film.getLikesCount() - 1);
            film.getLikedUserIds().remove(user.getId());
            films.update(film);
        }
        return film.getLikesCount();
    }

    private Film getFilmOrThrow(Long id) {
        return films.findById(id).orElseThrow(() -> new NotFoundException("Фильм с id: " + id + " не найден"));
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
