package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Set;

@Data
@EqualsAndHashCode(of = "id")
public class Film {

    private Long id;
    @NotBlank
    @NotNull
    private String name;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
    private Long duration;
    private Integer likesCount;
    private Set<FilmGenre> genres;
    private MPARate mpa;
    private Set<Long> likedUserIds;
}
