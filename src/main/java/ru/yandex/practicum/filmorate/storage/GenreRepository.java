package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.util.List;
import java.util.Optional;

public interface GenreRepository {
    List<FilmGenre> findAll();

    Optional<FilmGenre> findById(Integer id);
}
