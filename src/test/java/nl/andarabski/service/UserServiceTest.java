package nl.andarabski.service;

import nl.andarabski.dto.*;
import nl.andarabski.mapper.UserMapper;
import nl.andarabski.model.*;
import nl.andarabski.repository.ApplicationRepository;
import nl.andarabski.repository.UserRepository;
import nl.andarabski.system.exception.ObjectNotFoundException;
import nl.andarabski.testsupport.TD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static nl.andarabski.testsupport.TD.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock ApplicationRepository applicationRepository;
    @Mock UserMapper userMapper;


    UserService userService;

    @BeforeEach
    void setUp() {
        var realMapper = Mappers.getMapper(UserMapper.class);
        userService = new UserService(userRepository, applicationRepository, userMapper);
    }

    @Test
    void findAll_mapsUsers() {
        var u1 = user(1L);
        var u2 = user(2L);
        given(userRepository.findAll()).willReturn(List.of(u1, u2));

        var d1 = TD.userDto(1L);
        var d2 = TD.userDto(2L);
        given(userMapper.toDto(u1)).willReturn(d1);
        given(userMapper.toDto(u2)).willReturn(d2);

        var out = userService.findAll();

        assertThat(out).extracting(UserDto::getId).containsExactly(1L, 2L);
        verify(userRepository).findAll();
        verify(userMapper).toDto(u1);
        verify(userMapper).toDto(u2);
        verifyNoMoreInteractions(userRepository, userMapper);
        verifyNoMoreInteractions(userRepository);
    }


    @Test
    void findById_includesApplications() {
        var u = user(1L);
        var v = vacancy(3L);
        var a1 = application(10L, u, v, ApplicationStatus.APPLIED, "ok");
        var a2 = application(11L, u, v, ApplicationStatus.PENDING, "ok2");
        v.setApplications(List.of(a1, a2));
        given(userRepository.findById(1L)).willReturn(Optional.of(u));

        var dto = userDto(1L);
        dto.setApplications(List.of(
                applicationDto(10L, 1L, 3L, "APPLIED", "ok"),
                applicationDto(11L, 1L, 3L, "PENDING", "ok2")
        ));
        given(userMapper.toDto(u)).willReturn(dto);

        var out = userService.findById(1L);

        assertThat(out.getId()).isEqualTo(1L);
        assertThat(out.getApplications()).hasSize(2);
        verify(userRepository).findById(1L);
        verify(userMapper).toDto(u);
        verifyNoMoreInteractions(userRepository, userMapper);
    }


    // 1) findById – not found
    @Test
    void findById_notFound_throws() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ObjectNotFoundException.class);

        verify(userRepository).findById(99L);
        verifyNoMoreInteractions(userRepository, userMapper);
    }


    // 2) findAll – empty → no mapping
    @Test
    void findAll_empty_returnsEmptyAndNoMapping() {
        given(userRepository.findAll()).willReturn(List.of());

        var out = userService.findAll();

        assertThat(out).isEmpty();
        verify(userRepository).findAll();
        verify(userMapper, never()).toDto(any());
        verifyNoMoreInteractions(userRepository, userMapper);
    }


    // 3) save – repo save + id terug
    @Test
    void save_success_assignsIdAndReturnsEntity() {
        // id moet null zijn bij create
        var dtoIn = TD.userDto(null);
        // entiteit zonder id
        var entity = TD.user(null);

        // mapper + repo stubs
        given(userMapper.toEntity(dtoIn)).willReturn(entity);
        given(userRepository.save(any(User.class))).willAnswer(inv -> {
            User x = inv.getArgument(0); x.setId(77L); return x;
        });
        // laat de mapper DTO teruggeven met het nieuwe id
        given(userMapper.toDto(any(User.class))).willReturn(TD.userDto(77L));


        // act
        var out = userService.create(dtoIn, null, null);
        // Assert
        assertThat(out.getId()).isEqualTo(77L);

        // Verify volgens & exacte interacties
        InOrder inOrder = inOrder(userMapper, userRepository);
        inOrder.verify(userMapper).toEntity(dtoIn);
        // of: save(any(User.class)) als je niet aan dezelfde referentie wilt vastzitten
        inOrder.verify(userRepository).save(entity);
        // of: toDto(any(User.class))
        inOrder.verify(userMapper).toDto(entity);
        inOrder.verifyNoMoreInteractions();
        // géén dubbele verifyNoMoreInteractions meer

    }


    // 4) update – success, velden gemerged
    @Test
    void update_success_mergesAllFields() {
        var existing = TD.user(1L);
        var patch = new UserDto();
        patch.setFirstName("LvGod");
        patch.setLastName("Andarabski");
        patch.setEmail("test@gmail.com");
        patch.setPassword("secret");
        patch.setAge(23);
        patch.setPhoto("photo");
        patch.setCv("doc.pdf");
        patch.setRole("admin");
        patch.setEnabled(true);
        patch.setApplications(List.of(
                applicationDto(10L, 1L, 3L, "APPLIED", "ok"),
                applicationDto(11L, 1L, 3L, "PENDING", "ok2")
        ));

        given(userRepository.findById(1L)).willReturn(Optional.of(existing));
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        // Laat de mock-mapper iets teruggeven
        given(userMapper.toDto(any(User.class))).willAnswer(inv -> {
            User u = inv.getArgument(0);
            UserDto d = new UserDto();
            d.setId(u.getId());
            d.setFirstName(u.getFirstName());
            d.setLastName(u.getLastName());
            d.setEmail(u.getEmail());
            d.setPassword(u.getPassword());
            d.setAge(u.getAge());
            d.setPhoto(u.getPhoto());
            d.setCv(u.getCv());
            d.setRole(u.getRole());
            d.setEnabled(u.isEnabled());

            // eventuele mapping van applications
            if (u.getApplications() != null) {
                d.setApplications(
                        u.getApplications().stream()
                                .map(a -> applicationDto(a.getId(), a.getUser().getId(), a.getVacancy().getId(),
                                        a.getStatus().name(), a.getMotivation()))
                                .toList()
                );
            }
            return d;
        });

        var updated = userService.update(1L, patch, null, null );

        assertThat(updated.getFirstName()).isEqualTo("LvGod");
        assertThat(updated.getLastName()).isEqualTo("Andarabski");
        assertThat(updated.getEmail()).isEqualTo("test@gmail.com");
        assertThat(updated.getPassword()).isEqualTo("secret");
        assertThat(updated.getAge()).isEqualTo(23);
        assertThat(updated.getPhoto()).isEqualTo("photo");
        assertThat(updated.getCv()).isEqualTo("doc.pdf");
        assertThat(updated.getRole()).isEqualTo("admin");
        assertThat(updated.isEnabled()).isTrue();
        assertThat(updated.getApplications()).hasSize(2);

        InOrder io = inOrder(userRepository);
        io.verify(userRepository).findById(1L);
        io.verify(userRepository).save(any(User.class));
        io.verifyNoMoreInteractions();
    }

    // 5) update – not found
    @Test
    void update_notFound_throws() {
        var patch = new UserDto();
        given(userRepository.findById(9L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(9L, patch, null, null))
                .isInstanceOf(ObjectNotFoundException.class)
                        .hasMessageContaining("User", 9);

        verify(userRepository).findById(9L);
        verify(userRepository, never()).save(any(User.class));
        verifyNoMoreInteractions(userRepository, userMapper);
    }


    // 6) findById – null applications → lege lijst
    @Test
    void findById_nullApplications_yieldsEmptyListInDto() {
        var u = TD.user(1L);
        u.setApplications(null); // edge case
        given(userRepository.findById(1L)).willReturn(Optional.of(u));

        var dto = TD.userDto(1L);
        // BELANGRIJK: gedrag van mapper vastleggen
        dto.setApplications(Collections.emptyList());
        given(userMapper.toDto(u)).willReturn(dto);

        // Act
        var out = userService.findById(1L);

        // Assert
        assertThat(out.getApplications()).isNotNull().isEmpty();

        // verify
        verify(userRepository).findById(1L);
        verify(userMapper).toDto(u);
        verifyNoMoreInteractions(userMapper);
    }


    @Test
    void delete_success() {
        var u = user(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(u));

        assertDoesNotThrow(() -> userService.delete(1L));

        InOrder io = inOrder(userRepository);
        io.verify(userRepository).findById(1L);
        io.verify(userRepository).delete(u);
        io.verifyNoMoreInteractions();
    }

    @Test
    void delete_notFound_throws() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> userService.delete(1L))
        .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("User", 1);


        verify(userRepository).findById(1L);
        verify(userRepository, never()).deleteById(anyLong());
        // extra safeguard
        verify(userRepository, never()).delete(any());
        verifyNoMoreInteractions(userRepository);
    }
}
