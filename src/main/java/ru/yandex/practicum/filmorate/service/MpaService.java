package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPARate;
import ru.yandex.practicum.filmorate.storage.JdbcMpaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {

    private final JdbcMpaRepository mpaRepository;

    public List<MPARate> getAllMpaRatings() {
        return mpaRepository.findAll();
    }

    public MPARate getMpaRatingById(Integer id) {
        return mpaRepository.findById(id).orElseThrow(() -> new NotFoundException("Рейтинг MPA с ID " + id + " не найден"));
    }

//    public void deleteAllMpaRatings() {
//        mpaRepository.deleteAll();
//    }
}
