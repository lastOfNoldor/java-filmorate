package ru.yandex.practicum.filmorate.ControllersTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(FilmController.class)
public class FilmControllerTest {
    private final String validTestFilm = """
    {
      "name": "Ironweed",
      "description": "wow very nice",
      "releaseDate": "1987-01-01",
      "duration": 15000
    }
    """;

    private void createValidFilm(String filmJson) throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isCreated());
    }

    private void createInvalidFilm(String filmJson) throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isBadRequest());
    }

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void clearData() throws Exception {
        mockMvc.perform(delete("/films/reset")).andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestWhenDataInvalid() throws Exception {
        String invalidNameJson = """
            {
              "name": "",
              "description": "wow very nice",
              "releaseDate": "1987-01-01",
              "duration": 15000
            }
            """;
        createInvalidFilm(invalidNameJson);

        String invalidDescriptionJson = """
            {
              "name": "Ironweed",
              "description": "wow very nice111111111111111111111111111111111111111111111111111111111111111111111111111
              11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111",
              "releaseDate": "1987-01-01",
              "duration": 15000
            }
            """;
        createInvalidFilm(invalidDescriptionJson);

        String invalidReleaseJson = """
            {
              "name": "Ironweed",
              "description": "wow very nice",
              "releaseDate": "1227-01-01",
              "duration": 15000
            }
            """;
        createInvalidFilm(invalidReleaseJson);

        String invalidDurationJson = """
            {
              "name": "",
              "description": "wow very nice",
              "releaseDate": "1227-01-01",
              "duration": 0
            }
            """;
        createInvalidFilm(invalidDurationJson);

        String invalidDuration2Json = """
            {
              "name": "",
              "description": "wow very nice",
              "releaseDate": "1227-01-01",
              "duration": -10
            }
            """;
        createInvalidFilm(invalidDuration2Json);

        String invalidEmptyJson = """
            {
            
            }
            """;
        createInvalidFilm(invalidEmptyJson);

        String nameAsNumberDurationAsString = """
            {
              "name": 0,
              "description": "wow very nice",
              "releaseDate": "1227-01-01",
              "duration": "ss"
            }
            """;
        createInvalidFilm(nameAsNumberDurationAsString);
    }

    @Test
    void shouldReturnAllFilms() throws Exception {
        createValidFilm(validTestFilm);
        String validTestFilm2 = """
    {
      "name": "Lord Of The Rings",
      "description": "wow very nice",
      "releaseDate": "2001-01-01",
      "duration": 15000
    }
    """;
        createValidFilm(validTestFilm2);
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray()) // проверка, что вернулся JSON-массив
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].name").value("Lord Of The Rings"))
                .andExpect(jsonPath("$[1].description").value("wow very nice"))
                .andExpect(jsonPath("$[1].releaseDate").value("2001-01-01"))
                .andExpect(jsonPath("$[1].duration").value(15000))
                .andExpect(jsonPath("$[0].name").value("Ironweed"))
                .andDo(result -> {
                    System.out.println("Response JSON: " + result.getResponse().getContentAsString());
                });
    }

    @Test
    void updateSuccessAndUpdateLoginEmailSuccess() throws Exception {
        createValidFilm(validTestFilm);
        String updatedValidTestFilm = """
                {
                   "id": 1,
                  "name": "Ironweed2",
                  "description": "wow very nice2",
                  "releaseDate": "1927-01-01",
                   "duration": 1288
                }
                """;

        mockMvc.perform(put("/films").contentType(MediaType.APPLICATION_JSON).content(updatedValidTestFilm)).andExpect(status().isOk());
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Ironweed2"))
                .andExpect(jsonPath("$[0].description").value("wow very nice2"))
                .andExpect(jsonPath("$[0].releaseDate").value("1927-01-01"))
                .andExpect(jsonPath("$[0].duration").value(1288));

    }

    @Test
    void idMissingBadRequest() throws Exception {
        createValidFilm(validTestFilm);
        String updatedValidTestFilm = """
                {
                  "name": "Ironweed2",
                  "description": "wow very nice2",
                  "releaseDate": "1927-01-01",
                   "duration": 1288
                }
                """;
        mockMvc.perform(put("/films").contentType(MediaType.APPLICATION_JSON).content(updatedValidTestFilm)).andExpect(status().isBadRequest());
    }

    @Test
    void FilmWithThatIdMissingNotFound() throws Exception {
        createValidFilm(validTestFilm);
        String updatedInvalidTestFilm = """
                {
                    "id": "3",
                  "name": "Ironweed2",
                  "description": "wow very nice2",
                  "releaseDate": "1927-01-01",
                   "duration": 1288
                }
                """;
        mockMvc.perform(put("/films").contentType(MediaType.APPLICATION_JSON).content(updatedInvalidTestFilm)).andExpect(status().isNotFound());
    }

    @Test
    void updateInvalidCredentials() throws Exception {
        createValidFilm(validTestFilm);
        String invalidNameJson = """
                {
                  "id": "1",
                  "name": "",
                  "description": "wow very nice",
                  "releaseDate": "1987-01-01",
                  "duration": 15000
                }
                """;
        mockMvc.perform(put("/films").contentType(MediaType.APPLICATION_JSON).content(invalidNameJson)).andExpect(status().isBadRequest());

        String invalidDescriptionJson = """
                {
                    "id": "1",
                  "name": "Ironweed",
                  "description": "wow very nice111111111111111111111111111111111111111111111111111111111111111111111111111
                  11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111",
                  "releaseDate": "1987-01-01",
                  "duration": 15000
                }
                """;
        mockMvc.perform(put("/films").contentType(MediaType.APPLICATION_JSON).content(invalidDescriptionJson)).andExpect(status().isBadRequest());

        String invalidReleaseJson = """
                {
                    "id": "1",
                  "name": "Ironweed",
                  "description": "wow very nice",
                  "releaseDate": "1227-01-01",
                  "duration": 15000
                }
                """;
        mockMvc.perform(put("/films").contentType(MediaType.APPLICATION_JSON).content(invalidReleaseJson)).andExpect(status().isBadRequest());

        String invalidDurationJson = """
                {
                    "id": "1",
                  "name": "",
                  "description": "wow very nice",
                  "releaseDate": "1227-01-01",
                  "duration": 0
                }
                """;
        mockMvc.perform(put("/films").contentType(MediaType.APPLICATION_JSON).content(invalidDurationJson)).andExpect(status().isBadRequest());

        String invalidDuration2Json = """
                {
                    "id": "1",
                  "name": "",
                  "description": "wow very nice",
                  "releaseDate": "1227-01-01",
                  "duration": -10
                }
                """;
        mockMvc.perform(put("/films").contentType(MediaType.APPLICATION_JSON).content(invalidDuration2Json)).andExpect(status().isBadRequest());

        String invalidEmptyJson = """
                {
                "id": "1"
                
                }
                """;
        mockMvc.perform(put("/films").contentType(MediaType.APPLICATION_JSON).content(invalidEmptyJson)).andExpect(status().isBadRequest());

        String nameAsNumberDurationAsString = """
                {
                    "id": "1",
                  "name": 0,
                  "description": "wow very nice",
                  "releaseDate": "1227-01-01",
                  "duration": "ss"
                }
                """;
        mockMvc.perform(put("/films").contentType(MediaType.APPLICATION_JSON).content(nameAsNumberDurationAsString)).andExpect(status().isBadRequest());

    }

    @Test
    void notAllFieldsShouldBeInUpdateSuccess() throws Exception {
        createValidFilm(validTestFilm);
        String updatedValidTestFilm = """
                {
                  "id": 1,
                  "name": "Ironweed2"
                }
                """;

        mockMvc.perform(put("/films").contentType(MediaType.APPLICATION_JSON).content(updatedValidTestFilm)).andExpect(status().isOk());
        mockMvc.perform(get("/films"))
                .andExpect(jsonPath("$[0].name").value("Ironweed2"))
                .andExpect(jsonPath("$[0].duration").value(15000));

    }
}
