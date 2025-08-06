package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = "id")
public class User {
    private Long id;
    @Email
    @NotBlank
    @NotNull
    private String email;
    @NotNull
    @NotBlank
    private String login;
    private String name;
    private LocalDate birthday;
}
