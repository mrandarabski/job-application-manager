package nl.andarabski.service;

import nl.andarabski.dto.VacancyDto;
import nl.andarabski.model.Application;
import nl.andarabski.model.Vacancy;
import nl.andarabski.repository.VacancyRepository;
import nl.andarabski.system.exception.ObjectNotFoundException;
import nl.andarabski.utils.StubDataEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class VacancyServiceTest {

    @Mock
    private VacancyRepository vacancyRepository;

    @InjectMocks
    private VacancyService vacancyService;

    List<Vacancy> listVacancies;
    List<Application> listApplications;
    StubDataEntities subData;

    @BeforeEach
    void setUp() {
        subData = new StubDataEntities();

        Vacancy vacancy1 = new Vacancy();
        vacancy1.setTitle("Java Developer");
        vacancy1.setDescription("Java Developer");
        vacancy1.setLocation("Amsterdam");
        vacancy1.setPostedAt(new java.util.Date());
        vacancy1.setApplications(this.subData.getListApplications());

        Vacancy vacancy2 = new Vacancy();
        vacancy2.setTitle("Python Developer");
        vacancy2.setDescription("Python Developer");
        vacancy2.setLocation("Rotterdam");
        vacancy2.setPostedAt(new java.util.Date());
        vacancy2.setApplications(this.subData.getListApplications());

        Vacancy vacancy3 = new Vacancy();
        vacancy3.setTitle("C# Developer");
        vacancy3.setDescription("C# Developer");
        vacancy3.setLocation("Utrecht");
        vacancy3.setPostedAt(new java.util.Date());
        vacancy3.setApplications(this.subData.getListApplications());

        this.listVacancies = new java.util.ArrayList<>();
        this.listVacancies.add(vacancy1);
        this.listVacancies.add(vacancy2);
        this.listVacancies.add(vacancy3);
    }

    @Test
    void testFindAllVacancies() {
        // Given
        given(this.vacancyRepository.findAll()).willReturn(this.listVacancies);
        List<VacancyDto> actualVacancies = this.vacancyService.findAll();

        // When
        assertThat(actualVacancies.size()).isEqualTo(this.listVacancies.size());

        // Then
        verify(this.vacancyRepository, times(1)).findAll();

    }

    @Test
    void testFindByIdVacancySuccess() {
        // Given
        Vacancy vacancy1 = new Vacancy();
        vacancy1.setTitle("Java Developer");
        vacancy1.setDescription("Java Developer");
        vacancy1.setLocation("Amsterdam");
       // Date fixedDate = new Date();

        //vacancy1.setPostedAt(fixedDate);
        vacancy1.setPostedAt(this.listVacancies.get(0).getPostedAt());
        vacancy1.setApplications(this.subData.getListApplications());
        given(this.vacancyRepository.findById(1L)).willReturn(java.util.Optional.of(this.listVacancies.get(0)));

        // When
        VacancyDto returnVacancy = this.vacancyService.findById(1L);
        assertThat(returnVacancy.getTitle()).isEqualTo(vacancy1.getTitle());
        assertThat(returnVacancy.getDescription()).isEqualTo(vacancy1.getDescription());
        assertThat(returnVacancy.getLocation()).isEqualTo(vacancy1.getLocation());
        assertThat(returnVacancy.getPostedAt()).isCloseTo(vacancy1.getPostedAt(), 5);
        assertThat(returnVacancy.getApplications()).isEqualTo(vacancy1.getApplications());

        // Then
        verify(this.vacancyRepository, times(1)).findById(1L);

    }

    @Test
    void testFindByIdVacancyNotFound() {
        // Given
        given(this.vacancyRepository.findById(1L)).willReturn(java.util.Optional.empty());
        Throwable thrown = catchThrowable( () -> this.vacancyService.findById(1L));

        // When
        assertThat(thrown).isInstanceOf(ObjectNotFoundException.class);
        assertThat(thrown).hasMessageContaining("Could not find Vacancy with Id: 1 :(");

        // Then
        verify(this.vacancyRepository, times(1)).findById(1L);

    }

    @Test
    void testSaveVacancySucess() {
        // Given
        Vacancy vacancy1 = new Vacancy();
        vacancy1.setTitle("Java Developer");
        vacancy1.setDescription("Java Developer");
        vacancy1.setLocation("Amsterdam");
        vacancy1.setPostedAt(new java.util.Date());
        vacancy1.setApplications(this.subData.getListApplications());

        given((Vacancy) this.vacancyRepository.save(vacancy1)).willReturn(vacancy1);

        // When
        Vacancy returnVacancy = this.vacancyService.save(vacancy1);

        // Then
        assertThat(returnVacancy.getTitle()).isEqualTo(vacancy1.getTitle());
        assertThat(returnVacancy.getDescription()).isEqualTo(vacancy1.getDescription());
        assertThat(returnVacancy.getLocation()).isEqualTo(vacancy1.getLocation());
        assertThat(returnVacancy.getPostedAt()).isCloseTo(vacancy1.getPostedAt(), 5);
        assertThat(returnVacancy.getApplications()).isEqualTo(vacancy1.getApplications());
        verify(this.vacancyRepository, times(1)).save(vacancy1);

    }

    @Test
    void testpUdateVacancySuccsses() {
        // Given
        Vacancy oldVacancy = new Vacancy();
        oldVacancy.setId(1L);
        oldVacancy.setTitle("Python Developer");
        oldVacancy.setDescription("Python Developer");
        oldVacancy.setLocation("Rotterdam");
        oldVacancy.setPostedAt(new java.util.Date());
        oldVacancy.setApplications(this.subData.getListApplications());

        Vacancy update = new Vacancy();
        update.setTitle("Python Developer Update");
        update.setDescription("Python Developer Update");
        update.setLocation("Rotterdam Update");
        update.setPostedAt(new java.util.Date());
        update.setApplications(this.subData.getListApplications());

        given(this.vacancyRepository.findById(1l)).willReturn(Optional.of(oldVacancy));
        given(this.vacancyRepository.save(oldVacancy)).willReturn(oldVacancy);

        // When
        Vacancy updatedVacancy = this.vacancyService.update(1L, update);
        assertThat(updatedVacancy.getId()).isEqualTo(1L);
        assertThat(updatedVacancy.getTitle()).isEqualTo(update.getTitle());
        assertThat(updatedVacancy.getDescription()).isEqualTo(update.getDescription());
        assertThat(updatedVacancy.getLocation()).isEqualTo(update.getLocation());
        assertThat(updatedVacancy.getPostedAt()).isCloseTo(update.getPostedAt(), 5);
        assertThat(updatedVacancy.getApplications()).isEqualTo(update.getApplications());

        // Then
        verify(this.vacancyRepository, times(1)).findById(1L);
        verify(this.vacancyRepository, times(1)).save(oldVacancy);

    }

    @Test
    void testpUdateVacancyNotFound() {
        // Given
        Vacancy update = new Vacancy();
        update.setTitle("Python Developer Update");
        update.setDescription("Python Developer Update");
        update.setLocation("Rotterdam Update");
        update.setPostedAt(new java.util.Date());
        update.setApplications(this.subData.getListApplications());

        given(this.vacancyRepository.findById(1l)).willReturn(java.util.Optional.empty());

        // When
        assertThrows(ObjectNotFoundException.class, () -> vacancyService.update(1L, update));

        // Then
        verify(this.vacancyRepository, times(1)).findById(1L);

    }

    @Test
    void testDeleteVacancySuccess() {
        // Given
        Vacancy vacancy = new Vacancy();
        vacancy.setTitle("Python Developer");
        vacancy.setDescription("Python Developer");
        vacancy.setLocation("Rotterdam");
        vacancy.setPostedAt(new java.util.Date());
        vacancy.setApplications(this.subData.getListApplications());

        given(this.vacancyRepository.findById(1L)).willReturn(java.util.Optional.of(vacancy));
        doNothing().when(this.vacancyRepository).deleteById(1l);

        // When
        this.vacancyService.delete(1l);

        // Then
        verify(this.vacancyRepository, times(1)).deleteById(1l);

    }

    @Test
    void testDeleteVacancyNotFound() {
        // Given
        given(this.vacancyRepository.findById(1L)).willReturn(java.util.Optional.empty());

        // When
        assertThrows(ObjectNotFoundException.class, () -> vacancyService.delete(1L));

        // Then
        verify(this.vacancyRepository, times(1)).findById(1L);

    }

}