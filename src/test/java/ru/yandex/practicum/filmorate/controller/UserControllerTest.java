package ru.yandex.practicum.filmorate.controller;

// CHECKSTYLE:OFF
//@SpringBootTest
//@AutoConfigureMockMvc
//class UserControllerTest {
//    private final String validTestUser = """
//            {
//              "login": "properlogin",
//              "email": "mail@mail.ru",
//              "name": "",
//              "birthday": "1990-01-01"
//            }
//            """;
//
//    private void createValidUser(String userJson) throws Exception {
//        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userJson)).andExpect(status().isCreated());
//    }
//
//    private void createInvalidUserWithBadRequest(String userJson) throws Exception {
//        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userJson)).andExpect(status().isBadRequest());
//    }
//
//    private void createInvalidUserWithInternalServerError(String userJson) throws Exception {
//        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userJson)).andExpect(status().isInternalServerError());
//    }
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private UserService userService;
//
//
//    @BeforeEach
//    public void clearData() throws Exception {
//        mockMvc.perform(delete("/users/reset")).andExpect(status().isOk());
//        userService.clearCredentialsSets();
//
//    }
//
//
//    @Test
//    void shouldReturnBadRequestWhenLoginInvalid() throws Exception {
//        String invalidUserJson = """
//                {
//                  "login": "невалидный логин!",
//                  "email": "mail@mail.ru",
//                  "name": "",
//                  "birthday": "1990-01-01"
//                }
//                """;
//
//        createInvalidUserWithBadRequest(invalidUserJson);
//
//        String invalidUserJson2 = """
//                {
//                  "login": "",
//                  "email": "mail@mail.ru",
//                  "name": "",
//                  "birthday": "1990-01-01"
//                }
//                """;
//
//        createInvalidUserWithBadRequest(invalidUserJson2);
//
//    }
//
//
//    @Test
//    void shouldReturnAllUsersAndPasteNameAsLogin() throws Exception {
//        createValidUser(validTestUser);
//        String validTestUser2 = """
//                {
//                  "login": "properlogin2",
//                  "email": "mail2@mail.ru",
//                  "name": "oleg",
//                  "birthday": "1990-01-01"
//                }
//                """;
//        createValidUser(validTestUser2);
//        mockMvc.perform(get("/users")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$").isArray()) // проверка, что вернулся JSON-массив
//                .andExpect(jsonPath("$").isNotEmpty()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].login").value("properlogin")).andExpect(jsonPath("$[0].email").value("mail@mail.ru")).andExpect(jsonPath("$[0].name").value("properlogin")).andExpect(jsonPath("$[0].birthday").value("1990-01-01")).andExpect(jsonPath("$[1].name").value("oleg")).andDo(result -> {
//                    System.out.println("Response JSON: " + result.getResponse().getContentAsString());
//                });
//    }
//
//    @Test
//    void duplicateCredentialsCheck() throws Exception {
//        createValidUser(validTestUser);
//        String duplicateUserJson = """
//                {
//                  "login": "properlogin",
//                  "email": "mail@mail.ru",
//                  "name": "oleg",
//                  "birthday": "1999-01-01"
//                }
//                """;
//        createInvalidUserWithBadRequest(duplicateUserJson);
//    }
//
//    @Test
//    void shouldReturnBadRequestWhenEmailInvalid() throws Exception {
//        String wrongEmailUser = """
//                {
//                  "login": "properlogin",
//                  "email": "notproperemail.ru",
//                  "name": "oleg",
//                  "birthday": "1999-01-01"
//                }
//                """;
//        createInvalidUserWithBadRequest(wrongEmailUser);
//        String wrongEmailUser2 = """
//                {
//                  "login": "properlogin",
//                  "email": "notproperemail@.ru",
//                  "name": "oleg",
//                  "birthday": "1999-01-01"
//                }
//                """;
//        createInvalidUserWithBadRequest(wrongEmailUser2);
//    }
//
//    @Test
//    void shouldReturnBadRequestWhenBirthdayIsInFuture() throws Exception {
//        String wrongBirthDateUser = """
//                {
//                  "login": "properlogin",
//                  "email": "properemail@mail.ru",
//                  "name": "oleg",
//                  "birthday": "2199-01-01"
//                }
//                """;
//        createInvalidUserWithBadRequest(wrongBirthDateUser);
//    }
//
//    @Test
//    void postInvalidRequests() throws Exception {
//        String invalidRaquest1 = "";
//        createInvalidUserWithInternalServerError(invalidRaquest1);
//    }
//
//    @Test
//    void updateSuccessAndUpdateLoginEmailSuccess() throws Exception {
//        createValidUser(validTestUser);
//        String updatedValidTestUser = """
//                {
//                  "id": 1,
//                  "login": "properlogin",
//                  "email": "mail@mail.ru",
//                  "name": "UpdatedName",
//                  "birthday": "1999-01-01"
//                }
//                """;
//
//        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedValidTestUser)).andExpect(status().isOk());
//        mockMvc.perform(get("/users")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].login").value("properlogin")).andExpect(jsonPath("$[0].email").value("mail@mail.ru")).andExpect(jsonPath("$[0].name").value("UpdatedName")).andExpect(jsonPath("$[0].birthday").value("1999-01-01"));
//        String updatedValidTestUser2 = """
//                {
//                  "id": 1,
//                  "login": "properlogin2",
//                  "email": "mail2@mail.ru",
//                  "name": "",
//                  "birthday": "1990-02-02"
//                }
//                """;
//        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedValidTestUser2)).andExpect(status().isOk());
//        mockMvc.perform(get("/users")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].login").value("properlogin2")).andExpect(jsonPath("$[0].email").value("mail2@mail.ru")).andExpect(jsonPath("$[0].name").value("properlogin2")).andExpect(jsonPath("$[0].birthday").value("1990-02-02"));
//    }
//
//    @Test
//    void idMissingBadRequest() throws Exception {
//        createValidUser(validTestUser);
//        String updatedValidTestUser = """
//                {
//                  "login": "properlogin2",
//                  "email": "mail@mail.ru",
//                  "name": "UpdatedName",
//                  "birthday": "1999-01-01"
//                }
//                """;
//        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedValidTestUser)).andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void userWithThatIdMissingNotFound() throws Exception {
//        createValidUser(validTestUser);
//        String updatedInvalidTestUser = """
//                {
//                    "id": "3",
//                  "login": "properlogin2",
//                  "email": "mail@mail.ru",
//                  "name": "UpdatedName",
//                  "birthday": "1999-01-01"
//                }
//                """;
//        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedInvalidTestUser)).andExpect(status().isNotFound());
//    }
//
//    @Test
//    void updateInvalidCredentials() throws Exception {
//        createValidUser(validTestUser);
//        String updatedInvalidLogin = """
//                {
//                    "id": "1",
//                  "login": "proper   login2",
//                  "email": "mail@mail.ru",
//                  "name": "UpdatedName",
//                  "birthday": "1999-01-01"
//                }
//                """;
//        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedInvalidLogin)).andExpect(status().isBadRequest());
//        String updatedInvalidEmail = """
//                {
//                    "id": "1",
//                  "login": "properlogin2",
//                  "email": "mailmail.ru",
//                  "name": "UpdatedName",
//                  "birthday": "1999-01-01"
//                }
//                """;
//        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedInvalidEmail)).andExpect(status().isBadRequest());
//        String updatedInvalidData = """
//                {
//                    "id": "1",
//                  "login": "properlogin2",
//                  "email": "mail@mail.ru",
//                  "name": "UpdatedName",
//                  "birthday": "2199-01-01"
//                }
//                """;
//        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedInvalidData)).andExpect(status().isBadRequest());
//
//    }
//
//    @Test
//    void notAllFieldsShouldBeInUpdateSuccess() throws Exception {
//        createValidUser(validTestUser);
//        String updatedValidTestUser = """
//                {
//                  "id": 1,
//                  "login": "properlogin2",
//                  "email": "mail@mail.ru",
//                  "name": "UpdatedName"
//                }
//                """;
//
//        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedValidTestUser)).andDo(print()).andExpect(status().isOk());
//        mockMvc.perform(get("/users")).andExpect(jsonPath("$[0].name").value("UpdatedName")).andExpect(jsonPath("$[0].birthday").value("1990-01-01"));
//
//    }
//
//    @Test
//    void duplicateWhenUpdate() throws Exception {
//        createValidUser(validTestUser);
//        String secondValidUser = """
//                {
//                  "login": "properlogin2",
//                  "email": "mail2@mail.ru",
//                  "name": "",
//                  "birthday": "1995-01-01"
//                }
//                """;
//        createValidUser(secondValidUser);
//        String updatedFirstUser1 = """
//                {
//                  "id": "1"
//                  "login": "properlogin2",
//                  "email": "mail@mail.ru",
//                  "name": "",
//                  "birthday": "1995-01-01"
//                }
//                """;
//        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedFirstUser1)).andExpect(status().isInternalServerError());
//        String updatedFirstUser2 = """
//                {
//                  "id": "1"
//                  "login": "properlogin",
//                  "email": "mail2@mail.ru",
//                  "name": "",
//                  "birthday": "1995-01-01"
//                }
//                """;
//        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(updatedFirstUser2)).andExpect(status().isInternalServerError());
//    }
//
//}

// CHECKSTYLE:ON
