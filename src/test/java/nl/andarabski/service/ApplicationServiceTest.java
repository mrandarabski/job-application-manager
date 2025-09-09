package nl.andarabski.service;

import nl.andarabski.converter.ApplicationToApplicationDtoConverter;
import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.mapper.ApplicationMapper;
import nl.andarabski.model.*;
import nl.andarabski.repository.*;
import nl.andarabski.system.exception.ObjectNotFoundException;
import nl.andarabski.testsupport.TD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.*;

import static nl.andarabski.testsupport.TD.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock ApplicationRepository applicationRepository;
    @Mock UserRepository userRepository;
    @Mock VacancyRepository vacancyRepository;
    @Mock ApplicationMapper applicationMapper;

    ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        var realMapper = Mappers.getMapper(ApplicationMapper.class);
        applicationService = new ApplicationService(applicationRepository ,  vacancyRepository, userRepository, applicationMapper);
    }
    @Test
    void findAll_mapsAndKeepsOrder() {
        var u = user(1L); var v = vacancy(3L);
        var a1 = application(10L, u, v, ApplicationStatus.APPLIED, "ok");
        var a2 = application(11L, u, v, ApplicationStatus.PENDING, "ok2");
        given(applicationRepository.findAll()).willReturn(List.of(a1, a2));

        var d1 = TD.applicationDto(10L, 1L, 3L, "APPLIED", "ok");
        var d2 = TD.applicationDto(11L, 1L, 3L, "PENDING", "ok2");
        given(applicationMapper.toDto(a1)).willReturn(d1);
        given(applicationMapper.toDto(a2)).willReturn(d2);


        var out = applicationService.findAll();

        assertThat(out).hasSize(2);
        assertThat(out).extracting(ApplicationDto::getId).containsExactly(10L, 11L);

        verify(applicationRepository).findAll();
        verify(applicationMapper).toDto(a1);
        verify(applicationMapper).toDto(a2);
        verifyNoMoreInteractions(applicationRepository, applicationMapper);
    }

    @Test
    void findById_notFound() {
        given(applicationRepository.findById(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.findById(10L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find Application with Id: " + 10L + " :(");

        verify(applicationRepository).findById(10L);
        verifyNoMoreInteractions(applicationRepository, applicationMapper);
    }

    @Test
    void findApplicationById_mapsAndKeepsOrder() {
        var u = user(1L); var v = vacancy(3L);
        var a1 = application(10L, u, v, ApplicationStatus.APPLIED, "ok");
        //var a2 = application(11L, u, v, ApplicationStatus.PENDING, "ok2");
       // v.setApplications(List.of(a1, a2));
        given(applicationRepository.findById(10L)).willReturn(Optional.of(a1));

        var dto = TD.applicationDto(10L, 1L, 3L, "APPLIED", "ok");
        given(applicationMapper.toDto(a1)).willReturn(dto);

        var out = applicationService.findById(10L);

        assertThat(out).isEqualTo(dto);
        assertThat(out.getId()).isEqualTo(10L);
        assertThat(out.getStatus()).isEqualTo("APPLIED");
        verify(applicationRepository).findById(10L);
        verify(applicationMapper).toDto(a1);
        verifyNoMoreInteractions(applicationRepository, applicationMapper);

    }

    @Test
    void saveApplication_assignsIdAndPreservesRelations() {
        ApplicationDto in = TD.applicationDto(null, 1L, 3L, "PENDING", "Motiv");

        // toEntity op exact dezelfde 'in'
        given(applicationMapper.toEntity(in)).willReturn(new Application());

        given(applicationRepository.save(any(Application.class)))
                .willAnswer(inv -> { Application a = inv.getArgument(0); a.setId(42L); return a; });

        var dto = TD.applicationDto(42L, 1L, 3L, "APPLIED", "Motiv");
        given(applicationMapper.toDto(any(Application.class))).willReturn(dto);

        ApplicationDto out = applicationService.create(in); // <— zelfde instance!

        assertThat(out.getId()).isEqualTo(42L);
        assertThat(out.getUserId()).isEqualTo(1L);
        assertThat(out.getVacancyId()).isEqualTo(3L);
        assertThat(out.getStatus()).isEqualTo("APPLIED");

        verify(applicationMapper).toEntity(in);
        verify(applicationRepository).save(any(Application.class));
        verify(applicationMapper).toDto(any(Application.class));
        verifyNoMoreInteractions(applicationRepository, applicationMapper);

    }

    @Test
    void update_mergesAndSaves() {
        var u = user(1L); var v = vacancy(3L);
        var existing = TD.application(1L, u, v, ApplicationStatus.PENDING, "old");
        ApplicationDto patch = new ApplicationDto();
        patch.setUserId(u.getId());
        patch.setVacancyId(v.getId());
        patch.setMotivation("new");
        patch.setStatus("PENDING");
        patch.setAppliedAt(FIXED_DATE);

        given(applicationRepository.findById(1L)).willReturn(Optional.of(existing));
        given(applicationRepository.save(any(Application.class))).willAnswer(inv -> inv.getArgument(0));

        given(applicationMapper.toDto(any(Application.class))).willAnswer(inv -> {
            Application a = inv.getArgument(0);
            ApplicationDto dto = new ApplicationDto();
            dto.setId(a.getId()); // ← essentieel
            dto.setUserId(a.getUser() != null ? a.getUser().getId() : null);
            dto.setVacancyId(a.getVacancy() != null ? a.getVacancy().getId() : null);
            dto.setMotivation(a.getMotivation());
            dto.setStatus(a.getStatus() != null ? a.getStatus().name() : null);
            dto.setAppliedAt(a.getAppliedAt());
            return dto;
        });

        var updated = applicationService.update(1L, patch);

        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getUserId()).isEqualTo(existing.getUser().getId());
        assertThat(updated.getVacancyId()).isEqualTo(existing.getVacancy().getId());
        assertThat(updated.getMotivation()).isEqualTo("new");
        assertThat(updated.getStatus()).isEqualTo("PENDING");
        assertThat(updated.getAppliedAt()).isEqualTo(FIXED_DATE);
        verify(applicationRepository).findById(1L);
        verify(applicationRepository).save(any(Application.class));
        verify(applicationMapper).toDto(any(Application.class));
        verifyNoMoreInteractions(applicationRepository, applicationMapper);
    }

    @Test
    void delete_success() {
        assertDoesNotThrow(() -> applicationService.delete(1L));

        verify(applicationRepository).deleteById(1L);
        verifyNoMoreInteractions(applicationRepository);
    }

    // 1) findById – not found
    @Test
    void delete_whenNotFound_translatesException() {
        doThrow(new EmptyResultDataAccessException(1))
                .when(applicationRepository).deleteById(999L);

        assertThatThrownBy(() -> applicationService.delete(999L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("999");

        verify(applicationRepository).deleteById(999L);
        verifyNoMoreInteractions(applicationRepository);
    }

    @Test
    void delete_whenIdIsNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> applicationService.delete(null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(applicationRepository);
    }


    // 3) applyToVacancy – user not found
    @Test
    void applyToVacancy_userNotFound_throws() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.applyToVacancy(1L, 2L, "mot"))
                .isInstanceOf(ObjectNotFoundException.class);

        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(vacancyRepository, applicationRepository);
    }

    // 4) applyToVacancy – vacancy not found
    @Test
    void applyToVacancy_vacancyNotFound_throws() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> applicationService.applyToVacancy(1L, 2L, "mot"))
                .isInstanceOf(ObjectNotFoundException.class);

        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(vacancyRepository, applicationRepository);
    }

    @Test
    void shouldApplyToVacancySuccessfully() {
        var u = user(1L); var v = vacancy(2L);
        given(userRepository.findById(1L)).willReturn(Optional.of(u));
        given(vacancyRepository.findById(2L)).willReturn(Optional.of(v));
        given(applicationRepository.existsByUserIdAndVacancyId(1L, 2L)).willReturn(false);
        given(applicationRepository.save(any(Application.class))).willAnswer(inv -> {
            Application a = inv.getArgument(0); a.setId(99L); return a;
        });

        var result = applicationService.applyToVacancy(1L, 2L, "I am excited");

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getUser().getId()).isEqualTo(1L);
        assertThat(result.getVacancy().getId()).isEqualTo(2L);
        // NB: laat de service 'PENDING' zetten zoals eerder besproken
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.PENDING);

        verify(applicationRepository).existsByUserIdAndVacancyId(1L, 2L);
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void shouldNotApplyTwiceToSameVacancy() {
        var u = user(1L); var v = vacancy(2L);
        var existing = application(10L, u, v, ApplicationStatus.APPLIED, "dup");
        given(userRepository.findById(1L)).willReturn(Optional.of(u));
        given(vacancyRepository.findById(2L)).willReturn(Optional.of(v));
        given(applicationRepository.existsByUserIdAndVacancyId(1L, 2L)).willReturn(true);

        assertThatThrownBy(() -> applicationService.applyToVacancy(1L, 2L, "again"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(applicationRepository).existsByUserIdAndVacancyId(1L, 2L);
        verify(applicationRepository, never()).save(any());
    }
}
