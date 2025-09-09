package nl.andarabski.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.dto.UserDto;
import nl.andarabski.mapper.UserMapper;
import nl.andarabski.model.Application;
import nl.andarabski.model.ApplicationStatus;
import nl.andarabski.service.UserService;
import nl.andarabski.system.StatusCode;
import nl.andarabski.system.exception.ExceptionHandlerAdvice;
import nl.andarabski.system.exception.ObjectNotFoundException;
import nl.andarabski.testsupport.TD;
import nl.andarabski.testsupport.web.RestMatchers;
import nl.andarabski.utils.StubDataDtos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static nl.andarabski.testsupport.TD.FIXED_DATE;
import static nl.andarabski.testsupport.TD.applicationDto;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;


import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
//@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ExceptionHandlerAdvice.class)
//@TestPropertySource(properties = "api.endpoint.base-url=/api/v1")
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;
    @MockitoBean UserMapper userMapper;


    @Value("${api.endpoint.base-url}")
    String baseUrl;
    private static final String USER = "User";
    String expected = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
            FIXED_DATE.toInstant().atOffset(ZoneOffset.UTC)   // of je eigen zone
    );


    @Test
    void findUserByIdSuccess() throws Exception {
        // Given
       var dto = new UserDto();
       dto.setId(1L);
       dto.setFirstName("Andre");
       dto.setLastName("Dabski");
       dto.setApplications(List.of(
                applicationDto(10L, 1L, 3L, "APPLIED", "ok"),
                applicationDto(11L, 1L, 3L, "PENDING", "ok2")
        ));

        given(userService.findById(1L)).willReturn(dto);

        // When and when
        this.mockMvc.perform(get(baseUrl + "/users/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.firstName").value("Andre"))
                .andExpect(jsonPath("$.data.applications[0].status").value("APPLIED"));
        verify(userService).findById(1L);
    }

    @Test
    void findUserByIdNotFoundMapsTo404() throws Exception {
        // GIVEN
        when(userService.findById(404L))
                .thenThrow(new ObjectNotFoundException("User", 404L));

        // WHEN AND THEN
        this.mockMvc.perform(get(baseUrl + "/users/404").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find " + USER + " with Id: 404 :(" ))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
    }


    @Test
    void findAllUsers() throws Exception {
        // GIVEN
        var a = new UserDto(); a.setId(1L); a.setFirstName("Andarabi"); a.setPhoto("leeuw.jpeg"); a.setCv("Cv_One.pdf"); a.setApplications(List.of(
                applicationDto(10L, 1L, 3L, "APPLIED", "ok"),
                applicationDto(11L, 1L, 3L, "PENDING", "ok2")
        ));
        var b = new UserDto(); b.setId(2L); b.setFirstName("Sanny"); b.setPhoto("vlinder.jpeg"); b.setCv("Cv_Two.pdf"); b.setApplications(List.of(
                applicationDto(10L, 1L, 3L, "APPLIED", "ok"),
                applicationDto(11L, 1L, 3L, "PENDING", "ok2")
        ));
        when(this.userService.findAll()).thenReturn(List.of(a, b));

        // WHEN AND THEN
        this.mockMvc.perform(get(baseUrl + "/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data[0].firstName").value("Andarabi"))
                .andExpect(jsonPath("$.data[0].photo").value("leeuw.jpeg"))
                .andExpect(jsonPath("$.data[0].cv").value("Cv_One.pdf"))
                .andExpect(jsonPath("$.data[0].applications[0].status").value("APPLIED"))

                .andExpect(jsonPath("$.data[1].firstName").value("Sanny"))
                .andExpect(jsonPath("$.data[1].photo").value("vlinder.jpeg"))
                .andExpect(jsonPath("$.data[1].cv").value("Cv_Two.pdf"))
                .andExpect(jsonPath("$.data[1].applications[1].status").value("PENDING"));
        verify(this.userService).findAll();
    }


    @Test
    void createUser_withPhotoAndCv_201() throws Exception {
        // files
        var photo = new MockMultipartFile("photo","avatar.jpg","image/jpeg","img".getBytes());
        var cv    = new MockMultipartFile("cv","resume.pdf","application/pdf","pdf".getBytes());

        String applicationsJson = "[]";

        var saved = new UserDto();
        saved.setId(1L);
        saved.setFirstName("Andarabi");
        saved.setPhoto("avatar.jpg");
        saved.setCv("resume.pdf");
        saved.setApplications(Collections.emptyList());

        when(userService.create(any(UserDto.class), any(MultipartFile.class), any(MultipartFile.class))).thenReturn(saved);

        // applications als JSON string (de controller leest dit als String en parset zelf)
        var apps = List.of(
                new Application() {{ setId(1L); setStatus(ApplicationStatus.APPLIED); setMotivation("ok"); }},
                new Application() {{ setId(2L); setStatus(ApplicationStatus.PENDING); setMotivation("ok2"); }}
        );

            // --- Act + Assert ---
        mockMvc.perform(multipart(baseUrl + "/users/add")
                            .file(photo)
                            .file(cv)
                            .param("firstName", "Andarabi")
                            .param("lastName", "Sanny")
                            .param("email", "s.andarabi@gmail.com")
                            .param("password", "secret")
                            .param("age", "15")
                            .param("role", "admin")
                            .param("enabled", "true")
                            .param("applications", applicationsJson)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.flag").value(true))
                    // jouw Result gebruikt StatusCode.SUCCESS in de body, ondanks HTTP 201
                    .andExpect(jsonPath("$.code").value(StatusCode.CREATED))
                    .andExpect(jsonPath("$.message").value("User created successfully"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.firstName").value("Andarabi"))
                    // veldnamen komen uit jouw UserDto (controller zet dto.setPhoto/dto.setCv)
                    .andExpect(jsonPath("$.data.photo").value("avatar.jpg"))
                    .andExpect(jsonPath("$.data.cv").value("resume.pdf"))
                    .andExpect(jsonPath("$.data.applications.length()").value(0));

            verify(userService).create(any(UserDto.class), any(MultipartFile.class), any(MultipartFile.class));
        }


    @Test
    void createUser_missingPhoto_404() throws Exception {
        var cv = new MockMultipartFile("cv","resume.pdf","application/pdf","pdf".getBytes());

        mockMvc.perform(multipart(baseUrl + "/users/add")
                       // .file(photo)
                        .file(cv) // GEEN photo
                        .param("firstName","Andre")
                        .param("lastName","Dabski")
                        .param("email","a@b.com")
                        .param("password","secret")
                        .param("age","30")
                        .param("role","admin")
                        .param("enabled","true")
                        .param("applications","[]")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Photo is required"))
                .andExpect(jsonPath("$.data").isEmpty());
    }


   @Test
    void updateUserSuccess() throws Exception {
        Long userId = 2L, vacancyId = 3L;

        var photo = new MockMultipartFile("photo", "p.jpg", "image/jpeg", "img".getBytes());
        var cv    = new MockMultipartFile("cv", "c.pdf", "application/pdf", "pdf".getBytes());
        String appsJson = new ObjectMapper().writeValueAsString(List.of(new ApplicationDto()));


        // Stubs converters + service
       var in = TD.applicationDto(0L, 1L, 3L, "PENDING", "Motiv");
       var out = TD.applicationDto(42L, 1L, 3L, "PENDING", "Motiv");

        var saved = new UserDto();
        saved.setId(2L);
        saved.setFirstName("Sanny");
        saved.setLastName("Andarabi");
        saved.setEmail("s.andarabi@gmail.com");
        saved.setPassword("12345");
        saved.setAge(25);
        saved.setRole("admin");
        saved.setEnabled(true);
        saved.setPhoto("photo.jpg");
        saved.setCv("resume.pdf");
        saved.setApplications(List.of(in, out));

       when(userService.update(eq(userId), any(UserDto.class), any(MultipartFile.class), any(MultipartFile.class))).thenReturn(saved);

            mockMvc.perform(
                            multipart(baseUrl + "/users/update/{id}", userId)
                                    .file(photo)
                                    .file(cv)
                                    .param("firstName", "Andere")
                                    .param("lastName", "Kings")
                                    .param("email", "s.andarabi@gmail.com")
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
                    .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                    .andExpect(jsonPath("$.message").value("Update success"))
                    .andExpect(jsonPath("$.data.id").value(2L))
                    .andExpect(jsonPath("$.data.firstName").value("Sanny"))
                    .andExpect(jsonPath("$.data.lastName").value("Andarabi"))
                    .andExpect(jsonPath("$.data.email").value("s.andarabi@gmail.com"))
                    .andExpect(jsonPath("$.data.password").value("12345"))
                    .andExpect(jsonPath("$.data.age").value(25))
                    .andExpect(jsonPath("$.data.role").value("admin"))
                    .andExpect(jsonPath("$.data.enabled").value(true))
                    .andExpect(jsonPath("$.data.applications.length()").value(2));
            verify(userService).update(eq(userId), any(UserDto.class), any(MultipartFile.class), any(MultipartFile.class));
        }


    @Test
    void updateUserNotFound() throws Exception {
        Long userId = 999L;

        var photo = new MockMultipartFile("photo", "p.jpg", "image/jpeg", "img".getBytes());
        var cv = new MockMultipartFile("cv", "c.pdf", "application/pdf", "pdf".getBytes());
        String appsJson = "[]";

        // wél de not-found simuleren:
        when(userService.update(eq(userId), any(UserDto.class), any(MultipartFile.class), any(MultipartFile.class)))
                .thenThrow(new ObjectNotFoundException("User", userId));

        mockMvc.perform(
                        multipart(baseUrl + "/users/update/{id}", userId)
                                .file(photo).file(cv)
                                .param("firstName", "Sanny")
                                .param("lastName", "Andarabi")
                                .param("email", "s.andarabi@gmail.com")
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
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find " + USER + " with Id: 999 :("))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
    }


   @Test
    void deleteUserSuccess() throws Exception {
        Long userId = 4L;
        // Service doet niets (void). Eventueel doNothing().when(userService).deleteById(userId);
        mockMvc.perform(delete(baseUrl + "/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete success"));
        verify(userService).delete(userId);
    }


   @Test
    void deleteUserNotFound() throws Exception {
        Long userId = 404L;
        doThrow(new ObjectNotFoundException("User", userId)).when(userService).delete(userId);

        mockMvc.perform(delete(baseUrl + "/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find " + USER + " with Id: 404 :(" ))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        verify(userService).delete(userId);
    }

}