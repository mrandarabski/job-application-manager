package nl.andarabski.service;

import nl.andarabski.converter.ApplicationToApplicationDtoConverter;
import nl.andarabski.converter.UserDtoToUserConverter;
import nl.andarabski.converter.UserToUserDtoConverter;
import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.dto.UserDto;
import nl.andarabski.model.Application;
import nl.andarabski.model.User;
import nl.andarabski.repository.ApplicationRepository;
import nl.andarabski.repository.UserRepository;
import nl.andarabski.system.exception.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;

@Service
@jakarta.transaction.Transactional
public class UserService {

    @Autowired
    private ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final UserToUserDtoConverter userConverter;                 // <— jouw bestaande converter
    private final UserDtoToUserConverter userDtoConverter;
    private final ApplicationToApplicationDtoConverter appConverter;    // <— converter voor Application -> ApplicationDto


    public UserService(UserRepository userRepository, UserToUserDtoConverter userConverter, UserDtoToUserConverter userDtoConverter,
                       ApplicationToApplicationDtoConverter appConverter) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
        this.userDtoConverter = userDtoConverter;
        this.appConverter = appConverter;
    }
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("User", id));
        return toDto(user);
    }

    public User save(User user) {
        List<Application> managedApplications = new ArrayList<>();
        if (user.getApplications() != null) {
            for (Application app : user.getApplications()) {
                if (app.getId() != null) {
                    // Haal beheerde (attached) instantie op
                    Application managedApp = applicationRepository.findById(app.getId())
                            .orElseThrow(() -> new RuntimeException("Application not found with id " + app.getId()));
                    managedApp.setUser(user); // belangrijk!
                    managedApplications.add(managedApp);
                }
            }
            user.setApplications(managedApplications);
        }
        return this.userRepository.save(user);
    }

    public User update(Long userId, User user){
        return this.userRepository.findById(userId)
                .map(oldUser -> {
                    oldUser.setFirstName(user.getFirstName());
                    oldUser.setLastName(user.getLastName());
                    oldUser.setEmail(user.getEmail());
                   // oldUser.setPassword(user.getPassword());
                    oldUser.setAge(user.getAge());
                    oldUser.setPhoto(user.getPhoto());
                    oldUser.setCv(user.getCv());
                    oldUser.setRole(user.getRole());
                    oldUser.setEnabled(user.isEnabled());
                    oldUser.setApplications(user.getApplications());
                    return this.userRepository.save(oldUser);
                })
                .orElseThrow(() -> new ObjectNotFoundException("User", userId));
    }

    /*@Transactional
    public UserDto update(Long userId, UserDto patch) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User", userId));

        userMapper.updateEntityFromDto(patch, user); // nulls worden genegeerd
        // let op: als applications bijgewerkt worden, bepaal je hier je strategie:
        // - ids resolven naar bestaande Application entities, of
        // - overschrijven. Kies wat je domein nodig heeft.

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }*/


    public void delete(Long userId) {
        User userToBeDeleted = this.userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User", userId));
        userToBeDeleted.removeAllApplications();
        this.userRepository.deleteById(userId);
    }

    private UserDto toDto(User user) {
        // Fail-fast als iemand de service zonder converter probeert te gebruiken
        Objects.requireNonNull(userConverter, "userConverter is not injected");
        Objects.requireNonNull(appConverter,   "appConverter is not injected");

        UserDto dto = userConverter.convert(user);

        List<ApplicationDto> appDtos = Optional.ofNullable(user.getApplications())
                .orElseGet(List::of)
                .stream()
                .sorted(
                        Comparator.comparing(
                                        Application::getAppliedAt,
                                        Comparator.nullsLast(Comparator.naturalOrder())) // duidelijker dan Date::compareTo
                                .thenComparing(
                                        Application::getId,
                                        Comparator.nullsLast(Comparator.naturalOrder()))  // duidelijker dan Long::compareTo
                )
                .map(appConverter::convert)
                .filter(Objects::nonNull) // voorkom null-elementen als de converter ooit null teruggeeft
                .toList();

        dto.setApplications(appDtos);
        return dto;
    }


   /* private UserDto toDto(User user) {
        UserDto dto = userConverter.convert(user);
        var apps = user.getApplications();
        if (apps == null) { dto.setApplications(List.of()); return dto; }
        var byAppliedAt = Comparator.comparing(Application::getAppliedAt, Comparator.nullsLast(Date::compareTo));
        var byId       = Comparator.comparing(Application::getId,        Comparator.nullsLast(Long::compareTo));
        List<ApplicationDto> appDtos = apps.stream()
                .sorted(byAppliedAt.thenComparing(byId))
                .map(appConverter::convert)
                .toList();
        dto.setApplications(appDtos);
        return dto;
    }*/

    // ===== helper =====
   /* private UserDto toDto(User user) {
        // Start met je bestaande userConverter (of handmatig mappen als je die niet gebruikt)
        UserDto dto = userConverter.convert(user);

        // Null-safe lijst ophalen
        List<Application> apps = user.getApplications() == null
                ? Collections.emptyList()
                : user.getApplications();

        // Sorteer deterministisch (appliedAt → id als tie-breaker), nulls achteraan
        Comparator<Application> byAppliedAt =
                Comparator.comparing(
                        Application::getAppliedAt,
                        Comparator.nullsLast(Date::compareTo)
                );
        Comparator<Application> byId =
                Comparator.comparing(
                        Application::getId,
                        Comparator.nullsLast(Long::compareTo)
                );

        List<Application> sorted = apps.stream()
                .sorted(byAppliedAt.thenComparing(byId))
                .toList();

        // Map daarna pas naar DTO’s
        List<ApplicationDto> appDtos = sorted.stream()
                .map(appConverter::convert)
                .toList();

        dto.setApplications(appDtos);
        return dto;
    }
*/
}
