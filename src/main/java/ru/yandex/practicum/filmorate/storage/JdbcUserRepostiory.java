package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class JdbcUserRepostiory implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT id,email,login,name,birthday FROM users";
    private static final String FIND_FRIENDSHIP_FOR_USER = "SELECT requester_id, addressee_id, status FROM friendships WHERE requester_id = ? OR addressee_id = ?";
    private static final String FIND_USER_BY_ID_QUERY = "SELECT email, login, name, birthday FROM users WHERE id = ?";
    private static final String DELETE_USER_BY_ID_QUERY = "DELETE FROM users WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(username, email, login, name, birthday)" +
            "VALUES (?, ?, ?, ?, ?) returning id";
    private static final String INSERT_FRIENDSHIP_QUERY = "INSERT INTO friendships(requester_id, addressee_id, status)" + "VALUES (?,?,?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String UPDATE_FRIENDSHIP_QUERY = "UPDATE friendships SET status = ? WHERE requester_id = ? AND addressee_id = ?";
    private static final String DELETE_FRIENDSHIP_QUERY = "DELETE FROM friendships WHERE requester_id = ? AND addressee_id = ?";
    private static final String MAKE_UNCONFIRMED_FRIENDSHIP_BY_INITIATOR = "UPDATE friendships SET requester_id = ?, addressee_id = ?, status = ? WHERE (requester_id = ? AND addressee_id = ?) OR (addressee_id = ? AND requester_id = ?)";
    protected final JdbcTemplate jdbc;
    protected final UserRowMapper mapper = new UserRowMapper();


    @Override
    public List<User> findAll() {
        List<User> result = jdbc.query(FIND_ALL_QUERY, mapper);
        for (User user : result) {
            user.setFriendship(findFriendshipForUser(user.getId()));

        }
        return result;
    }

    public boolean deleteUserById(Long id) {
        int affectedRows = jdbc.update(DELETE_USER_BY_ID_QUERY, id);
        return affectedRows == 1;
    }

    private Map<Long, FriendshipStatus> findFriendshipForUser(Long userId) {
        return jdbc.query(
                FIND_FRIENDSHIP_FOR_USER,
                rs -> {
                    Map<Long, FriendshipStatus> friendsMap = new HashMap<>();
                    while (rs.next()) {
                        Long requesterId = rs.getLong("requester_id");
                        Long addresseeId = rs.getLong("addressee_id");
                        FriendshipStatus status = FriendshipStatus.valueOf(rs.getString("status"));
                        Long friendId = requesterId.equals(userId) ? addresseeId : requesterId;
                        friendsMap.put(friendId, status);
                    }
                    return friendsMap;
                },
                userId, userId  // Параметры для подтсановки в запрос
        );
    }

    @Override
    public User create(User user) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        Object[] params = new Object[] {
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        };
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;}, keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);
        if ((id != null)) {
            user.setId(id);
            return user;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    public boolean saveFriendshipRequest(Long requesterId, Long addresseeId, FriendshipStatus status) {
        int affectedRows = jdbc.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(INSERT_FRIENDSHIP_QUERY);
                    ps.setLong(1, requesterId);
                    ps.setLong(2, addresseeId);
                    ps.setString(3, status.name());
                    return ps;
                }
        );
        return affectedRows == 1;
    }

    @Override
    public User update(User user) {
        Object[] params = new Object[] {
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        };
        int rowsUpdated = jdbc.update(UPDATE_QUERY, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        return user;
    }

    public boolean updateFriendshipStatus(Long requesterId, Long addresseeId, FriendshipStatus status) {
        Object[] params = new Object[] {
                status.name(),
                requesterId,
                addresseeId
        };
        int affectedRows = jdbc.update(UPDATE_FRIENDSHIP_QUERY, params);
        return affectedRows == 1;
    }

    public boolean makeConfirmedFriendshipUnconfirmed(Long requesterId, Long friendId, FriendshipStatus status){
        Object[] params = new Object[] {
                friendId,
                requesterId,
                status.name(),
                requesterId,
                friendId,
                friendId,
                requesterId
        };
        int affectedRows = jdbc.update(MAKE_UNCONFIRMED_FRIENDSHIP_BY_INITIATOR, params);
        return affectedRows == 1;
    }

    @Override
    public Optional<User> findById(Long id) {
        try {
            User result = jdbc.queryForObject(FIND_USER_BY_ID_QUERY, mapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public boolean deleteUnconfirmedFriendship(Long userId, Long friendId) {
        Object[] params = new Object[] {
                userId,
                friendId
        };
        int affectedRows = jdbc.update(DELETE_FRIENDSHIP_QUERY, params);
        return affectedRows == 1;
    }

    @Override
    public void deleteAll() {

    }
}
