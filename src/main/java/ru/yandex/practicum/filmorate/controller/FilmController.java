package ru.yandex.practicum.filmorate.controller;

import ch.qos.logback.classic.Logger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(FilmController.class);


    @GetMapping
    public List<Film> findAll() {
        logger.info("Запрос на получение данных о всех фильмах");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        logger.info("Запрос на получение данных о фильме с Id: {}", id);
        return filmService.findById(id);
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
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10", name = "count") int count) {
        logger.info("Запрос на получение {} самых популярных фильмов в базе", count);
        return filmService.getPopularFilms(count);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@Valid @RequestBody Film film) {
        logger.info("Запрос на добавление нового фильма: {}", film.getName());
        filmValidate(film);
        return filmService.create(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        logger.info("Запрос на обновление данных фильма с ID: {}", newFilm.getId());
        filmValidate(newFilm);
        return filmService.update(newFilm);
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
        filmService.deleteAll();
    }


}
