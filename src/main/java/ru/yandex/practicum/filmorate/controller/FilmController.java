package ru.yandex.practicum.filmorate.controller;

import ch.qos.logback.classic.Logger;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map <Long, Film> films = new HashMap<>();
    private static final Logger logger = (Logger) LoggerFactory.getLogger(FilmController.class);

    @GetMapping
    public Collection<Film> findAll(){
        return films.values();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@Valid @RequestBody Film film) {
        try {
            if (film.getId() == null) {
                logger.info("Запрос на добавление нового фильма: {}", film.getName());
                filmValidate(film);
                film.setId(getNextId());
                films.put(film.getId(), film);
                logger.info("Успешное добавление фильма: {}. ID: {}", film.getName(), film.getId());
                return film;
            }
            throw new ValidationException("Новый фильм не должен иметь Id до добавления");
        } catch (ValidationException e) {
            logger.error("Ошибка при добавлении нового фильма: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {

        if (newFilm.getId() == null) {
            logger.error("В запросе отсутствует ID фильма");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID фильма не указан");
        }
        logger.info("Запрос на обновление данных фильма с ID: {}", newFilm.getId());
        try {
            filmValidate(newFilm);
            if (films.containsKey(newFilm.getId())) {
                Film oldFilm = films.get(newFilm.getId());
                oldFilm.setName(newFilm.getName());
                Optional.ofNullable(newFilm.getDescription()).ifPresent(oldFilm::setDescription);
                Optional.ofNullable(newFilm.getReleaseDate()).ifPresent(oldFilm::setReleaseDate);
                Optional.ofNullable(newFilm.getDuration()).ifPresent(oldFilm::setDuration);
                logger.info("Данные фильма успешно обновлены. ID: {}", newFilm.getId());
                films.put(oldFilm.getId(),oldFilm);
                return oldFilm;
            }
            logger.error("Не найден фильм для обновления, ID: {}", newFilm.getId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильм не найден");
        } catch (ValidationException e) {
            logger.error("Ошибка обновления данных: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

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
        if(film.getReleaseDate() != null) {
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

    private long getNextId() {
        long currentMaxId = films.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;

    }

    // метод для изоляции тестов, чтобы каждый тест в UserControllerTest не зависил от предыдущего
    @DeleteMapping("/reset")
    public void reset() {
        films.clear();
    }


}
