package nl.andarabski.service;

import nl.andarabski.converter.*;
import nl.andarabski.dto.*;
import nl.andarabski.model.*;
import nl.andarabski.repository.VacancyRepository;
import nl.andarabski.system.exception.ObjectNotFoundException;
import nl.andarabski.testsupport.TD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static nl.andarabski.testsupport.TD.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class VacancyServiceTest {

    @Mock VacancyRepository vacancyRepository;
    @Mock VacancyToVacancyDtoConverter vacancyToDto;
    @Mock ApplicationToApplicationDtoConverter appToDto;

    @InjectMocks VacancyService vacancyService;

    @Test
    void findById_mapsApplications() {
        var v = vacancy(3L);
        var u = user(1L);
        var a1 = application(10L, u, v, ApplicationStatus.APPLIED, "ok");
        var a2 = application(11L, u, v, ApplicationStatus.PENDING, "ok2");

        given(vacancyRepository.findById(3L)).willReturn(Optional.of(v));
        VacancyDto dto = vacancyDto(3L);
        given(vacancyToDto.convert(v)).willReturn(dto);
        given(appToDto.convert(a1)).willReturn(applicationDto(10L, 1L, 3L, "APPLIED", "ok"));
        given(appToDto.convert(a2)).willReturn(applicationDto(11L, 1L, 3L, "PENDING", "ok2"));

        var out = vacancyService.findById(3L);

        assertThat(out.getId()).isEqualTo(3L);
        assertThat(out.getApplications()).hasSize(2);
        assertThat(out.getPostedAt()).isEqualTo(FIXED_DATE);

        verify(vacancyRepository).findById(3L);
        verify(vacancyToDto).convert(v);
        verify(appToDto, times(2)).convert(any(Application.class));
        verifyNoMoreInteractions(vacancyRepository, vacancyToDto, appToDto);
    }

    // 1) findById – not found
    @Test
    void findById_notFound_throws() {
        given(vacancyRepository.findById(77L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> vacancyService.findById(77L))
                .isInstanceOf(ObjectNotFoundException.class);

        verify(vacancyRepository).findById(77L);
        verifyNoMoreInteractions(vacancyRepository, vacancyToDto, appToDto);
    }

    // 2) findAll – twee items + mapping
    @Test
    void findAll_mapsTwoVacancies() {
        var v1 = TD.vacancy(1L);
        var v2 = TD.vacancy(2L);
        given(vacancyRepository.findAll()).willReturn(List.of(v1, v2));

        var d1 = TD.vacancyDto(1L);
        var d2 = TD.vacancyDto(2L);
        given(vacancyToDto.convert(v1)).willReturn(d1);
        given(vacancyToDto.convert(v2)).willReturn(d2);

        var out = vacancyService.findAll();

        assertThat(out).extracting(VacancyDto::getId).containsExactly(1L, 2L);
        verify(vacancyRepository).findAll();
        verify(vacancyToDto, times(2)).convert(any(Vacancy.class));
        verifyNoMoreInteractions(vacancyRepository, vacancyToDto);
    }

    // 3) save – id toekenning
    @Test
    void save_success_assignsId() {
        var toSave = TD.vacancy(0L); toSave.setId(null);
        given(vacancyRepository.save(any(Vacancy.class))).willAnswer(inv -> {
            Vacancy x = inv.getArgument(0); x.setId(55L); return x;
        });

        var saved = vacancyService.save(toSave);

        assertThat(saved.getId()).isEqualTo(55L);
        verify(vacancyRepository).save(toSave);
        verifyNoMoreInteractions(vacancyRepository);
    }

    // 4) update – success (incl. companyName verifiëren)
    @Test
    void update_success_mergesAllFields_includingCompanyName() {
        var existing = TD.vacancy(3L);
        var patch = new Vacancy();
        patch.setTitle("Kotlin Dev");
        patch.setCompanyName("Oracle");
        patch.setDescription("desc");
        patch.setLocation("Utrecht");
        patch.setPostedAt(TD.FIXED_DATE);

        given(vacancyRepository.findById(3L)).willReturn(Optional.of(existing));
        given(vacancyRepository.save(any(Vacancy.class))).willAnswer(inv -> inv.getArgument(0));

        var updated = vacancyService.update(3L, patch);

        assertThat(updated.getTitle()).isEqualTo("Kotlin Dev");
        assertThat(updated.getCompanyName()).isEqualTo("Oracle");
        assertThat(updated.getDescription()).isEqualTo("desc");
        assertThat(updated.getLocation()).isEqualTo("Utrecht");
        assertThat(updated.getPostedAt()).isEqualTo(TD.FIXED_DATE);

        InOrder io = inOrder(vacancyRepository);
        io.verify(vacancyRepository).findById(3L);
        io.verify(vacancyRepository).save(any(Vacancy.class));
        io.verifyNoMoreInteractions();
    }

    // 5) delete – not found
    @Test
    void delete_notFound_throws() {
        given(vacancyRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> vacancyService.delete(99L))
                .isInstanceOf(ObjectNotFoundException.class);

        verify(vacancyRepository).findById(99L);
        verify(vacancyRepository, never()).deleteById(anyLong());
        verifyNoMoreInteractions(vacancyRepository);
    }


    @Test
    void delete_success() {
        var v = vacancy(5L);
        given(vacancyRepository.findById(5L)).willReturn(Optional.of(v));

        assertDoesNotThrow(() -> vacancyService.delete(5L));

        InOrder io = inOrder(vacancyRepository);
        io.verify(vacancyRepository).findById(5L);
        io.verify(vacancyRepository).deleteById(5L);
        io.verifyNoMoreInteractions();
    }
}
