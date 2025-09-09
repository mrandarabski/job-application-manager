package nl.andarabski.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.mapper.ApplicationMapper;
import nl.andarabski.model.Application;
import nl.andarabski.model.ApplicationStatus;
import nl.andarabski.model.User;
import nl.andarabski.model.Vacancy;
import nl.andarabski.service.ApplicationService;

import nl.andarabski.system.StatusCode;
import nl.andarabski.system.exception.ExceptionHandlerAdvice;
import nl.andarabski.system.exception.ObjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import static nl.andarabski.testsupport.TD.FIXED_DATE;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(ExceptionHandlerAdvice.class)
//@TestPropertySource(properties = "api.endpoint.base-url=/api/v1")
class ApplicationControllerTest {

@Autowired private MockMvc mockMvc;
@Autowired private ObjectMapper objectMapper;

@MockitoBean ApplicationService applicationService;
@MockitoBean ApplicationMapper applicationMapper;

    @Value("${api.endpoint.base-url}")
    String baseUrl;
    private static final String APPLICATION = "Application";

    String expected = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
            FIXED_DATE.toInstant().atOffset(ZoneOffset.UTC)   // of je eigen zone
    );

    // ======= helpers =======
    private ApplicationDto dto(long id, long userId, long vacancyId, String status, String motivation) {
        ApplicationDto d = new ApplicationDto();
        d.setId(id);
        d.setUserId(userId);
        d.setVacancyId(vacancyId);
        d.setStatus(status);
        d.setMotivation(motivation);
        d.setAppliedAt(new Date(1_700_000_000_000L));
        return d;
    }

    // ======= CRUD =======

    @Test
    void getAll_returnsOkWithList() throws Exception {
        var d1 = dto(10L, 1L, 3L, "APPLIED", "ok");
        var d2 = dto(11L, 1L, 3L, "PENDING", "ok2");
        given(applicationService.findAll()).willReturn(List.of(d1, d2));

        mockMvc.perform(get(baseUrl + "/applications").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value(10));

        verify(applicationService).findAll();
    }

    @Test
    void getById_returnsOk() throws Exception {
        var d1 = dto(10L, 1L, 3L, "APPLIED", "ok");
        given(applicationService.findById(10L)).willReturn(d1);

        mockMvc.perform(get( baseUrl + "/applications/10").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.id").value(10));

        verify(applicationService).findById(10L);
    }

    @Test
    void getById_returnsNotFound() throws Exception {
        var d1 = dto(404L, 1L, 3L, "APPLIED", "ok");
        given(applicationService.findById(404L))
                .willThrow(new ObjectNotFoundException(APPLICATION, 404L));
        mockMvc.perform(get( baseUrl + "/applications/404").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find " + APPLICATION + " with Id: 404 :(" ))
                .andExpect(jsonPath("$.data").isEmpty());
        verify(applicationService).findById(404L);

    }

    // .andExpect(jsonPath("$.message").value("Could not find " + APPLICATION + " with Id: 404 :(" ))
    @Test
    void create_returns201() throws Exception {
        var in = dto(0L, 1L, 3L, "PENDING", "Motiv");
        var out = dto(42L, 1L, 3L, "PENDING", "Motiv");
        given(applicationService.create(Mockito.any(ApplicationDto.class))).willReturn(out);

        mockMvc.perform(post(baseUrl  + "/applications/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.CREATED))
                .andExpect(jsonPath("$.message").value("Application created successfully"))
                .andExpect(jsonPath("$.data.id").value(42))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.vacancyId").value(3))
                .andExpect(jsonPath("$.data.motivation").value("Motiv"))
                .andExpect(jsonPath("$.data.appliedAt", matchesPattern("\\d{4}-\\d{2}-\\d{2}T.*[Z+\\-].*")));

        verify(applicationService).create(Mockito.any(ApplicationDto.class));
    }

    @Test
    void update_returns200() throws Exception {
        var patch = new ApplicationDto();
        patch.setMotivation("new");

        var out = dto(1L, 1L, 3L, "APPLIED", "new");

        given(applicationService.update(eq(1L), org.mockito.ArgumentMatchers.any(ApplicationDto.class)))
                .willReturn(out);

        mockMvc.perform(put(baseUrl + "/applications/1")       // <-- volledig pad
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Application updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.vacancyId").value(3))
                .andExpect(jsonPath("$.data.motivation").value("new"))
                .andExpect(jsonPath("$.data.appliedAt", matchesPattern("\\d{4}-\\d{2}-\\d{2}T.*[Z+\\-].*")));

        verify(applicationService).update(eq(1L), any(ApplicationDto.class));
    }

    @Test
    void updateFailed_returns400() throws Exception {
        var patch = new ApplicationDto();
        patch.setMotivation("new");
        given(applicationService.update(eq(1L), org.mockito.ArgumentMatchers.any(ApplicationDto.class)))
        .willThrow(new ObjectNotFoundException(APPLICATION, 404L));

        mockMvc.perform(put(baseUrl + "/applications/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find Application with Id: 404 :("))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(applicationService).update(eq(1L), any(ApplicationDto.class));

    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete(baseUrl + "/applications/1"))
                .andExpect(status().isOk())
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Delete Success"))
        .andExpect(jsonPath("$.data").isEmpty());
        
        verify(applicationService).delete(1L);
    }

    // ======= APPLY use-case =======

    @Test
    void apply_returns201_andMapsEntityToDto() throws Exception {
        // service geeft entity terug
        var user = new User(); user.setId(1L);
        var vacancy = new Vacancy(); vacancy.setId(3L);
        var app = new Application();
        app.setId(99L);
        app.setUser(user);
        app.setVacancy(vacancy);
        app.setStatus(ApplicationStatus.PENDING);
        app.setMotivation("Hire me");
        app.setAppliedAt(FIXED_DATE);
       // app.setAppliedAt(new Date(1_700_000_000_000L));
        given(applicationService.applyToVacancy(1L, 3L, "Hire me")).willReturn(app);

        // controller map’t via mapper naar DTO
        var dto = dto(99L, 1L, 3L, "PENDING", "Hire me");
        given(applicationMapper.toDto(app)).willReturn(dto);

        var body = objectMapper.writeValueAsString(new ApplyBody("Hire me"));

        mockMvc.perform(post(baseUrl + "/applications/users/1/vacancies/3/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.CREATED))
                .andExpect(jsonPath("$.message").value("Application submitted successfully"))
                .andExpect(jsonPath("$.data.id").value(99))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.vacancyId").value(3));

        verify(applicationService).applyToVacancy(1L, 3L, "Hire me");
        verify(applicationMapper).toDto(app);
    }

    @Test
    void apply_missingMotivation_returns400() throws Exception {
        var body = objectMapper.writeValueAsString(new ApplyBody("  "));

        mockMvc.perform(post(baseUrl + "/applications/users/1/vacancies/3/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Invalid motivation"))
                .andExpect(jsonPath("$.data").isEmpty());

    }

    @Test
    void apply_alreadyApplied_returns409() throws Exception {
        given(applicationService.applyToVacancy(1L, 3L, "again"))
                .willThrow(new IllegalArgumentException("User already applied"));

        var body = objectMapper.writeValueAsString(new ApplyBody("again"));

        mockMvc.perform(post(baseUrl + "/applications/users/1/vacancies/3/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.CONFLICT))
                .andExpect(jsonPath("$.message", containsString("applied")))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        given(applicationService.findById(999L)).willThrow(new ObjectNotFoundException("Application", 999L));

        mockMvc.perform(get(baseUrl + "/applications/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find Application with Id: 999 :("))
                .andExpect(jsonPath("$.data").isEmpty());

    }

    // helper body voor apply endpoint
    static class ApplyBody {
        public ApplyBody() {}
        public ApplyBody(String motivation) { this.motivation = motivation; }
        public String motivation;
        public String getMotivation() { return motivation; }
        public void setMotivation(String motivation) { this.motivation = motivation; }
    }
}


