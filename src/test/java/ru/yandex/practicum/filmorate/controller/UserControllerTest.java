package ru.yandex.practicum.filmorate.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


// CHECKSTYLE:OFF
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    private final String validTestUser = """
            {
              "login": "properlogin",
              "email": "mail@mail.ru",
              "name": "",
              "birthday": "1990-01-01"
            }
            """;

    private long createValidUser(String userJson) throws Exception {
        MvcResult result = mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userJson)).andExpect(status().isCreated()).andReturn();

        String response = result.getResponse().getContentAsString();
        return JsonPath.parse(response).read("$.id", Long.class); // Получаем ID
    }

    private void createInvalidUserWithBadRequest(String userJson) throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userJson)).andExpect(status().isBadRequest());
    }

    private void createInvalidUserWithInternalServerError(String userJson) throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userJson)).andExpect(status().isInternalServerError());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;


    @BeforeEach
    public void clearData() throws Exception {
        mockMvc.perform(delete("/users/reset")).andExpect(status().isOk());
        userService.clearCredentialsSets();

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

        createInvalidUserWithBadRequest(invalidUserJson);

        String invalidUserJson2 = """
                {
                  "login": "",
                  "email": "mail@mail.ru",
                  "name": "",
                  "birthday": "1990-01-01"
                }
                """;

        createInvalidUserWithBadRequest(invalidUserJson2);

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
        createInvalidUserWithBadRequest(duplicateUserJson);
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
        createInvalidUserWithBadRequest(wrongEmailUser);
        String wrongEmailUser2 = """
                {
                  "login": "properlogin",
                  "email": "notproperemail@.ru",
                  "name": "oleg",
                  "birthday": "1999-01-01"
                }
                """;
        createInvalidUserWithBadRequest(wrongEmailUser2);
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
        createInvalidUserWithBadRequest(wrongBirthDateUser);
    }

    @Test
    void postInvalidRequests() throws Exception {
        String invalidRequest1 = "";
        createInvalidUserWithInternalServerError(invalidRequest1);
    }


    @Test
    void updateSuccessAndUpdateLoginEmailSuccess() throws Exception {
        long userId = createValidUser(validTestUser); // Получаем ID

        String updatedUserJson = """
                {
                  "id": %d,
                  "login": "properlogin",
                  "email": "mail@mail.ru",
                  "name": "UpdatedName",
                  "birthday": "1999-01-01"
                }
                """.formatted(userId);

        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedUserJson)).andExpect(status().isOk());
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
    void notAllFieldsShouldBeInUpdateSuccess() throws Exception {
        long userId = createValidUser(validTestUser);
        String updatedValidTestUser = """
                {
                  "id": %d,
                  "login": "properlogin2",
                  "email": "mail@mail.ru",
                  "name": "UpdatedName"
                }
                """.formatted(userId);

        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedValidTestUser)).andDo(print()).andExpect(status().isOk());
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
        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedFirstUser1)).andExpect(status().isInternalServerError());
        String updatedFirstUser2 = """
                {
                  "id": "1"
                  "login": "properlogin",
                  "email": "mail2@mail.ru",
                  "name": "",
                  "birthday": "1995-01-01"
                }
                """;
        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedFirstUser2)).andExpect(status().isInternalServerError());
    }

}

// CHECKSTYLE:ON
