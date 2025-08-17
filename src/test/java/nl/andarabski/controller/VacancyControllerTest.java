package nl.andarabski.controller;


import com.fasterxml.jackson.databind.ObjectMapper;

import nl.andarabski.dto.VacancyDto;
import nl.andarabski.service.VacancyService;
import nl.andarabski.system.StatusCode;
import nl.andarabski.system.exception.ObjectNotFoundException;
import nl.andarabski.utils.StubDataDtos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(VacancyController.class)
@AutoConfigureJsonTesters
@TestPropertySource(properties = "api.endpoint.base-url=/api/v1")
class VacancyControllerTest {

    @MockitoBean
    private VacancyService vacancyService;
    @Autowired
    JacksonTester<VacancyDto> vacancyDtoJson;
//    @MockitoBean
//    private VacancyDtoToVacancyConverter vacancyDtoToVacancyConverter;
//    @MockitoBean
//    private VacancyToVacancyDtoConverter vacancyToVacancyDtoConverter;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // gebruik dezelfde mapper als je app

    private StubDataDtos stubDataDtos;


    private List<VacancyDto> vacancies;

    // Testdata als DTO’s (niet als entiteiten!)
    private VacancyDto vacancy1, vacancy2, vacancy3;

    private final static String VACANCY_OBJECT = "Vacancy";

    @Value("/api/v1")
    String baseUrl;
    String expected;

