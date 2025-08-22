package nl.andarabski.service;

import nl.andarabski.converter.*;
import nl.andarabski.dto.*;
import nl.andarabski.model.*;
import nl.andarabski.repository.UserRepository;
import nl.andarabski.system.exception.ObjectNotFoundException;
import nl.andarabski.testsupport.TD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static nl.andarabski.testsupport.TD.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserToUserDtoConverter userToDto;
    @Mock ApplicationToApplicationDtoConverter appToDto;


    @InjectMocks UserService userService;

    @Test
    void findAll_mapsUsers() {
        var u1 = user(1L); var u2 = user(2L);
        given(userRepository.findAll()).willReturn(List.of(u1, u2));
        given(userToDto.convert(u1)).willReturn(userDto(1L));
        given(userToDto.convert(u2)).willReturn(userDto(2L));

        var out = userService.findAll();

        assertThat(out).extracting(UserDto::getId).containsExactly(1L, 2L);
        verify(userRepository).findAll();
        verify(userToDto, times(2)).convert(any(User.class));
        verifyNoMoreInteractions(userRepository, userToDto);
    }

    @Test
    void findById_includesApplications() {
        var u = user(1L);
        var v = vacancy(3L);
        var a1 = application(10L, u, v, ApplicationStatus.APPLIED, "ok");
        var a2 = application(11L, u, v, ApplicationStatus.PENDING, "ok2");
        given(userRepository.findById(1L)).willReturn(Optional.of(u));

        var dto = userDto(1L);
        given(userToDto.convert(u)).willReturn(dto);
        given(appToDto.convert(a1)).willReturn(applicationDto(10L, 1L, 3L, "APPLIED", "ok"));
        given(appToDto.convert(a2)).willReturn(applicationDto(11L, 1L, 3L, "PENDING", "ok2"));

        var out = userService.findById(1L);

        assertThat(out.getId()).isEqualTo(1L);
        assertThat(out.getApplications()).hasSize(2);
        verify(userRepository).findById(1L);
        verify(userToDto).convert(u);
        verify(appToDto, times(2)).convert(any(Application.class));
    }

    // 1) findById – not found
    @Test
    void findById_notFound_throws() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ObjectNotFoundException.class);

        verify(userRepository).findById(99L);
        verifyNoMoreInteractions(userRepository, userToDto, appToDto);
    }

    // 2) findAll – empty → no mapping
    @Test
    void findAll_empty_returnsEmptyAndNoMapping() {
        given(userRepository.findAll()).willReturn(List.of());

        var out = userService.findAll();

        assertThat(out).isEmpty();
        verify(userRepository).findAll();
        verify(userToDto, never()).convert(any());
        verifyNoMoreInteractions(userRepository, userToDto);
    }

    // 3) save – repo save + id terug
    @Test
    void save_success_assignsIdAndReturnsEntity() {
        var u = TD.user(0L); u.setId(null); // nieuw
        given(userRepository.save(any(User.class))).willAnswer(inv -> {
            User x = inv.getArgument(0); x.setId(7L); return x;
        });

        var saved = userService.save(u);

        assertThat(saved.getId()).isEqualTo(7L);
        verify(userRepository).save(u);
        verifyNoMoreInteractions(userRepository);
    }

    // 4) update – success, velden gemerged
    @Test
    void update_success_mergesAllFields() {
        var existing = TD.user(1L);
        var patch = new User();
        patch.setFirstName("New");
        patch.setLastName("Name");
        patch.setEmail("new@mail.com");
        patch.setPassword("secret");
        patch.setRole("user");
        patch.setEnabled(false);

        given(userRepository.findById(1L)).willReturn(Optional.of(existing));
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        var updated = userService.update(1L, patch);

        assertThat(updated.getFirstName()).isEqualTo("New");
        assertThat(updated.getLastName()).isEqualTo("Name");
        assertThat(updated.getEmail()).isEqualTo("new@mail.com");
        assertThat(updated.getPassword()).isEqualTo("secret");
        assertThat(updated.getRole()).isEqualTo("user");
        assertThat(updated.isEnabled()).isFalse();

        InOrder io = inOrder(userRepository);
        io.verify(userRepository).findById(1L);
        io.verify(userRepository).save(any(User.class));
        io.verifyNoMoreInteractions();
    }

    // 5) update – not found
    @Test
    void update_notFound_throws() {
        given(userRepository.findById(9L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(9L, new User()))
                .isInstanceOf(ObjectNotFoundException.class);

        verify(userRepository).findById(9L);
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository);
    }

    // 6) findById – null applications → lege lijst
    @Test
    void findById_nullApplications_yieldsEmptyListInDto() {
        var u = TD.user(1L);
        u.setApplications(null); // edge case
        given(userRepository.findById(1L)).willReturn(Optional.of(u));

        var dto = TD.userDto(1L);
        given(userToDto.convert(u)).willReturn(dto);

        var out = userService.findById(1L);

        assertThat(out.getApplications()).isNotNull().isEmpty();
        verify(userRepository).findById(1L);
        verify(userToDto).convert(u);
        verify(appToDto, never()).convert(any());
    }


    @Test
    void delete_success() {
        var u = user(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(u));

        userService.delete(1L);

        InOrder io = inOrder(userRepository);
        io.verify(userRepository).findById(1L);
        io.verify(userRepository).deleteById(1L);
        io.verifyNoMoreInteractions();
    }
}
