package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.storage.JdbcGenreRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final JdbcGenreRepository genreRepository;

    public List<FilmGenre> getAllGenres() {
        return genreRepository.findAll();
    }

    public FilmGenre getGenreById(Integer id) {
        return genreRepository.findById(id).orElseThrow(() -> new NotFoundException("Жанр с ID " + id + " не найден"));
    }


}
