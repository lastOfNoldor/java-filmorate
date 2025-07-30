package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class FilmGenre {
//    COMEDY, DRAMA, CARTOON, THRILLER, DOCUMENTAL, ACTION
    @NotNull
    int id;
    @NotBlank
    @NotNull
    String name;
}
