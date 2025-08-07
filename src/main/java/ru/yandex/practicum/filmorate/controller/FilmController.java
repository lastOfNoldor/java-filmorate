package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public List<Film> findAll() {
        log.info("Запрос на получение данных о всех фильмах");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Запрос на получение данных о фильме с Id: {}", id);
        return filmService.findById(id);
    }

    @PutMapping("{id}/like/{userId}")
    public int addLikeToFilm(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на установку лайка) фильму с Id: {} пользователем c Id: {}", id, userId);
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public int removeLikeToFilm(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на удаление лайка( фильму с Id: {} пользователем c Id: {}", id, userId);
        return filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10", name = "count") int count) {
        log.info("Запрос на получение {} самых популярных фильмов в базе", count);
        return filmService.getPopularFilms(count);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Запрос на добавление нового фильма: {}", film.getName());
        filmValidate(film);
        return filmService.create(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        log.info("Запрос на обновление данных фильма с ID: {}", newFilm.getId());
        filmValidate(newFilm);
        return filmService.update(newFilm);
    }

    private void filmValidate(Film film) {
        if (film.getName() == null) {
            log.error("Не указано имя фильма");
            throw new ValidationException("Не указано имя фильма");
        }
        if (film.getDescription() != null) {
            if (film.getDescription().length() > 200) {
                log.warn("Указано слишком длинное описание фильма");
                throw new ValidationException("Слишком длинное описание фильма.");
            }
        }
        if (film.getReleaseDate() != null) {
            if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
                log.warn("Дата фильма меньше допустимого значения: {}", film.getReleaseDate());
                throw new ValidationException("Недопустимая дата фильма");
            }
        }
        if (film.getDuration() != null) {
            if (film.getDuration() <= 0) {
                log.error("Продолжительность фильма меньше допустимого значения: {}", film.getDuration());
                throw new ValidationException("Недопустимая продолжительность фильма");
            }
        }
    }

    @DeleteMapping("/reset")
    public void reset() {
        filmService.deleteAll();
    }


}
