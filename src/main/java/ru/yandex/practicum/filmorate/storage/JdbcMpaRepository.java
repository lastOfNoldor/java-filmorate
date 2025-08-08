package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MPARate;
import ru.yandex.practicum.filmorate.storage.mappers.MPARateRowMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class JdbcMpaRepository implements MpaRepository {
    private static final String GET_ALL_MPA = "SELECT id, name FROM mpa_rates ORDER BY id";
    private static final String GET_MPA_BY_ID = "SELECT id, name FROM mpa_rates WHERE id = :id";
    protected final NamedParameterJdbcOperations jdbc;
    protected final MPARateRowMapper mapper = new MPARateRowMapper();


    public Optional<MPARate> findById(Integer id) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(GET_MPA_BY_ID, new MapSqlParameterSource("id", id), mapper));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    public List<MPARate> findAll() {
        return List.copyOf(jdbc.query(GET_ALL_MPA, Collections.emptyMap(), mapper));
    }


}
