package nl.andarabski.service;

import nl.andarabski.dto.*;
import nl.andarabski.mapper.VacancyMapper;
import nl.andarabski.model.*;
import nl.andarabski.repository.ApplicationRepository;
import nl.andarabski.repository.VacancyRepository;
import nl.andarabski.system.exception.ObjectNotFoundException;
import nl.andarabski.testsupport.TD;
import org.junit.jupiter.api.BeforeEach;
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
    @Mock ApplicationRepository applicationRepository;
    @Mock VacancyMapper vacancyMapper;

    VacancyService vacancyService;

    @BeforeEach
    void setUp() {
        vacancyService = new VacancyService(vacancyRepository, applicationRepository, vacancyMapper);
    }

    @Test
    void findById_mapsApplications() {
        var v = vacancy(3L);
        var u = user(1L);
        var a1 = application(10L, u, v, ApplicationStatus.APPLIED, "ok");
        var a2 = application(11L, u, v, ApplicationStatus.PENDING, "ok2");
        v.setApplications(List.of(a1, a2));

        given(vacancyRepository.findById(3L)).willReturn(Optional.of(v));

        VacancyDto dto = vacancyDto(3L);
        dto.setPostedAt(FIXED_DATE);
        dto.setApplications(List.of(
                applicationDto(10L, 1L, 3L,"APPLIED", "ok"),
                applicationDto(11L, 1L, 3L,"PENDING", "ok2")
        ));
        given(vacancyMapper.toDto(v)).willReturn(dto);


        var out = vacancyService.findById(3L);

        assertThat(out.getId()).isEqualTo(3L);
        assertThat(out.getApplications()).hasSize(2);
        assertThat(out.getPostedAt()).isEqualTo(FIXED_DATE);

        verify(vacancyRepository).findById(3L);
        verify(vacancyMapper).toDto(v);

        verifyNoMoreInteractions(vacancyRepository, vacancyMapper);
    }

    // 1) findById – not found
    @Test
    void findById_notFound_throws() {
        given(vacancyRepository.findById(77L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> vacancyService.findById(77L))
                .isInstanceOf(ObjectNotFoundException.class);

        verify(vacancyRepository).findById(77L);
        verifyNoMoreInteractions(vacancyRepository, vacancyMapper);
    }

    // 2) findAll – twee items + mapping
    @Test
    void findAll_mapsTwoVacancies() {
        var v1 = TD.vacancy(1L);
        var v2 = TD.vacancy(2L);
        given(vacancyRepository.findAll()).willReturn(List.of(v1, v2));

        var d1 = TD.vacancyDto(1L);

        var d2 = TD.vacancyDto(2L);
        given(vacancyMapper.toDto(v1)).willReturn(d1);
        given(vacancyMapper.toDto(v2)).willReturn(d2);

        var out = vacancyService.findAll();

        assertThat(out).extracting(VacancyDto::getId).containsExactly(1L, 2L);

        verify(vacancyRepository).findAll();
        verify(vacancyMapper).toDto(v1);
        verify(vacancyMapper).toDto(v2);
        verifyNoMoreInteractions(vacancyRepository, vacancyMapper);
        verifyNoMoreInteractions(applicationRepository);
    }

    // 3) save – id toekenning
    @Test
    void save_success_assignsId() {
        var toSave = TD.vacancy(0L);
        toSave.setId(null);
        var dto = TD.vacancyDto(null); // of handmatig een VacancyDto bouwen

        given(vacancyMapper.toEntity(dto)).willReturn(toSave);
        given(vacancyMapper.toDto(any(Vacancy.class))).willReturn(TD.vacancyDto(55L));

        given(vacancyRepository.save(any(Vacancy.class))).willAnswer(inv -> {
            Vacancy x = inv.getArgument(0);
            x.setId(55L);
            return x;
        });
        var saved = vacancyService.create(dto);
        assertThat(saved.getId()).isEqualTo(55L);
        verify(vacancyRepository).save(toSave);
        verifyNoMoreInteractions(vacancyRepository);
    }

    // 4) update – success (incl. companyName verifiëren)
    @Test
    void update_success_mergesAllFields_includingCompanyName() {
        var existing = TD.vacancy(3L);
        var patch = new VacancyDto();
        patch.setTitle("Kotlin Dev");
        patch.setCompanyName("Oracle");
        patch.setDescription("desc");
        patch.setLocation("Utrecht");
        patch.setPostedAt(TD.FIXED_DATE);

        var expectedDto = new VacancyDto();
        expectedDto.setId(3L);
        expectedDto.setTitle("Kotlin Dev");
        expectedDto.setCompanyName("Oracle");
        expectedDto.setDescription("desc");
        expectedDto.setLocation("Utrecht");
        expectedDto.setPostedAt(TD.FIXED_DATE);

        given(vacancyRepository.findById(3L)).willReturn(Optional.of(existing));
        given(vacancyRepository.save(existing)).willReturn(existing);

        // mapper: simuleer partial update + terug naar DTO
        doAnswer(inv -> {
            Vacancy target = inv.getArgument(0);
            VacancyDto src = inv.getArgument(1);
            target.setTitle(src.getTitle());
            target.setCompanyName(src.getCompanyName());
            target.setDescription(src.getDescription());
            target.setLocation(src.getLocation());
            target.setPostedAt(src.getPostedAt());
            return null;
        }).when(vacancyMapper).update(eq(existing), eq(patch));

        given(vacancyMapper.toDto(existing)).willReturn(expectedDto);

        // act
        var updated = vacancyService.update(3L, patch);

        assertThat(updated.getTitle()).isEqualTo("Kotlin Dev");
        assertThat(updated.getCompanyName()).isEqualTo("Oracle");
        assertThat(updated.getDescription()).isEqualTo("desc");
        assertThat(updated.getLocation()).isEqualTo("Utrecht");
        assertThat(updated.getPostedAt()).isEqualTo(TD.FIXED_DATE);

        // assert
        InOrder io = inOrder(vacancyRepository); // let op: alléén repo hier
        io.verify(vacancyRepository).findById(3L);
        io.verify(vacancyRepository).save(any(Vacancy.class));
        io.verifyNoMoreInteractions();            // checkt alleen repo

        // mapper-calls niet in-order, maar wél expliciet
        verify(vacancyMapper).update(existing, patch);   // of hoe jouw method heet
        verify(vacancyMapper).toDto(any(Vacancy.class));
        verifyNoMoreInteractions(vacancyRepository, vacancyMapper);
    }

    @Test
    void update_notFound_returnsNotFound() {
        var patch = new VacancyDto();
        given(vacancyRepository.findById(11L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> vacancyService.update(11L, patch))
        .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("Vacancy", 11);
        verify(vacancyRepository).findById(11L);
        verify(vacancyRepository, never()).save(any(Vacancy.class));
        verifyNoMoreInteractions(vacancyMapper);
    }

    // 5) delete – not found
    @Test
    void delete_notFound_throws() {
        given(vacancyRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> vacancyService.delete(99L))
                .isInstanceOf(ObjectNotFoundException.class)
                        .hasMessageContaining("Vacancy", 99);

        verify(vacancyRepository).findById(99L);
        verify(vacancyRepository, never()).deleteById(anyLong());
        // extra safeguard
        verify(vacancyRepository, never()).delete(any());
        verifyNoMoreInteractions(vacancyRepository);
    }


    @Test
    void delete_success() {
        var v = vacancy(5L);
        given(vacancyRepository.findById(5L)).willReturn(Optional.of(v));

        assertDoesNotThrow(() -> vacancyService.delete(5L));

        InOrder io = inOrder(vacancyRepository);
        io.verify(vacancyRepository).findById(5L);
        io.verify(vacancyRepository).delete(v);
        io.verifyNoMoreInteractions();
    }
}
