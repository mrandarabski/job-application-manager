package nl.andarabski.service;

import nl.andarabski.converter.ApplicationToApplicationDtoConverter;
import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.model.*;
import nl.andarabski.repository.*;
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
class ApplicationServiceTest {

    @Mock ApplicationRepository applicationRepository;
    @Mock UserRepository userRepository;
    @Mock VacancyRepository vacancyRepository;
    @Mock ApplicationToApplicationDtoConverter appToDtoConverter;

    @InjectMocks ApplicationService applicationService;

    @Test
    void findAll_mapsAndKeepsOrder() {
        var u = user(1L); var v = vacancy(3L);
        var a1 = application(10L, u, v, ApplicationStatus.APPLIED, "ok");
        var a2 = application(11L, u, v, ApplicationStatus.PENDING, "ok2");
        given(applicationRepository.findAll()).willReturn(List.of(a1, a2));

        var d1 = TD.applicationDto(10L, 1L, 3L, "APPLIED", "ok");
        var d2 = TD.applicationDto(11L, 1L, 3L, "PENDING", "ok2");
        given(appToDtoConverter.convert(a1)).willReturn(d1);
        given(appToDtoConverter.convert(a2)).willReturn(d2);


        var out = applicationService.findAll();

        assertThat(out).hasSize(2);
        assertThat(out).extracting(ApplicationDto::getId).containsExactly(10L, 11L);

        verify(applicationRepository).findAll();
        verify(appToDtoConverter, times(2)).convert(any(Application.class));
        verifyNoMoreInteractions(applicationRepository, appToDtoConverter);
    }

    @Test
    void save_assignsIdAndPreservesRelations() {
        var u = user(1L); var v = vacancy(3L);
        var toSave = application(0L, u, v, ApplicationStatus.APPLIED, "Motiv");
        toSave.setId(null); // simulatie nieuw

        given(applicationRepository.save(any(Application.class))).willAnswer(inv -> {
            Application a = inv.getArgument(0); a.setId(42L); return a;
        });

        var saved = applicationService.save(toSave);

        assertThat(saved.getId()).isEqualTo(42L);
        assertThat(saved.getUser().getId()).isEqualTo(1L);
        assertThat(saved.getVacancy().getId()).isEqualTo(3L);

        var captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        assertThat(captor.getValue().getMotivation()).isEqualTo("Motiv");
        verifyNoMoreInteractions(applicationRepository);
    }

    @Test
    void update_mergesAndSaves() {
        var u = user(1L); var v = vacancy(3L);
        var existing = application(1L, u, v, ApplicationStatus.PENDING, "old");
        var patch = new Application();
        patch.setMotivation("new"); patch.setStatus(ApplicationStatus.APPLIED); patch.setAppliedAt(FIXED_DATE);

        given(applicationRepository.findById(1L)).willReturn(Optional.of(existing));
        given(applicationRepository.save(any(Application.class))).willAnswer(inv -> inv.getArgument(0));

        var updated = applicationService.update(1L, patch);

        assertThat(updated.getMotivation()).isEqualTo("new");
        assertThat(updated.getStatus()).isEqualTo(ApplicationStatus.APPLIED);

        InOrder io = inOrder(applicationRepository);
        io.verify(applicationRepository).findById(1L);
        io.verify(applicationRepository).save(any(Application.class));
        io.verifyNoMoreInteractions();
    }

    @Test
    void delete_success() {
        var u = user(1L); var v = vacancy(3L);
        var app = application(1L, u, v, ApplicationStatus.APPLIED, "ok");
        given(applicationRepository.findById(1L)).willReturn(Optional.of(app));

        assertDoesNotThrow(() -> applicationService.delete(1L));

        InOrder io = inOrder(applicationRepository);
        io.verify(applicationRepository).findById(1L);
        io.verify(applicationRepository).deleteById(1L);
        io.verifyNoMoreInteractions();
    }

    // 1) findById – not found
    @Test
    void findById_notFound_throws() {
        given(applicationRepository.findById(404L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.findById(404L))
                .isInstanceOf(ObjectNotFoundException.class);

        verify(applicationRepository).findById(404L);
        verifyNoMoreInteractions(applicationRepository);
    }

    // 2) delete – not found
    @Test
    void delete_notFound_throws() {
        given(applicationRepository.findById(77L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.delete(77L))
                .isInstanceOf(ObjectNotFoundException.class);

        verify(applicationRepository).findById(77L);
        verify(applicationRepository, never()).deleteById(anyLong());
        verifyNoMoreInteractions(applicationRepository);
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
        var u = TD.user(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(u));
        given(vacancyRepository.findById(2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.applyToVacancy(1L, 2L, "mot"))
                .isInstanceOf(ObjectNotFoundException.class);

        verify(userRepository).findById(1L);
        verify(vacancyRepository).findById(2L);
        verifyNoMoreInteractions(userRepository, vacancyRepository);
        verifyNoInteractions(applicationRepository);
    }

    @Test
    void shouldApplyToVacancySuccessfully() {
        var u = user(1L); var v = vacancy(2L);
        given(userRepository.findById(1L)).willReturn(Optional.of(u));
        given(vacancyRepository.findById(2L)).willReturn(Optional.of(v));
        given(applicationRepository.findAll()).willReturn(List.of());
        given(applicationRepository.save(any(Application.class))).willAnswer(inv -> {
            Application a = inv.getArgument(0); a.setId(99L); return a;
        });

        var result = applicationService.applyToVacancy(1L, 2L, "I am excited");

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getUser().getId()).isEqualTo(1L);
        assertThat(result.getVacancy().getId()).isEqualTo(2L);
        // NB: laat de service 'PENDING' zetten zoals eerder besproken
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.PENDING);

        InOrder io = inOrder(userRepository, vacancyRepository, applicationRepository);
        io.verify(userRepository).findById(1L);
        io.verify(vacancyRepository).findById(2L);
        io.verify(applicationRepository, times(2)).findAll();
        io.verify(applicationRepository).save(any(Application.class));
        io.verifyNoMoreInteractions();
    }

    @Test
    void shouldNotApplyTwiceToSameVacancy() {
        var u = user(1L); var v = vacancy(2L);
        var existing = application(10L, u, v, ApplicationStatus.APPLIED, "dup");
        given(userRepository.findById(1L)).willReturn(Optional.of(u));
        given(vacancyRepository.findById(2L)).willReturn(Optional.of(v));
        given(applicationRepository.findAll()).willReturn(List.of(existing));

        assertThatThrownBy(() -> applicationService.applyToVacancy(1L, 2L, "again"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(applicationRepository, never()).save(any());
    }
}
