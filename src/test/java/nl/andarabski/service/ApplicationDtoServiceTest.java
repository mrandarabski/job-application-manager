package nl.andarabski.service;

import nl.andarabski.model.Application;
import nl.andarabski.model.ApplicationStatus;
import nl.andarabski.model.User;
import nl.andarabski.model.Vacancy;
import nl.andarabski.repository.ApplicationRepository;
import nl.andarabski.repository.UserRepository;
import nl.andarabski.repository.VacancyRepository;
import nl.andarabski.system.exception.ObjectNotFoundException;
import nl.andarabski.utils.StubDataEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.MILLIS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationDtoServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VacancyRepository vacancyRepository;
    @Mock
    private UserService userService;
    @Mock
    private VacancyService vacancyService;
    @InjectMocks
    private ApplicationService applicationService;

    List<Application> applications;
    StubDataEntities subData;

    @BeforeEach
    void setUp() {
        subData = new StubDataEntities();

        Application application1 = new Application();
        application1.setId(1l);
        application1.setUser(new User());
        application1.setVacancy(new Vacancy());
        application1.setMotivation("We will contact you soon.");
        application1.setStatus(ApplicationStatus.APPLIED);
        application1.setAppliedAt(new java.util.Date());

        Application application2 = new Application();
        application2.setId(2l);
        application2.setUser(new User());
        application2.setVacancy(new Vacancy());
        application2.setMotivation("We will contact you soon.");
        application2.setStatus(ApplicationStatus.PENDING);
        application2.setAppliedAt(new java.util.Date());

        Application application3 = new Application();
        application3.setId(3l);
        application3.setUser(new User());
        application3.setVacancy(new Vacancy());
        application3.setMotivation("We will contact you soon.");
        application3.setStatus(ApplicationStatus.REJECTED);
        application3.setAppliedAt(new java.util.Date());

        applications = new ArrayList<>();
        applications.add(application1);
        applications.add(application2);
        applications.add(application3);

    }

    @Test
    void testFindAllApplications() {
        // Given
        given(this.applicationRepository.findAll()).willReturn(this.applications);

        // When
        List<Application> acualApplicatins = this.applicationService.findAll();

        // Then
       assertThat(acualApplicatins.size()).isEqualTo(this.applications.size());
       verify(this.applicationRepository, times(1)).findAll();

    }

    @Test
    void testFindByIdApplicationSuccess() {
        // Given
        Application application = new Application();
        application.setId(1l);
        //application.setUser(new User());
        //application.setVacancy(new Vacancy());
        application.setMotivation("We will contact you soon.");
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(new java.util.Date());

        User user1 = new User();
        user1.setFirstName("Andre");
        user1.setLastName("Dabski");
        user1.setEmail("test@gmail.com");
        user1.setPassword("12345");
        user1.setRole("admin");
        user1.setEnabled(true);
        user1.setApplications(this.subData.getListApplications());

        Vacancy vacancy = new Vacancy();
        vacancy.setTitle("Python Developer");
        vacancy.setDescription("Python Developer");
        vacancy.setLocation("Rotterdam");
        vacancy.setPostedAt(new java.util.Date());
        vacancy.setApplications(this.subData.getListApplications());

        application.setUser(user1);
        application.setVacancy(vacancy);

        given(this.applicationRepository.findById(1l)).willReturn(Optional.of(application));

        // When
        Application actualApplication = this.applicationService.findById(1l);

        // Then
        assertThat(actualApplication.getId()).isEqualTo(1l);
        assertThat(actualApplication.getUser()).isEqualTo(user1);
        assertThat(actualApplication.getVacancy()).isEqualTo(vacancy);
        assertThat(actualApplication.getMotivation()).isEqualTo("We will contact you soon.");
        assertThat(actualApplication.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(actualApplication.getAppliedAt()).isCloseTo(application.getAppliedAt(), 5);

        verify(this.applicationRepository, times(1)).findById(1l);

    }

    @Test
    void testFindByIdApplicationNotFound() {
        // Given
        given(this.applicationRepository.findById(1l)).willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> {
            Application actualApplication = this.applicationService.findById(1l);
        });

        // Then
        assertThat(thrown)
        .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("Could not find Application with Id: 1 :(");

    }

    @Test
    void testSaveApplicationSucess() {
        // Given
        Application newApplication = new Application();
        newApplication.setId(1l);
        newApplication.setUser(new User());
        newApplication.setVacancy(new Vacancy());
        newApplication.setMotivation("We will contact you soon.");
        newApplication.setStatus(ApplicationStatus.APPLIED);
        newApplication.setAppliedAt(new java.util.Date());

        given(this.applicationRepository.save(newApplication)).willReturn(newApplication);

        // When
        Application actualApplication = this.applicationService.save(newApplication);

        // Then
        assertThat(actualApplication.getId()).isEqualTo(1l);
        assertThat(actualApplication.getUser()).isEqualTo(newApplication.getUser());
        assertThat(actualApplication.getVacancy()).isEqualTo(newApplication.getVacancy());
        assertThat(actualApplication.getMotivation()).isEqualTo("We will contact you soon.");
        assertThat(actualApplication.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        verify(this.applicationRepository, times(1)).save(newApplication);

    }

    @Test
    void testUpdateApplicationSuccsse() {
        // Given
        Application oldApplication = new Application();
        oldApplication.setId(1l);
        oldApplication.setUser(new User());
        oldApplication.setVacancy(new Vacancy());
        oldApplication.setMotivation("We will contact you soon.");
        oldApplication.setStatus(ApplicationStatus.APPLIED);
        oldApplication.setAppliedAt(new java.util.Date());

        Application update = new Application();
        update.setUser(new User());
        update.setVacancy(new Vacancy());
        update.setMotivation("We will contact you soon update.");
        update.setStatus(ApplicationStatus.APPLIED);
        update.setAppliedAt(new java.util.Date());

        given(this.applicationRepository.findById(1l)).willReturn(Optional.of(oldApplication));
        given(this.applicationRepository.save(oldApplication)).willReturn(oldApplication);

        // When
        Application updatedApplication = this.applicationService.update(1l, update);

        // Then
        assertThat(updatedApplication.getId()).isEqualTo(1l);
        assertThat(updatedApplication.getUser()).isEqualTo(oldApplication.getUser());
        assertThat(updatedApplication.getVacancy()).isEqualTo(oldApplication.getVacancy());
        assertThat(updatedApplication.getMotivation()).isEqualTo(update.getMotivation());
        assertThat(updatedApplication.getStatus()).isEqualTo(update.getStatus());
       // assertThat(updatedApplication.getAppliedAt()).isCloseTo(update.getAppliedAt(), 5);
        assertThat(updatedApplication.getAppliedAt().toInstant())
                .isCloseTo(update.getAppliedAt().toInstant(), within(5, MILLIS));
        verify(this.applicationRepository, times(1)).findById(1l);
        verify(this.applicationRepository, times(1)).save(oldApplication);

    }

    @Test
    void testpUdateApplicationNotFound() {
        // Given
        Application updatedApplication = new Application();
        updatedApplication.setUser(new User());
        updatedApplication.setVacancy(new Vacancy());
        updatedApplication.setMotivation("We will contact you soon.");
        updatedApplication.setStatus(ApplicationStatus.APPLIED);
        updatedApplication.setAppliedAt(new java.util.Date());
        given(this.applicationRepository.findById(1l)).willReturn(Optional.empty());

        // When
        assertThrows(ObjectNotFoundException.class, () -> {
           this.applicationService.update(1l, updatedApplication);
        });

        // Then
        verify(this.applicationRepository, times(1)).findById(1l);

    }

    @Test
    void testDeleteApplicationSuccess() {
        // Given
        Application application = new Application();
        application.setId(1l);
        application.setUser(new User());
        application.setVacancy(new Vacancy());
        application.setMotivation("We will contact you soon.");
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(new java.util.Date());

        given(this.applicationRepository.findById(1l)).willReturn(Optional.of(application));
        doNothing().when(this.applicationRepository).deleteById(1l);

        // When
        this.applicationService.delete(1l);

        // Then
        verify(this.applicationRepository, times(1)).deleteById(1l);

    }

    @Test
    void testDeleteApplicationNotFound() {
        // Given
        given(this.applicationRepository.findById(1l)).willReturn(Optional.empty());

        // When
        assertThrows(ObjectNotFoundException.class, () -> {
            this.applicationService.delete(1l);
        });

        // Then
        verify(this.applicationRepository, times(1)).findById(1l);

    }

    @Test
    void shouldApplyToVacancySuccessfully() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setApplications(new ArrayList<>());  // ✅ voorkomt de null
        Vacancy vacancy = new Vacancy();
        vacancy.setId(2L);
        vacancy.setApplications(new ArrayList<>());


        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(vacancyRepository.findById(2L)).willReturn(Optional.of(vacancy));
        given(applicationRepository.findAll()).willReturn(List.of()); // geen eerdere


        Application savedApplication = new Application();
        savedApplication.setUser(user);
        savedApplication.setVacancy(vacancy);
        savedApplication.setMotivation("I am excited to join.");
        savedApplication.setStatus(ApplicationStatus.PENDING);

        given(applicationRepository.save(any(Application.class))).willReturn(savedApplication);

        // Act
        Application result = applicationService.applyToVacancy(1L, 2L, "I am excited to join.");

        // Assert
        assertThat(result.getUser().getId()).isEqualTo(1L);
        assertThat(result.getVacancy().getId()).isEqualTo(2L);
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }


    @Test
    void shouldNotApplyTwiceToSameVacancy() {
        // Arrange
        User user = new User();
        user.setId(1L);
        Vacancy vacancy = new Vacancy();
        vacancy.setId(2L);
        Application existingApplication = new Application();
        existingApplication.setUser(user);
        existingApplication.setVacancy(vacancy);

        given(applicationRepository.findAll()).willReturn(List.of(existingApplication));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(vacancyRepository.findById(2L)).willReturn(Optional.of(vacancy));


        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                applicationService.applyToVacancy(1L, 2L, "Nog een poging")
        );
    }

}