package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPARate;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmRowMapper implements RowMapper<Film> {
    protected final MPARateRowMapper mpaRateRowMapper = new MPARateRowMapper();

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getLong("duration"));
        Integer likes = rs.getObject("likes_count", Integer.class);
        film.setLikesCount(likes != null ? likes : 0);
        MPARate rate = new MPARate();
        rate.setId(rs.getInt("mpa_id")); // аллиасы из FIND_ALL_FILMS_QUERY
        rate.setName(rs.getString("mpa_name"));
        film.setMpa(rate);
        return film;
    }
}
