package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {

    private Long id;
    @NotBlank
    @NotNull
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;
}
