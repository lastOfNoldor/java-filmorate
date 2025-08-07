package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class MPARate {
    //    G, PG, PG13, R, NC17
    @NotNull
    private Integer id;
    @NotBlank
    @NotNull
    private String name;
}
