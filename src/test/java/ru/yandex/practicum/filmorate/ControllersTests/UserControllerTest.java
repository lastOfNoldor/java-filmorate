package ru.yandex.practicum.filmorate.ControllersTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.UserController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
class UserControllerTest {
    private final String validTestUser = """
            {
              "login": "properlogin",
              "email": "mail@mail.ru",
              "name": "",
              "birthday": "1990-01-01"
            }
            """;

    private void createValidUser(String userJson) throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userJson)).andExpect(status().isCreated());
    }

    private void createInvalidUser(String userJson) throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userJson)).andExpect(status().isBadRequest());
    }

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void clearData() throws Exception {
        mockMvc.perform(delete("/users/reset")).andExpect(status().isOk());
    }


    @Test
    void shouldReturnBadRequestWhenLoginInvalid() throws Exception {
        String invalidUserJson = """
                {
                  "login": "невалидный логин!",
                  "email": "mail@mail.ru",
                  "name": "",
                  "birthday": "1990-01-01"
                }
                """;

        createInvalidUser(invalidUserJson);

        String invalidUserJson2 = """
                {
                  "login": "",
                  "email": "mail@mail.ru",
                  "name": "",
                  "birthday": "1990-01-01"
                }
                """;

        createInvalidUser(invalidUserJson2);

    }


    @Test
    void shouldReturnAllUsersAndPasteNameAsLogin() throws Exception {
        createValidUser(validTestUser);
        String validTestUser2 = """
                {
                  "login": "properlogin2",
                  "email": "mail2@mail.ru",
                  "name": "oleg",
                  "birthday": "1990-01-01"
                }
                """;
        createValidUser(validTestUser2);
        mockMvc.perform(get("/users")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$").isArray()) // проверка, что вернулся JSON-массив
                .andExpect(jsonPath("$").isNotEmpty()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].login").value("properlogin")).andExpect(jsonPath("$[0].email").value("mail@mail.ru")).andExpect(jsonPath("$[0].name").value("properlogin")).andExpect(jsonPath("$[0].birthday").value("1990-01-01")).andExpect(jsonPath("$[1].name").value("oleg")).andDo(result -> {
                    System.out.println("Response JSON: " + result.getResponse().getContentAsString());
                });
    }

    @Test
    void duplicateCredentialsCheck() throws Exception {
        createValidUser(validTestUser);
        String duplicateUserJson = """
                {
                  "login": "properlogin",
                  "email": "mail@mail.ru",
                  "name": "oleg",
                  "birthday": "1999-01-01"
                }
                """;
        createInvalidUser(duplicateUserJson);
    }

    @Test
    void shouldReturnBadRequestWhenEmailInvalid() throws Exception {
        String wrongEmailUser = """
                {
                  "login": "properlogin",
                  "email": "notproperemail.ru",
                  "name": "oleg",
                  "birthday": "1999-01-01"
                }
                """;
        createInvalidUser(wrongEmailUser);
        String wrongEmailUser2 = """
                {
                  "login": "properlogin",
                  "email": "notproperemail@.ru",
                  "name": "oleg",
                  "birthday": "1999-01-01"
                }
                """;
        createInvalidUser(wrongEmailUser2);
    }

    @Test
    void shouldReturnBadRequestWhenBirthdayIsInFuture() throws Exception {
        String wrongBirthDateUser = """
                {
                  "login": "properlogin",
                  "email": "properemail@mail.ru",
                  "name": "oleg",
                  "birthday": "2199-01-01"
                }
                """;
        createInvalidUser(wrongBirthDateUser);
    }

    /**
     * апдейт без айди         ++++
     * апдейт пользователя которого нет +++
     * апдейт с невалидными значенииями +++
     * апдейт с отсутвием не основных полей +++
     * полностью успешный апдейт            ++
     * успешная смена лдогина и почты       ++
     * дупликат при смене
     * полностью пустой стринг              ++
     */

    @Test
    void postInvalidRequests() throws Exception {
        String invalidRaquest1 = "";
        createInvalidUser(invalidRaquest1);
    }

    @Test
    void updateSuccessAndUpdateLoginEmailSuccess() throws Exception {
        createValidUser(validTestUser);
        String updatedValidTestUser = """
                {
                  "id": 1,
                  "login": "properlogin",
                  "email": "mail@mail.ru",
                  "name": "UpdatedName",
                  "birthday": "1999-01-01"
                }
                """;

        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedValidTestUser)).andExpect(status().isOk());
        mockMvc.perform(get("/users")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].login").value("properlogin")).andExpect(jsonPath("$[0].email").value("mail@mail.ru")).andExpect(jsonPath("$[0].name").value("UpdatedName")).andExpect(jsonPath("$[0].birthday").value("1999-01-01"));
        String updatedValidTestUser2 = """
                {
                  "id": 1,
                  "login": "properlogin2",
                  "email": "mail2@mail.ru",
                  "name": "",
                  "birthday": "1990-02-02"
                }
                """;
        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedValidTestUser2)).andExpect(status().isOk());
        mockMvc.perform(get("/users")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].login").value("properlogin2")).andExpect(jsonPath("$[0].email").value("mail2@mail.ru")).andExpect(jsonPath("$[0].name").value("properlogin2")).andExpect(jsonPath("$[0].birthday").value("1990-02-02"));
    }

    @Test
    void idMissingBadRequest() throws Exception {
        createValidUser(validTestUser);
        String updatedValidTestUser = """
                {
                  "login": "properlogin2",
                  "email": "mail@mail.ru",
                  "name": "UpdatedName",
                  "birthday": "1999-01-01"
                }
                """;
        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedValidTestUser)).andExpect(status().isBadRequest());
    }

    @Test
    void userWithThatIdMissingNotFound() throws Exception {
        createValidUser(validTestUser);
        String updatedInvalidTestUser = """
                {
                    "id": "3",
                  "login": "properlogin2",
                  "email": "mail@mail.ru",
                  "name": "UpdatedName",
                  "birthday": "1999-01-01"
                }
                """;
        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedInvalidTestUser)).andExpect(status().isNotFound());
    }

    @Test
    void updateInvalidCredentials() throws Exception {
        createValidUser(validTestUser);
        String updatedInvalidLogin = """
                {
                    "id": "1",
                  "login": "proper   login2",
                  "email": "mail@mail.ru",
                  "name": "UpdatedName",
                  "birthday": "1999-01-01"
                }
                """;
        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedInvalidLogin)).andExpect(status().isBadRequest());
        String updatedInvalidEmail = """
                {
                    "id": "1",
                  "login": "properlogin2",
                  "email": "mailmail.ru",
                  "name": "UpdatedName",
                  "birthday": "1999-01-01"
                }
                """;
        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedInvalidEmail)).andExpect(status().isBadRequest());
        String updatedInvalidData = """
                {
                    "id": "1",
                  "login": "properlogin2",
                  "email": "mail@mail.ru",
                  "name": "UpdatedName",
                  "birthday": "2199-01-01"
                }
                """;
        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedInvalidData)).andExpect(status().isBadRequest());

    }

    @Test
    void notAllFieldsShouldBeInUpdateSuccess() throws Exception {
        createValidUser(validTestUser);
        String updatedValidTestUser = """
                {
                  "id": 1,
                  "login": "properlogin2",
                  "email": "mail@mail.ru",
                  "name": "UpdatedName"
                }
                """;

        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedValidTestUser)).andExpect(status().isOk());
        mockMvc.perform(get("/users")).andExpect(jsonPath("$[0].name").value("UpdatedName")).andExpect(jsonPath("$[0].birthday").value("1990-01-01"));

    }

    @Test
    void duplicateWhenUpdate() throws Exception {
        createValidUser(validTestUser);
        String secondValidUser = """
                {
                  "login": "properlogin2",
                  "email": "mail2@mail.ru",
                  "name": "",
                  "birthday": "1995-01-01"
                }
                """;
        createValidUser(secondValidUser);
        String updatedFirstUser1 = """
                {
                  "id": "1"
                  "login": "properlogin2",
                  "email": "mail@mail.ru",
                  "name": "",
                  "birthday": "1995-01-01"
                }
                """;
        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedFirstUser1)).andExpect(status().isBadRequest());
        String updatedFirstUser2 = """
                {
                  "id": "1"
                  "login": "properlogin",
                  "email": "mail2@mail.ru",
                  "name": "",
                  "birthday": "1995-01-01"
                }
                """;
        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedFirstUser1)).andExpect(status().isBadRequest());
    }

}
