package ru.yandex.practicum.filmorate.controller;

import ch.qos.logback.classic.Logger;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmStorage films;
    private final FilmService filmService;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(FilmController.class);

    @Autowired
    public FilmController(FilmStorage films, FilmService filmService) {
        this.films = films;
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        logger.info("Запрос на получение данных о всех фильмах");
        return films.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Film> getFilmById(@PathVariable Long id) {
        logger.info("Запрос на получение данных о фильме с Id: {}", id);
        return films.findById(id);
    }

    @PutMapping("{id}/like/{userId}")
    public int addLikeToFilm(@PathVariable Long id, @PathVariable Long userId) {
        logger.info("Запрос на установку лайка) фильму с Id: {} пользователем c Id: {}", id, userId);
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public int removeLikeToFilm(@PathVariable Long id, @PathVariable Long userId) {
        logger.info("Запрос на удаление лайка( фильму с Id: {} пользователем c Id: {}", id, userId);
        return filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10", name = "count") int count) {
        logger.info("Запрос на получение {} самых популярных фильмов в базе", count);
        return filmService.getPopularFilms(count);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@Valid @RequestBody Film film) {
        if (film.getId() != null) {
            logger.error("В запросе на создание присутствует ID фильма");
            throw new ValidationException("Фильм с id: " + film.getId() + " уже существует");
        }
        logger.info("Запрос на добавление нового фильма: {}", film.getName());
        filmValidate(film);
        return films.create(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            logger.error("В запросе отсутствует ID фильма");
            throw new ValidationException("ID фильма не указан");
        }
        logger.info("Запрос на обновление данных фильма с ID: {}", newFilm.getId());
        filmValidate(newFilm);
        return films.update(newFilm);
    }

    private void filmValidate(Film film) {
        if (film.getName() == null) {
            logger.error("Не указано имя фильма");
            throw new ValidationException("Не указано имя фильма");
        }
        if (film.getDescription() != null) {
            if (film.getDescription().length() > 200) {
                logger.warn("Указано слишком длинное описание фильма");
                throw new ValidationException("Слишком длинное описание фильма.");
            }
        }
        if (film.getReleaseDate() != null) {
            if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
                logger.warn("Дата фильма меньше допустимого значения: {}", film.getReleaseDate());
                throw new ValidationException("Недопустимая дата фильма");
            }
        }
        if (film.getDuration() != null) {
            if (film.getDuration() <= 0) {
                logger.error("Продолжительность фильма меньше допустимого значения: {}", film.getDuration());
                throw new ValidationException("Недопустимая продолжительность фильма");
            }
        }
    }

    // метод для изоляции тестов, чтобы каждый тест в UserControllerTest не зависил от предыдущего
    @DeleteMapping("/reset")
    public void reset() {
        films.deleteAll();
    }


}
