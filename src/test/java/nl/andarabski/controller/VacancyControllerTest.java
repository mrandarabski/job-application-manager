package nl.andarabski.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.andarabski.dto.VacancyDto;
import nl.andarabski.mapper.VacancyMapper;
import nl.andarabski.model.ApplicationStatus;
import nl.andarabski.model.Vacancy;
import nl.andarabski.service.VacancyService;
import nl.andarabski.system.StatusCode;
import nl.andarabski.system.exception.ExceptionHandlerAdvice;
import nl.andarabski.system.exception.ObjectNotFoundException;
import nl.andarabski.testsupport.TD;
import nl.andarabski.testsupport.web.RestMatchers;
import nl.andarabski.utils.StubDataDtos;
import nl.andarabski.utils.StubDataEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static nl.andarabski.testsupport.TD.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*@WebMvcTest(
        controllers = VacancyController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "nl\\.andarabski\\.converter\\..*"
        )
)
@Import(ExceptionHandlerAdvice.class)
@TestPropertySource(properties = "api.endpoint.base-url=/api/v1")*/
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import({ExceptionHandlerAdvice.class})
class VacancyControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private VacancyMapper vacancyMapper;
    @MockitoBean private VacancyService vacancyService;
    @Autowired private ObjectMapper objectMapper;

    @Value("/api/v1")
    String baseUrl;
    private final static String OBJECT_VACANCY = "Vacancy";

    // Helpers

    private static Vacancy sampleEntity(Long id) {
        var v = new Vacancy();
        v.setId(id);
        v.setTitle("Java Developer");
        v.setCompanyName("Oracle");
        v.setDescription("We are looking for Java Developer in heart and soul");
        v.setLocation("Amsterdam");
        v.setPostedAt(FIXED_DATE);
        v.setApplications(List.of(
                application(10L, null, null, ApplicationStatus.APPLIED, "ok"),
                application(11L, null, null, ApplicationStatus.PENDING, "ok2")
        ));
        return v;
    }

    private static VacancyDto sampleDto(Long id) {
        var d = new VacancyDto();
        d.setId(id);
        d.setTitle("Java Developer");
        d.setCompanyName("Oracle");
        d.setDescription("We are looking for Java Developer in heart and soul");
        d.setLocation("Amsterdam");
        d.setPostedAt(FIXED_DATE);
        d.setApplications(List.of(
                applicationDto(10L, 1L, 3L, "APPLIED", "ok"),
                applicationDto(11L, 1L, 3L, "PENDING", "ok2")
        ));
        return d;
    }

    // given
    @Test
    void findVacancyByIdSuccess() throws Exception {
        var v1 = new VacancyDto();
        v1.setId(1L);
        v1.setTitle("Title");
        v1.setCompanyName("Oracle Company");
        v1.setDescription("Oracle Developer");
        v1.setLocation("Voorschoten Location");
        v1.setPostedAt(FIXED_DATE);
        v1.setApplications(List.of(
                applicationDto(10L, 1L, 3L, "APPLIED", "ok"),
                applicationDto(11L, 1L, 3L, "PENDING", "ok2")
        ));

        when(vacancyService.findById(1L)).thenReturn(v1);

        mockMvc.perform(get( baseUrl + "/vacancies/1").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Title"))
                .andExpect(jsonPath("$.data.companyName").value("Oracle Company"))
                .andExpect(jsonPath("$.data.description").value("Oracle Developer"))
                .andExpect(jsonPath("$.data.location").value("Voorschoten Location"))
                .andExpect(jsonPath("$.data.postedAt", org.hamcrest.Matchers.startsWith("2025-08-15T12:00:00")))
                .andExpect(jsonPath("$.data.applications[0].status").value("APPLIED"));

        verify(vacancyService).findById(1L);
    }

    @Test
    void findVacancyByIdNotFound() throws Exception {
        // given
        when(vacancyService.findById(404L)).thenThrow(new ObjectNotFoundException("Vacancy", 404L));

        // when and then
        mockMvc.perform(get( baseUrl +"/vacancies/404").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find " + OBJECT_VACANCY + " with Id: 404 :("))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        verify(vacancyService).findById(404L);
    }

    @Test
    void findAllVacanciesSuccess() throws Exception {
        var v1 = new VacancyDto();
        v1.setId(1L);
        v1.setTitle("Title");
        v1.setCompanyName("Oracle Company");
        v1.setDescription("Oracle Developer");
        v1.setLocation("Voorschoten Location");
        v1.setPostedAt(FIXED_DATE);
        v1.setApplications(List.of(
                applicationDto(10L, 1L, 3L, "APPLIED", "ok"),
                applicationDto(11L, 1L, 3L, "PENDING", "ok2")
        ));

        var v2 = new VacancyDto();
        v2.setId(2L);
        v2.setTitle("Title2");
        v2.setCompanyName("IBM Company");
        v2.setDescription("J2EE Developer");
        v2.setLocation("Spijkenisse Location");
        v2.setPostedAt(FIXED_DATE);
        v2.setApplications(List.of(
                applicationDto(10L, 1L, 3L, "APPLIED", "ok"),
                applicationDto(11L, 1L, 3L, "PENDING", "ok2")
        ));

        when(vacancyService.findAll()).thenReturn(List.of(v1, v2));

        mockMvc.perform(get( baseUrl + "/vacancies").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
               // .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Title"))
                .andExpect(jsonPath("$.data[0].companyName").value("Oracle Company"))
                .andExpect(jsonPath("$.data[0].description").value("Oracle Developer"))
                .andExpect(jsonPath("$.data[0].location").value("Voorschoten Location"))
                .andExpect(jsonPath("$.data[0].postedAt",
                        org.hamcrest.Matchers.matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*")))
                .andExpect(jsonPath("$.data[0].applications[0].status").value("APPLIED"))

                .andExpect(jsonPath("$.data[1].title").value("Title2"))
                .andExpect(jsonPath("$.data[1].companyName").value("IBM Company"))
                .andExpect(jsonPath("$.data[1].description").value("J2EE Developer"))
                .andExpect(jsonPath("$.data[1].location").value("Spijkenisse Location"))
                .andExpect(jsonPath("$.data[1].postedAt",
                        org.hamcrest.Matchers.matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*")))
                .andExpect(jsonPath("$.data[1].applications[0].status").value("APPLIED"));
        verify(vacancyService).findAll();

    }


    @Test
    void createVacancySuccess() throws Exception {
        var requestDto  = TD.vacancyDto(null);
        var responseDto = TD.vacancyDto(10L);

        //given(vacancyService.create(any(VacancyDto.class))).willReturn(responseDto);
        when(vacancyService.create(any(VacancyDto.class))).thenReturn(responseDto);

        mockMvc.perform(post(baseUrl + "/vacancies/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.CREATED)) // = 200 in jouw Result schema
                .andExpect(jsonPath("$.message").value("Vacancy created successfully"))
                .andExpect(jsonPath("$.data.title").value("Java Developer"))
                .andExpect(jsonPath("$.data.companyName").value("IBM Company"))
                .andExpect(jsonPath("$.data.description").value("We are looking for an experienced Java Developer"))
                .andExpect(jsonPath("$.data.location").value("Amsterdam"))
                .andExpect(jsonPath("$.data.postedAt",
                        org.hamcrest.Matchers.matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*")))
                .andExpect(jsonPath("$.data.applications.length()").value(0));
        verify(vacancyService).create(any(VacancyDto.class));
    }


    @Test
    void createVacancyValidationErrorReturn400() throws Exception {

        var invalid  = sampleDto(null);

        mockMvc.perform(post(baseUrl + "/vacancies/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid))
                        .content(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Invalid request payload"))
                .andExpect(jsonPath("$.data", org.hamcrest.Matchers.containsString("JSON parse error")));
        org.mockito.Mockito.verifyNoInteractions(vacancyService);
    }


    @Test
    void update_success() throws Exception {
        Long vacancyId = 3L;
        var in = TD.applicationDto(0L, 1L, 3L, "APPLIED", "Motiv");
        var out = TD.applicationDto(42L, 1L, 3L, "PENDING", "Motiv");
        var requestDto  = sampleDto(1L);

        var responseDto = sampleDto(3L);
        responseDto.setTitle("J2EE Developer updated");
        responseDto.setCompanyName("Oracle Company updated");
        responseDto.setDescription("An experienced J2EE developer updated");
        responseDto.setLocation("Spijkenisse City updated");
        responseDto.setPostedAt(FIXED_DATE); // let op: zie opmerking hieronder
        responseDto.setApplications(List.of(in, out)); // status PENDING in dto[0]?

       when(vacancyService.update(eq(vacancyId), any(VacancyDto.class))).thenReturn(responseDto);

        mockMvc.perform(put(baseUrl + "/vacancies/{id}", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"))
                .andExpect(jsonPath("$.data.id").value(3L))
                .andExpect(jsonPath("$.data.title").value("J2EE Developer updated"))
                .andExpect(jsonPath("$.data.companyName").value("Oracle Company updated"))
                .andExpect(jsonPath("$.data.description").value("An experienced J2EE developer updated"))
                .andExpect(jsonPath("$.data.location").value("Spijkenisse City updated"))
                .andExpect(jsonPath("$.data.postedAt").exists())
                .andExpect(jsonPath("$.data.applications[0].status").value("APPLIED"));
        verify(vacancyService).update(eq(3L), any(VacancyDto.class));
    }

    @Test
    void updateVacancy_validationError_returns400() throws Exception {
        var invalid = new VacancyDto();
        invalid.setId(999L);
        invalid.setTitle("");         // invalid: @NotBlank
        invalid.setCompanyName(null); // invalid: @NotNull

        mockMvc.perform(put(baseUrl + "/vacancies/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Invalid request payload")) // of "Validation failed"
                .andExpect(jsonPath("$.data.title").exists())
                .andExpect(jsonPath("$.data.companyName").exists());
    }


    // ---------- DELETE ----------
    @Test
    void delete_success() throws Exception {
        Long id = 1L;
        //willDoNothing().given(vacancyService).delete(1L);

        mockMvc.perform(delete(baseUrl + "/vacancies/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"));
    }

    // ---------- NOT FOUND via Advice ----------
    @Test
    void deleteVacancyNotFoutd_404() throws Exception {
        Long vacancyId = 404L;
        doThrow(new ObjectNotFoundException("Vacancy", vacancyId)).when(vacancyService).findById(vacancyId);

        mockMvc.perform(get(baseUrl + "/vacancies/{id}", vacancyId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find " + OBJECT_VACANCY + " with Id: 404 :(" ))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        verify(vacancyService).findById(vacancyId);
    }
}