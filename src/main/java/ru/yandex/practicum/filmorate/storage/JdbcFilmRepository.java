package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.*;

@Repository
@AllArgsConstructor
public class JdbcFilmRepository implements FilmStorage {
    private static final String FIND_ALL_FILMS_QUERY = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
            m.id AS mpa_id, m.name AS mpa_name, COUNT(fl.user_id) AS likes_count
            FROM films f
            LEFT JOIN mpa_rates m ON f.mpa_id = m.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.id, m.name
            ORDER BY id ASC""";
    private static final String FIND_ALL_FILMS_LIKED_IDS = "SELECT film_id, user_id FROM film_likes ORDER BY film_id";
    private static final String FIND_ALL_GENRES_FOR_ALL_FILMS = """
            SELECT fg.film_id, g.id, g.name FROM film_genres fg
            JOIN genres g ON fg.genre_id = g.id
            ORDER BY fg.film_id""";
    private static final String FIND_FILM_BY_ID_QUERY = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
            m.id AS mpa_id, m.name AS mpa_name, COUNT(fl.user_id) AS likes_count
            FROM films f
            LEFT JOIN mpa_rates m ON f.mpa_id = m.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            WHERE f.id = :id
            GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.id, m.name;""";
    private static final String FIND_FILM_LIKED_IDS = "SELECT user_id FROM film_likes WHERE film_id = :id";
    private static final String FIND_GENRES_BY_FILM_ID = """
            SELECT g.id, g.name FROM genres g
            JOIN film_genres fg ON fg.genre_id = g.id
            WHERE fg.film_id = :id ORDER BY g.id""";
    private static final String INSERT_NEW_FILM = "INSERT INTO films(name, description, release_date, duration, mpa_id) " + "VALUES (:name, :description, :release_date, :duration, :mpa_id)";
    private static final String INSERT_LIKED_USERS_IDS = "INSERT INTO film_likes(film_id, user_id) VALUES (:film_id, :user_id)";
    private static final String INSERT_FILM_GENRES = "INSERT INTO film_genres(film_id, genre_id) VALUES (:film_id, :genre_id)";
    private static final String UPDATE_FILM = """
            UPDATE films
            SET name = :name,
                description = :description,
                release_date = :release_date,
                duration = :duration,
                mpa_id = :mpa_id
            WHERE id = :id""";
    private static final String DELETE_OLD_FILM_GENRES = """
            DELETE FROM film_genres
            WHERE film_id = :film_id""";
    private static final String INSERT_NEW_FILM_GENRES = """
            INSERT INTO film_genres(film_id, genre_id)
            VALUES (:film_id, :genre_id)""";
    private static final String DELETE_OLD_FILM_LIKES = """
            DELETE FROM film_likes
            WHERE film_id = :film_id""";
    private static final String INSERT_NEW_FILM_LIKES = """
            INSERT INTO film_likes(film_id, user_id)
            VALUES (:film_id, :user_id)""";
    private static final String DELETE_FILM_BY_ID = "DELETE FROM films WHERE id = :id";
    private static final String DELETE_ALL_FILMS_FOR_TESTS = "DELETE FROM films";
    private static final String GET_ALL_GENRES_FOR_CHECK = "SELECT id, name FROM genres";

    protected final NamedParameterJdbcOperations jdbc;
    protected final FilmRowMapper mapper = new FilmRowMapper();
    protected final GenreRowMapper genreMapper = new GenreRowMapper();

    public List<Film> findAll() {
        // Основные данные (фильмы(cMPA) + жанры + лайки) В 3 ЗАПРОСА, А НЕ N+1
        List<Film> films = jdbc.query(FIND_ALL_FILMS_QUERY, Collections.emptyMap(), mapper);
        // Жанры для всех фильмов одним запросом
        if (!films.isEmpty()) {
            // Загрузка и группировка жанров
            Map<Long, Set<FilmGenre>> genresMap = new HashMap<>();
            jdbc.query(FIND_ALL_GENRES_FOR_ALL_FILMS, (rs, rowNum) -> genresMap.computeIfAbsent(rs.getLong("film_id"), k -> new HashSet<>()).add(genreMapper.mapRow(rs, rowNum)));
            // Загрузка и группировка жанров likeids
            Map<Long, Set<Long>> likedUsersIdsMap = new HashMap<>();
            jdbc.query(FIND_ALL_FILMS_LIKED_IDS, (rs, rowNum) -> likedUsersIdsMap.computeIfAbsent(rs.getLong("film_id"), k -> new HashSet<>()).add(rs.getLong("user_id")));
            // Назначение данных фильмам
            films.forEach(f -> {
                f.setGenres(genresMap.getOrDefault(f.getId(), Set.of()));
                f.setLikedUserIds(likedUsersIdsMap.getOrDefault(f.getId(), Set.of()));
            });
        }
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        try {
            MapSqlParameterSource param = new MapSqlParameterSource("id", id);
            Film film = jdbc.queryForObject(FIND_FILM_BY_ID_QUERY, param, mapper);
            if (film != null) {
                film.setGenres(findFilmGenres(id));
                film.setLikedUserIds(findFilmLikedUserIds(id));
            }
            return Optional.ofNullable(film);
        } catch (DataAccessException ignored) {
            return Optional.empty();
        }
    }

