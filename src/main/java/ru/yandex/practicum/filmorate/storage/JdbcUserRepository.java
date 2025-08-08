package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class JdbcUserRepository implements UserStorage {
    private static final String FIND_ALL_USERS = "SELECT id,email,login,name,birthday FROM users";
    private static final String FIND_USER_BY_ID_QUERY = "SELECT id, email, login, name, birthday FROM users WHERE id = :id";
    private static final String DELETE_USER_BY_ID_QUERY = "DELETE FROM users WHERE id = :id";
    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday) " + "VALUES (:email, :login, :name, :birthday)";
    private static final String INSERT_FRIENDSHIP = "INSERT INTO friendships(user_id, friend_id)" + "VALUES (:user_id, :friend_id)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = :email, login = :login, name = :name, birthday = :birthday WHERE id = :id";
    private static final String DELETE_FRIENDSHIP = "DELETE FROM friendships WHERE user_id = :user_id AND friend_id = :friend_id";
    private static final String FIND_FRIENDS = """
            SELECT u.id, u.email, u.login, u.name, u.birthday
            FROM users u
            JOIN friendships f ON u.id = f.friend_id
            WHERE f.user_id = :user_id""";
    private static final String DELETE_ALL_USERS = "DELETE FROM users";
    protected final NamedParameterJdbcOperations jdbc;
    protected final UserRowMapper mapper = new UserRowMapper();


    @Override
    public List<User> findAll() {
        return jdbc.query(FIND_ALL_USERS, Collections.emptyMap(), mapper);
    }


    public boolean deleteUserById(Long id) {
        int affectedRows = jdbc.update(DELETE_USER_BY_ID_QUERY, new MapSqlParameterSource().addValue("id", id));
        return affectedRows == 1;
    }

    @Override
    public User create(User user) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("email", user.getEmail()).addValue("login", user.getLogin()).addValue("name", user.getName()).addValue("birthday", user.getBirthday());
        jdbc.update(INSERT_QUERY, params, keyHolder, new String[]{"id"});
        Long id = Optional.ofNullable(keyHolder.getKeyAs(Long.class)).orElseThrow(() -> new InternalServerException("Не удалось сохранить данные"));
        user.setId(id);
        return user;
    }

    public boolean saveFriendshipRequest(Long userId, Long friendId) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("user_id", userId).addValue("friend_id", friendId);
        try {
            int affectedRows = jdbc.update(INSERT_FRIENDSHIP, params);
            return affectedRows == 1;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

    public boolean deleteFriendshipRequest(Long userId, Long friendId) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("user_id", userId).addValue("friend_id", friendId);
        int affectedRows = jdbc.update(DELETE_FRIENDSHIP, params);
        return affectedRows == 1;
    }

    @Override
    public User update(User user) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("email", user.getEmail()).addValue("login", user.getLogin()).addValue("name", user.getName()).addValue("birthday", user.getBirthday()).addValue("id", user.getId());
        int rowsUpdated = jdbc.update(UPDATE_QUERY, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        try {
            User result = jdbc.queryForObject(FIND_USER_BY_ID_QUERY, new MapSqlParameterSource().addValue("id", id), mapper);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public List<User> findFriends(Long userId) {
        return List.copyOf(jdbc.query(FIND_FRIENDS, new MapSqlParameterSource().addValue("user_id", userId), mapper));
    }

    @Override
    public boolean deleteAll() {
        int affectedRows = jdbc.update(DELETE_ALL_USERS, Collections.emptyMap());
        return affectedRows > 0;
    }
}
