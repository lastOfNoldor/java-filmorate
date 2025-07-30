package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(of = "id")
public class Film {

    private Long id;
    @NotBlank
    @NotNull
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;
    private int likesCount;
    private List<FilmGenre> filmGenre;
    private MPARate mpaRate;
    private Set<Long> likedUserIds = new HashSet<>();
}