    private Set<Long> findFilmLikedUserIds(Long id) {
        MapSqlParameterSource param = new MapSqlParameterSource("id", id);
        Set<Long> likedUsersIdsMap = new HashSet<>();
        jdbc.query(FIND_FILM_LIKED_IDS, param, (rs, rowNum) -> likedUsersIdsMap.add(rs.getLong("user_id")));
        return likedUsersIdsMap;
    }

    private Set<FilmGenre> findFilmGenres(Long id) {
        MapSqlParameterSource param = new MapSqlParameterSource("id", id);
        Set<FilmGenre> genres = new HashSet<>();
        /*(rs, rowNum) -> genres.add(genreMapper.mapRow(rs, rowNum))  А НЕ
        jdbc.query(FIND_GENRES_BY_FILM_ID, param, genreMapper) ПОТОМУ ЧТО нужен Set а не List*/
        jdbc.query(FIND_GENRES_BY_FILM_ID, param, (rs, rowNum) -> genres.add(genreMapper.mapRow(rs, rowNum)));
        return genres;
    }


    @Override
    @Transactional
    public Film create(Film film) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("name", film.getName()).addValue("description", film.getDescription()).addValue("release_date", film.getReleaseDate()).addValue("duration", film.getDuration()).addValue("mpa_id", film.getMpa() != null ? film.getMpa().getId() : null);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(INSERT_NEW_FILM, params, keyHolder, new String[]{"id"});
        Long filmId = Optional.ofNullable(keyHolder.getKeyAs(Long.class)).orElseThrow(() -> new InternalServerException("Не удалось сохранить данные"));
        if (!film.getLikedUserIds().isEmpty()) {
            batchInsertLikes(filmId, film.getLikedUserIds());
        }
        if (!film.getGenres().isEmpty()) {
            batchInsertGenres(filmId, film.getGenres());
        }
        film.setId(filmId);
        return film;
    }

    private void batchInsertLikes(Long filmId, Set<Long> userIds) {
        SqlParameterSource[] batchParams = userIds.stream().map(userId -> new MapSqlParameterSource().addValue("film_id", filmId).addValue("user_id", userId)).toArray(SqlParameterSource[]::new);
        jdbc.batchUpdate(INSERT_LIKED_USERS_IDS, batchParams);
    }

    private void batchInsertGenres(Long filmId, Set<FilmGenre> genres) {
        SqlParameterSource[] batchParams = genres.stream().map(genre -> new MapSqlParameterSource().addValue("film_id", filmId).addValue("genre_id", genre.getId())).toArray(SqlParameterSource[]::new);
        jdbc.batchUpdate(INSERT_FILM_GENRES, batchParams);
    }

    @Override
    @Transactional
    public Film update(Film film) {
        // 1. Обновление основных данных фильма
        MapSqlParameterSource filmParams = new MapSqlParameterSource().addValue("name", film.getName()).addValue("description", film.getDescription()).addValue("release_date", film.getReleaseDate()).addValue("duration", film.getDuration()).addValue("mpa_id", film.getMpa().getId()).addValue("id", film.getId());

        int rowsUpdated = jdbc.update(UPDATE_FILM, filmParams);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные фильма");
        }
        // 2. Обновление жанров и лайков в одном вызове
        updateFilmRelations(film);
        return film;
    }

    @Transactional
    private void updateFilmRelations(Film film) {
        Long filmId = film.getId();

        // 1. Подготовка данных для batch-операций
        List<MapSqlParameterSource> genreBatchParams = film.getGenres().stream().map(genre -> new MapSqlParameterSource().addValue("film_id", filmId).addValue("genre_id", genre.getId())).toList();

        List<MapSqlParameterSource> likesBatchParams = film.getLikedUserIds().stream().map(userId -> new MapSqlParameterSource().addValue("film_id", filmId).addValue("user_id", userId)).toList();
        // 2. Выполнение в одной транзакции
        jdbc.batchUpdate(DELETE_OLD_FILM_GENRES, new MapSqlParameterSource[]{new MapSqlParameterSource("film_id", filmId)});

        if (!genreBatchParams.isEmpty()) {
            jdbc.batchUpdate(INSERT_NEW_FILM_GENRES, genreBatchParams.toArray(new SqlParameterSource[0]));
        }

        jdbc.batchUpdate(DELETE_OLD_FILM_LIKES, new MapSqlParameterSource[]{new MapSqlParameterSource("film_id", filmId)});

        if (!likesBatchParams.isEmpty()) {
            jdbc.batchUpdate(INSERT_NEW_FILM_LIKES, likesBatchParams.toArray(new SqlParameterSource[0]));
        }
    }

    public Set<FilmGenre> getAllGenres() {
        return Set.copyOf(jdbc.query(GET_ALL_GENRES_FOR_CHECK, Collections.emptyMap(), genreMapper));
    }


    @Transactional
    public boolean deleteFilmById(Long id) {
        MapSqlParameterSource param = new MapSqlParameterSource("id", id);
        int affectedRows = jdbc.update(DELETE_FILM_BY_ID, param);
        return affectedRows == 1;
    }

    @Transactional
    @Override
    public boolean deleteAll() {
        int affectedRows = jdbc.update(DELETE_ALL_FILMS_FOR_TESTS, Collections.emptyMap());
        return affectedRows > 0;
    }
}
