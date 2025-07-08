package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();


    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Успешное добавление фильма: {}. ID: {}", film.getName(), film.getId());
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            oldFilm.setName(newFilm.getName());
            Optional.ofNullable(newFilm.getDescription()).ifPresent(oldFilm::setDescription);
            Optional.ofNullable(newFilm.getReleaseDate()).ifPresent(oldFilm::setReleaseDate);
            Optional.ofNullable(newFilm.getDuration()).ifPresent(oldFilm::setDuration);
            log.info("Данные фильма успешно обновлены. ID: {}", newFilm.getId());
            films.put(oldFilm.getId(), oldFilm);
            return oldFilm;
        }
        log.error("Не найден фильм для обновления, ID: {}", newFilm.getId());
        throw new NotFoundException("Фильм не найден");
    }

    @Override
    public Optional<Film> findById(Long id) {
        log.info("Выполнение запроса поиска фильма с id: {} в хранилище", id);
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void deleteAll() {
        films.clear();
    }

    private long getNextId() {
        long currentMaxId = films.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;

    }
}
