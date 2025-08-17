package nl.andarabski.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.andarabski.converter.UserDtoToUserConverter;
import nl.andarabski.converter.UserToUserDtoConverter;
import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.dto.UserDto;
import nl.andarabski.model.User;
import nl.andarabski.service.UserService;
import nl.andarabski.system.StatusCode;
import nl.andarabski.system.exception.ObjectNotFoundException;
import nl.andarabski.util.FileUploadUtil;
import nl.andarabski.utils.StubDataDtos;
import nl.andarabski.utils.StubDataEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.endpoint.base-url=/api/v1")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDtoToUserConverter userDtoToUserConverter;

    @MockitoBean
    private UserToUserDtoConverter userToUserDtoConverter;
    @Autowired
    ObjectMapper objectMapper;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @MockitoBean
    List<UserDto> users;
    @MockitoBean
    private StubDataDtos stubDataDtos;

    // Testdata als DTO’s (niet als entiteiten!)
    private UserDto user1, user2, user3;

    private final static String OBJECT_USER = "User";


    @BeforeEach
    void setUp() {

        users = new ArrayList<>();
        stubDataDtos = new StubDataDtos();

        user1 = new UserDto();
        user1.setFirstName("Andre");
        user1.setLastName("Dabski");
        user1.setEmail("test@gmail.com");
        user1.setPassword("12345");
        user1.setAge(25);
        user1.setPhoto("src/main/resources/mock/leeuw.jpeg");
        user1.setCv("src/main/resources/mock/Cv_One.pdf");
        user1.setRole("admin");
        user1.setEnabled(true);
        user1.setApplications(stubDataDtos.getListApplications());

        user2 = new UserDto();
        user2.setFirstName("John");
        user2.setLastName("Johnson");
        user2.setEmail("test@gmail.com");
        user2.setPassword("54321");
        user2.setAge(25);
        user2.setPhoto("src/main/resources/mock/natuur.jpeg");
        user2.setCv("src/main/resources/mock/Cv_Two.pdf");
        user2.setRole("admin user");
        user2.setEnabled(true);
        user2.setApplications(stubDataDtos.getListApplications());

        user3 = new UserDto();
        user3.setFirstName("Sonny");
        user3.setLastName("Andarabski");
        user3.setEmail("test@gmail.com");
        user3.setPassword("12345");
        user3.setAge(21);
        user3.setPhoto("src/main/resources/mock/vlinder.jpeg");
        user3.setCv("src/main/resources/mock/Cv_Three.pdf");
        user3.setRole("user");
        user3.setEnabled(true);
        user3.setApplications(stubDataDtos.getListApplications());

        this.users.add(user1);
        this.users.add(user2);
        this.users.add(user3);
    }

    @Test
    void findUserByIdSuccess() throws Exception {
        // Given
        given(this.userService.findById(eq(1L))).willReturn(users.get(0));

        // When and when
        this.mockMvc.perform(get(baseUrl + "/users/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.firstName").value("Andre"))
                .andExpect(jsonPath("$.data.lastName").value("Dabski"))
                .andExpect(jsonPath("$.data.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.data.password").value("12345"))
                .andExpect(jsonPath("$.data.age").value(25))
                .andExpect(jsonPath("$.data.photo").value("src/main/resources/mock/leeuw.jpeg"))
                .andExpect(jsonPath("$.data.cv").value("src/main/resources/mock/Cv_One.pdf"))
                .andExpect(jsonPath("$.data.role").value("admin"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.applications[0].status").value("APPLIED"));
                //.andExpect(jsonPath("$.data.applications").value(2));

    }

    @Test
    void findUserByIdNotFound() throws Exception {
        // GIVEN
        given(this.userService.findById(eq(1L))).willThrow(new ObjectNotFoundException(OBJECT_USER, 1L ));

        // WHEN AND THEN
        this.mockMvc.perform(get(baseUrl + "/users/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find " + OBJECT_USER + " with Id: 1 :(" ))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void findAllUsers() throws Exception {
        // GIVEN
        given(this.userService.findAll()).willReturn(users);

        // WHEN AND THEN
        this.mockMvc.perform(get(baseUrl + "/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data[0].firstName").value("Andre"))
                .andExpect(jsonPath("$.data[0].lastName").value("Dabski"))
                .andExpect(jsonPath("$.data[0].email").value("test@gmail.com"))
                .andExpect(jsonPath("$.data[0].password").value("12345"))
                .andExpect(jsonPath("$.data[0].age").value(25))
                .andExpect(jsonPath("$.data[0].photo").value("src/main/resources/mock/leeuw.jpeg"))
                .andExpect(jsonPath("$.data[0].cv").value("src/main/resources/mock/Cv_One.pdf"))
                .andExpect(jsonPath("$.data[0].role").value("admin"))
                .andExpect(jsonPath("$.data[0].enabled").value(true))
                .andExpect(jsonPath("$.data[0].applications[0].status").value("APPLIED"))
                .andExpect(jsonPath("$.data[1].firstName").value("John"))
                .andExpect(jsonPath("$.data[1].lastName").value("Johnson"))
                .andExpect(jsonPath("$.data[1].email").value("test@gmail.com"))
                .andExpect(jsonPath("$.data[1].password").value("54321"))
                .andExpect(jsonPath("$.data[1].age").value(25))
                .andExpect(jsonPath("$.data[1].photo").value("src/main/resources/mock/natuur.jpeg"))
                .andExpect(jsonPath("$.data[1].cv").value("src/main/resources/mock/Cv_Two.pdf"))
                .andExpect(jsonPath("$.data[1].role").value("admin user"))
                .andExpect(jsonPath("$.data[1].enabled").value(true))
                .andExpect(jsonPath("$.data[1].applications[1].status").value("PENDING"));
    }

    @Test
    void createUser() throws Exception {
        // 1) Applications JSON (zoals controller verwacht)
        var apps = java.util.List.of(
                new ApplicationDto() {{ setId(1L); setStatus("APPLIED"); setMotivation("ok"); }},
                new ApplicationDto() {{ setId(2L); setStatus("PENDING"); setMotivation("ok"); }}
        );
        String appsJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(apps);

        // 2) Files
        var photo = new org.springframework.mock.web.MockMultipartFile(
                "photo", "p.jpg", "image/jpeg", "img".getBytes());
        var cv = new org.springframework.mock.web.MockMultipartFile(
                "cv", "c.pdf", "application/pdf", "pdf".getBytes());

        // 3) Converters + service stubs
        // Controller: UserDto → User (voor save)
        var entityToSave = new User();
        when(userDtoToUserConverter.convert(any(UserDto.class)))
                .thenReturn(entityToSave);

        // Service: save(User) → User (saved)
        var savedEntity = new User();
        savedEntity.setId(4L);
        when(userService.save(any(User.class)))
                .thenReturn(savedEntity);

        // Controller: User → UserDto (response)
        var responseDto = new UserDto();
        responseDto.setId(4L);
        responseDto.setFirstName("Sanny");
        responseDto.setLastName("Andarabi");
        responseDto.setEmail("sanny.and@gmail.com");
        responseDto.setAge(25);
        responseDto.setRole("admin");
        responseDto.setEnabled(true);
        responseDto.setApplications(apps);
        when(userToUserDtoConverter.convert(savedEntity))
                .thenReturn(responseDto);

        // 4) Static utilities mocken zodat er geen echte disk-I/O is
        try (var mocked = org.mockito.Mockito.mockStatic(FileUploadUtil.class)) {
            mocked.when(() -> FileUploadUtil.savePhoto(any())).thenReturn("photo.png");
            mocked.when(() -> FileUploadUtil.saveDocument(any())).thenReturn("cv.pdf");

            mockMvc.perform(
                            multipart(baseUrl + "/users/add")
                                    .file(photo)
                                    .file(cv)
                                    .param("firstName", "Andre")
                                    .param("lastName", "Dabski")
                                    .param("email", "test@gmail.com")
                                    .param("password", "12345")
                                    .param("age", "25")
                                    .param("role", "admin")
                                    .param("enabled", "true")
                                    .param("applications", appsJson)
                                    // multipart() gebruikt standaard POST voor file upload, maar expliciet kan ook:
                                    .with(req -> { req.setMethod("POST"); return req; })
                                    .accept(MediaType.APPLICATION_JSON)
                    )

                    // HTTP status komt van @ResponseStatus(HttpStatus.CREATED)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.flag").value(true))
                    // Body code is 200 omdat jij dat zelf zo zet in new Result(true, 200, "Add Success", ...)
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Add Success"))
                    .andExpect(jsonPath("$.data.id").value(4L))
                    .andExpect(jsonPath("$.data.firstName").value("Sanny"))
                    .andExpect(jsonPath("$.data.applications.length()").value(apps.size()));

        }

    }


    @Test
    void createUserNotFound() throws Exception {
        // Deze test checkt een 400 door validatie (geen foto). “NotFound” past niet echt bij create;
        // maar zo vul je hem inhoudelijk met een nuttige negatieve case.
        var cv = new MockMultipartFile("cv", "c.pdf", "application/pdf", "pdf".getBytes());
      //  var photo = new MockMultipartFile("photo", "c.jpg", "application/jpg", "jpg".getBytes());
        String appsJson = new ObjectMapper().writeValueAsString(List.of(new ApplicationDto()));

        mockMvc.perform(
                        multipart(baseUrl + "/users/add")
                                .file(cv) // GEEN 'photo'
                              //  .file(photo)
                                .param("firstName", "Andre")
                                .param("lastName", "Dabski")
                                .param("email", "test@gmail.com")
                                .param("password", "12345")
                                .param("age", "25")
                                .param("role", "admin")
                                .param("enabled", "true")
                                .param("applications", appsJson)
                                .with(req -> { req.setMethod("POST"); return req; })
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find photo with Id: null :("));
    }

   @Test
    void updateUserSuccess() throws Exception {
        Long userId = 7L;

        var photo = new MockMultipartFile("photo", "p.jpg", "image/jpeg", "img".getBytes());
        var cv    = new MockMultipartFile("cv", "c.pdf", "application/pdf", "pdf".getBytes());
        String appsJson = new ObjectMapper().writeValueAsString(List.of(new ApplicationDto()));

        // Stubs converters + service
        UserDto incomingDto = new UserDto();
        incomingDto.setApplications(List.of(new ApplicationDto()));

        User toUpdateEntity = new User();
        User updatedEntity  = new User(); updatedEntity.setId(userId);
        UserDto responseDto = new UserDto(); responseDto.setId(userId); responseDto.setFirstName("Updated");

        // Controller converteert DTO->Entity vóór save/update:
        when(userDtoToUserConverter.convert(any(UserDto.class))).thenReturn(toUpdateEntity);
        // Service voert update uit (pas de signatuur aan naar jouw service!)
        when(userService.update(eq(userId), any(User.class))).thenReturn(updatedEntity);
        // Controller converteert Entity->DTO voor response:
        when(userToUserDtoConverter.convert(updatedEntity)).thenReturn(responseDto);

        try (var mocked = Mockito.mockStatic(FileUploadUtil.class)) {
            mocked.when(() -> FileUploadUtil.savePhoto(any())).thenReturn("photo.png");
            mocked.when(() -> FileUploadUtil.saveDocument(any())).thenReturn("cv.pdf");

            mockMvc.perform(
                            multipart(baseUrl + "/users/update/{id}", userId)
                                    .file(photo)
                                    .file(cv)
                                    .param("firstName", "Andre")
                                    .param("lastName", "Dabski")
                                    .param("email", "test@gmail.com")
                                    .param("password", "12345")
                                    .param("age", "25")
                                    .param("role", "admin")
                                    .param("enabled", "true")
                                    .param("applications", appsJson)
                                    .with(req -> { req.setMethod("PUT"); return req; }) // multipart defaults to POST
                                    .accept(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk()) // of isCreated() als jij 201 terugstuurt
                    .andExpect(jsonPath("$.flag").value(true))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsStringIgnoringCase("Update")))
                    .andExpect(jsonPath("$.data.id").value(userId))
                    .andExpect(jsonPath("$.data.firstName").value("Updated"));
        }
    }

    @Test
    void updateUserNotFound() throws Exception {
        Long userId = 999L;

        var photo = new MockMultipartFile("photo", "p.jpg", "image/jpeg", "img".getBytes());
        var cv = new MockMultipartFile("cv", "c.pdf", "application/pdf", "pdf".getBytes());
        String appsJson = "[]";

        when(userDtoToUserConverter.convert(any(UserDto.class))).thenReturn(new User());
        when(userService.update(eq(userId), any(User.class)))
                .thenThrow(new ObjectNotFoundException("User", userId));

        try (var mocked = Mockito.mockStatic(FileUploadUtil.class)) {
            mocked.when(() -> FileUploadUtil.savePhoto(any())).thenReturn("photo.png");
            mocked.when(() -> FileUploadUtil.saveDocument(any())).thenReturn("cv.pdf");

            mockMvc.perform(
                            multipart(baseUrl + "/users/update/{id}", userId)
                                    .file(photo).file(cv)
                                    .param("firstName", "Andre")
                                    .param("lastName", "Dabski")
                                    .param("email", "test@gmail.com")
                                    .param("password", "12345")
                                    .param("age", "25")
                                    .param("role", "admin")
                                    .param("enabled", "true")
                                    .param("applications", appsJson)
                                    .with(req -> {
                                        req.setMethod("PUT");
                                        return req;
                                    })
                                    .accept(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.flag").value(false))
                    .andExpect(jsonPath("$.message").value("Could not find " + OBJECT_USER + " with Id: 999 :(" ));
        }
    }

   @Test
    void deleteUserSuccess() throws Exception {
        Long userId = 4L;
        // Service doet niets (void). Eventueel doNothing().when(userService).deleteById(userId);
        mockMvc.perform(delete(baseUrl + "/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsStringIgnoringCase("Delete")));
    }

   @Test
    void deleteUserNotFound() throws Exception {
        Long userId = 404L;
        doThrow(new ObjectNotFoundException("User", userId)).when(userService).delete(userId);

        mockMvc.perform(delete(baseUrl + "/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Could not find " + OBJECT_USER + " with Id: 404 :(" ));
    }

}