    @BeforeEach
    void setUp() {
        vacancies = new ArrayList<>();

        stubDataDtos = new StubDataDtos();

        // als je response UTC gebruikt:
        Date postedAt = new Date();
         expected = OffsetDateTime.ofInstant(postedAt.toInstant(), ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        vacancy1 = new VacancyDto();
        vacancy1.setTitle("Java Developer");
        vacancy1.setCompanyName("Oracle");
        vacancy1.setDescription("We are looking for Java Developer in heart and soul");
        vacancy1.setLocation("Amsterdam");
        vacancy1.setPostedAt(new java.util.Date());
        vacancy1.setApplications(stubDataDtos.getListApplications());

        vacancy2 = new VacancyDto();
        vacancy2.setTitle("Python Developer");
        vacancy2.setCompanyName("Microsoft");
        vacancy2.setDescription("A Python Developer in heart and soul");
        vacancy2.setLocation("Rotterdam");
        vacancy2.setPostedAt(new java.util.Date());
        vacancy2.setApplications(stubDataDtos.getListApplications());

        vacancy3 = new VacancyDto();
        vacancy3.setTitle("C# Developer");
        vacancy3.setCompanyName("Oracle");
        vacancy3.setDescription("C# Developer");
        vacancy3.setLocation("Utrecht");
        vacancy3.setPostedAt(new java.util.Date());
        vacancy3.setApplications(stubDataDtos.getListApplications());

        this.vacancies.add(vacancy1);
        this.vacancies.add(vacancy2);
        this.vacancies.add(vacancy3);
    }

    @Test
    void findAllVacancies() throws Exception {
    // Given. Arrange inputs and targets. Define the behavior of Mock object userService.
        given(vacancyService.findAll()).willReturn(vacancies);

    // When and then
        this.mockMvc.perform(get(this.baseUrl + "/vacancies").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data[0].title").value("Java Developer"))
                .andExpect(jsonPath("$.data[0].companyName").value("Oracle"))
                .andExpect(jsonPath("$.data[0].description").value("We are looking for Java Developer in heart and soul"))
                .andExpect(jsonPath("$.data[0].location").value("Amsterdam"))
                .andExpect(jsonPath("$.data[0].postedAt").value( matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{2,3}(Z|\\+00:00)")))
                .andExpect(jsonPath("$.data[0].applications[0].status").value("APPLIED"))
                .andExpect(jsonPath("$.data[1].title").value("Python Developer"))
                .andExpect(jsonPath("$.data[1].companyName").value("Microsoft"))
                .andExpect(jsonPath("$.data[1].description").value("A Python Developer in heart and soul"))
                .andExpect(jsonPath("$.data[1].location").value("Rotterdam"))
                .andExpect(jsonPath("$.data[1].postedAt").value( matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{2,3}(Z|\\+00:00)")))
                .andExpect(jsonPath("$.data[1].applications[1].status").value("PENDING"));

    }

    @Test
    void findByIdSuccess() throws Exception {
        // Given. Arrange inputs and targets. Define the behavior of Mock object userService.
        given(this.vacancyService.findById(eq(1L))).willReturn(vacancies.get(0));


        // When and then
        this.mockMvc.perform(get(this.baseUrl + "/vacancies/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.title").value("Java Developer"))
                .andExpect(jsonPath("$.data.companyName").value("Oracle"))
                .andExpect(jsonPath("$.data.description").value("We are looking for Java Developer in heart and soul"))
                .andExpect(jsonPath("$.data.location").value("Amsterdam"))
                .andExpect(jsonPath("$.data.postedAt").value( matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{2,3}(Z|\\+00:00)")))
                .andExpect(jsonPath("$.data.applications[0].status").value("APPLIED"));
    }

    @Test
    void findByIdNotFound() throws Exception {
        // Given. Arrange inputs and targets. Define the behavior of Mock object userService.
        given(this.vacancyService.findById(eq(1L))).willThrow(new ObjectNotFoundException(VACANCY_OBJECT, 1L));

        // When and then
        this.mockMvc.perform(get(baseUrl + "/vacancies/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find " + VACANCY_OBJECT + " with Id: 1 :("))
                .andExpect(jsonPath("$.data").isEmpty());

    }




 /*   @Test
    void createVacancySuccess() throws Exception {
        // 1) Testdata
        List<ApplicationDto> apps = List.of(
                makeApp(1L, "APPLIED", "ok"),
                makeApp(2L, "PENDING", "ok")
        );

        VacancyDto request = new VacancyDto();
        request.setTitle("Java Developer");
        request.setCompanyName("Oracle");
        request.setDescription("A Python Developer in heart and soul");
        request.setLocation("Rotterdam");
        // meestal laat je postedAt door de server zetten; zo niet:
        request.setPostedAt(new Date());
        request.setApplications(apps);

        String body = objectMapper.writeValueAsString(request);

        // 2) Stubs (DTO -> Entity -> save -> Entity -> DTO)
        Vacancy entityToSave = new Vacancy();
        when(vacancyDtoToVacancyConverter.convert(any(VacancyDto.class)))
                .thenReturn(entityToSave);

        Vacancy savedEntity = new Vacancy();
        savedEntity.setId(1L);
        when(vacancyService.save(any(Vacancy.class)))
                .thenReturn(savedEntity);

        VacancyDto responseDto = new VacancyDto();
        responseDto.setId(1L);
        responseDto.setTitle("Java Developer");
        responseDto.setCompanyName("Oracle");
        responseDto.setDescription("A Python Developer in heart and soul");
        responseDto.setLocation("Rotterdam");
        responseDto.setPostedAt(new Date());
        responseDto.setApplications(apps);

        when(vacancyToVacancyDtoConverter.convert(savedEntity))
                .thenReturn(responseDto);

        // 3) Act + Assert
        mockMvc.perform(post(baseUrl + "/vacancies/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body)) // <-- stuur een VOLLEDIGE VacancyDto als JSON
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Create Success"))
                .andExpect(jsonPath("$.data.title").value("Java Developer"))
                .andExpect(jsonPath("$.data.companyName").value("Oracle"))
                .andExpect(jsonPath("$.data.description").value("A Python Developer in heart and soul"))
                .andExpect(jsonPath("$.data.location").value("Rotterdam"))
                .andExpect(jsonPath("$.data.postedAt",
                        org.hamcrest.Matchers.matchesPattern(
                                "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{2,3}(Z|\\+00:00)"
                        )))
                .andExpect(jsonPath("$.data.applications.length()").value(apps.size()));

        // 4) (optioneel) verify
        verify(vacancyDtoToVacancyConverter).convert(any(VacancyDto.class));
        verify(vacancyService).save(any(Vacancy.class));
        verify(vacancyToVacancyDtoConverter).convert(savedEntity);
    }

    private static ApplicationDto makeApp(Long id, String status, String motivation) {
        ApplicationDto d = new ApplicationDto();
        d.setId(id);
        d.setStatus(status);
        d.setMotivation(motivation);
        return d;
    }*/

    @Test
    void createVacancyFailed() {
        // Given. Arrange inputs and targets. Define the behavior of Mock object userService.


        // When and then

    }

    @Test
    void updateVacancySuccess() {
        // Given. Arrange inputs and targets. Define the behavior of Mock object userService.


        // When and then

    }

    @Test
    void updateVacancyFailed() {
        // Given. Arrange inputs and targets. Define the behavior of Mock object userService.


        // When and then

    }

    @Test
    void deleteVacancyByIdSuccess() {
        // Given. Arrange inputs and targets. Define the behavior of Mock object userService.


        // When and then

    }

    @Test
    void deleteVacancyByIdFailed() {
        // Given. Arrange inputs and targets. Define the behavior of Mock object userService.


        // When and then

    }
}