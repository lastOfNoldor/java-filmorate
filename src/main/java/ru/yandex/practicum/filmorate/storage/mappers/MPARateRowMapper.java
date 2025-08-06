package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.MPARate;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MPARateRowMapper implements RowMapper<MPARate> {
    @Override
    public MPARate mapRow(ResultSet rs, int rowNum) throws SQLException {
        MPARate mpa = new MPARate();
        mpa.setId(rs.getInt("id"));
        mpa.setName(rs.getString("name"));
        return mpa;
    }
}
