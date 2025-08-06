package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.MPARate;

import java.util.List;
import java.util.Optional;

public interface MpaRepository {
    List<MPARate> findAll();

    Optional<MPARate> findById(Integer id);

}
