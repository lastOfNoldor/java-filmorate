package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MPARate;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
@Slf4j
public class MpaController {

    private final MpaService mpaService;

    @GetMapping
    public List<MPARate> getAllMpaRatings() {
        log.info("Запрос на получение всех рейтингов MPA");
        return mpaService.getAllMpaRatings();
    }

    @GetMapping("/{id}")
    public MPARate getMpaRatingById(@PathVariable Integer id) {
        log.info("Запрос на получение рейтинга MPA с ID: {}", id);
        return mpaService.getMpaRatingById(id);
    }

}
