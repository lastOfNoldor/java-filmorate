package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class JdbcGenreRepository implements GenreRepository {
    private static final String FIND_ALL_SQL = "SELECT id, name FROM genres ORDER BY id";
    private static final String FIND_BY_ID_SQL = "SELECT id, name FROM genres WHERE id = :id";
    private final NamedParameterJdbcOperations jdbc;
    private final GenreRowMapper mapper = new GenreRowMapper();

    @Override
    public List<FilmGenre> findAll() {
        return jdbc.query(FIND_ALL_SQL, mapper);
    }

    @Override
    public Optional<FilmGenre> findById(Integer id) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(FIND_BY_ID_SQL, new MapSqlParameterSource("id", id), mapper));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }


}
