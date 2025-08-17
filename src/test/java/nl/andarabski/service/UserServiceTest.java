package nl.andarabski.service;

import nl.andarabski.converter.ApplicationDtoToApplicationConverter;
import nl.andarabski.converter.ApplicationToApplicationDtoConverter;
import nl.andarabski.converter.UserDtoToUserConverter;
import nl.andarabski.converter.UserToUserDtoConverter;
import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.dto.UserDto;
import nl.andarabski.model.Application;
import nl.andarabski.model.ApplicationStatus;
import nl.andarabski.model.User;
import nl.andarabski.repository.UserRepository;
import nl.andarabski.repository.VacancyRepository;
import nl.andarabski.system.exception.ObjectNotFoundException;
import nl.andarabski.utils.StubDataEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock private UserRepository userRepository;

    // mock de converters die UserService gebruikt
    @Mock private UserToUserDtoConverter userConverter;
    @Mock private UserDtoToUserConverter userDtoConverter;
    @Mock private ApplicationToApplicationDtoConverter appConverter;

    @InjectMocks
    private UserService userService;

    private StubDataEntities subData;
    private List<User> users;


    @BeforeEach
    void setUp() {

        subData = new StubDataEntities();

        // generieke stub: kopieer de velden die je in de assertions gebruikt
        lenient().when(userConverter.convert(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            UserDto d = new UserDto();
            d.setId(u.getId());
            d.setFirstName(u.getFirstName());
            d.setLastName(u.getLastName());
            d.setEmail(u.getEmail());
            d.setPassword(u.getPassword());
            d.setRole(u.getRole());
            d.setEnabled(u.isEnabled());
            // applicaties worden hieronder door appConverter gezet
            return d;
        });

        // applicatie-conversie; meestal controleren tests hooguit de grootte/ids
        lenient().when(appConverter.convert(any(Application.class))).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            ApplicationDto d = new ApplicationDto();
            d.setId(a.getId());
            d.setMotivation(a.getMotivation());
            d.setStatus(a.getStatus() == null ? null : a.getStatus().name()); // <-- hier
            d.setAppliedAt(a.getAppliedAt());
            return d;
        });


        User user1 = new User();
        user1.setFirstName("Andre");
        user1.setLastName("Dabski");
        user1.setEmail("test@gmail.com");
        user1.setPassword("12345");
        user1.setRole("admin");
        user1.setEnabled(true);
        user1.setApplications(this.subData.getListApplications());

        User user2 = new User();
        user2.setFirstName("John");
        user2.setLastName("Johnson");
        user2.setEmail("test@gmail.com");
        user2.setPassword("54321");
        user2.setRole("admin user");
        user2.setEnabled(true);
        user2.setApplications(this.subData.getListApplications());

        User user3 = new User();
        user3.setFirstName("Sonny");
        user3.setLastName("Andarabski");
        user3.setEmail("test@gmail.com");
        user3.setPassword("12345");
        user3.setRole("user");
        user3.setEnabled(true);
        user3.setApplications(this.subData.getListApplications());

        this.users = new ArrayList();
        this.users.add(user1);
        this.users.add(user2);
        this.users.add(user3);
    }

    @Test
    public void findAllUsers() {
        given(this.userRepository.findAll()).willReturn(this.users);
        List<UserDto> actualUsers = this.userService.findAll();
        assertThat(actualUsers.size()).isEqualTo(this.users.size());
        verify(this.userRepository, times(1)).findAll();
    }

    @Test
    void testFindByIdSuccess() {
        User user1 = new User();
        user1.setFirstName("Andre");
        user1.setLastName("Dabski");
        user1.setEmail("test@gmail.com");
        user1.setPassword("12345");
        user1.setRole("admin");
        user1.setEnabled(true);
        user1.setApplications(this.subData.getListApplications());
        given(this.userRepository.findById(1L)).willReturn(Optional.of(user1));

        UserDto returnUser = this.userService.findById(1L);

        assertThat(returnUser.getId()).isEqualTo(user1.getId());
        assertThat(returnUser.getFirstName()).isEqualTo(user1.getFirstName());
        assertThat(returnUser.getLastName()).isEqualTo(user1.getLastName());
        assertThat(returnUser.getEmail()).isEqualTo(user1.getEmail());
        assertThat(returnUser.getPassword()).isEqualTo(user1.getPassword());
        assertThat(returnUser.getRole()).isEqualTo(user1.getRole());

        assertThat(returnUser.getApplications()).hasSameSizeAs(user1.getApplications());
        for (int i = 0; i < user1.getApplications().size(); i++) {
            String expectedStatus = user1.getApplications().get(i).getStatus() == null
                    ? null
                    : user1.getApplications().get(i).getStatus().name();

            assertThat(returnUser.getApplications().get(i).getStatus())
                    .isEqualTo(expectedStatus);
           // assertThat(returnUser.getApplications().get(i).getStatus()).isEqualTo("APPLIED");


            assertThat(returnUser.getApplications().get(i).getMotivation())
                    .isEqualTo(user1.getApplications().get(i).getMotivation());
            assertThat(returnUser.getApplications().get(i).getAppliedAt())
                    .isEqualTo(user1.getApplications().get(i).getAppliedAt());
        }


        verify(this.userRepository, times(1)).findById(1L);
    }

    @Test
    void testFindByIdNotFound() {
        given(this.userRepository.findById(1L)).willReturn(Optional.empty());
        Throwable thrown = catchThrowable(() -> this.userService.findById(1L));
        assertThat(thrown).isInstanceOf(ObjectNotFoundException.class);
        assertThat(thrown).hasMessageContaining("Could not find User with Id: 1 :(");
        verify(this.userRepository, times(1)).findById(1L);
    }

    @Test
    void testSaveSuccess() {
        User user1 = new User();
        user1.setFirstName("Andre");
        user1.setLastName("Dabski");
        user1.setEmail("test@gmail.com");
        user1.setPassword("12345");
        user1.setRole("admin");
        user1.setEnabled(true);
        user1.setApplications(this.subData.getListApplications());

        given((User) this.userRepository.save(user1)).willReturn(user1);

        User returnUser = this.userService.save(user1);
        assertThat(returnUser.getId()).isEqualTo(user1.getId());
        assertThat(returnUser.getFirstName()).isEqualTo(user1.getFirstName());
        assertThat(returnUser.getLastName()).isEqualTo(user1.getLastName());
        assertThat(returnUser.getEmail()).isEqualTo(user1.getEmail());
        assertThat(returnUser.getPassword()).isEqualTo(user1.getPassword());
        assertThat(returnUser.getRole()).isEqualTo(user1.getRole());
        assertThat(returnUser.getApplications()).isEqualTo(user1.getApplications());

        verify(this.userRepository, times(1)).save(user1);
    }

    @Test
    void testUdateSuccess() {
        User oldUser = new User();
        oldUser.setId(1L);
        oldUser.setFirstName("Andre");
        oldUser.setLastName("Dabski");
        oldUser.setEmail("test@gmail.com");
        oldUser.setPassword("12345");
        oldUser.setRole("admin");
        oldUser.setEnabled(true);
        oldUser.setApplications(this.subData.getListApplications());


        User update = new User();
        update.setFirstName("Kiki updated");
        update.setLastName("MyKiki updated");
        update.setEmail("test@gmail.com");
        update.setPassword("12345");
        update.setRole("admin");
        update.setEnabled(true);
        update.setApplications(this.subData.getListApplications());

        given(this.userRepository.save(oldUser)).willReturn(oldUser);
        given(this.userRepository.findById(1L)).willReturn(Optional.of(oldUser));

        User updatedUser = this.userService.update(1L, update);

        assertThat(updatedUser.getId()).isEqualTo(1L);
        assertThat(updatedUser.getFirstName()).isEqualTo(update.getFirstName());
        assertThat(updatedUser.getLastName()).isEqualTo(update.getLastName());
        assertThat(updatedUser.getEmail()).isEqualTo(update.getEmail());
        assertThat(updatedUser.getPassword()).isEqualTo(update.getPassword());
        assertThat(updatedUser.getRole()).isEqualTo(update.getRole());
        assertThat(updatedUser.getApplications()).isEqualTo(update.getApplications());

        verify(this.userRepository, times(1)).findById(1L);
        verify(this.userRepository, times(1)).save(oldUser);
    }

    @Test
    void testUpdateNotFoud() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("<EMAIL>");
        user.setPassword("<PASSWORD>");
        user.setRole("admin");
        user.setEnabled(true);
        user.setApplications(this.subData.getListApplications());

        given(this.userRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> this.userService.update(1L, user));
        verify(this.userRepository, times(1)).findById(1L);
    }

    @Test
    void testDeleteSuccess() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("<EMAIL>");
        user.setPassword("<PASSWORD>");
        user.setRole("admin");
        user.setEnabled(true);
        user.setApplications(this.subData.getListApplications());

        given(this.userRepository.findById(Mockito.anyLong())).willReturn(Optional.of(user));
        doNothing().when(this.userRepository).deleteById(1L);

        // When
        this.userService.delete(user.getId());

        // Then
        verify(this.userRepository, times(1)).deleteById(user.getId());

    }

    @Test
    void testDeleteNotFound() {
        // Given
        given(this.userRepository.findById(Mockito.anyLong())).willReturn(Optional.empty());

        // When
        assertThrows(ObjectNotFoundException.class, () -> this.userService.delete(1L));

        // Then
        verify(this.userRepository, times(1)).findById(1L);
    }

    private static List<ApplicationDto> toExpectedDtos(List<Application> apps) {
        return apps.stream().map(a -> {
            ApplicationDto d = new ApplicationDto();
            d.setId(a.getId());
            d.setMotivation(a.getMotivation());
            d.setStatus(a.getStatus() == null ? null : a.getStatus().name());
            d.setAppliedAt(a.getAppliedAt());
            return d;
        }).toList();
    }

}
