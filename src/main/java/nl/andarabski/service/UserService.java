package nl.andarabski.service;

import lombok.RequiredArgsConstructor;
import nl.andarabski.dto.UserDto;
import nl.andarabski.mapper.UserMapper;
import nl.andarabski.model.Application;
import nl.andarabski.model.ApplicationStatus;
import nl.andarabski.model.User;
import nl.andarabski.model.Vacancy;
import nl.andarabski.repository.ApplicationRepository;
import nl.andarabski.repository.UserRepository;
import nl.andarabski.system.exception.ObjectNotFoundException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.util.*;

@Service
@RequiredArgsConstructor
@jakarta.transaction.Transactional
public class UserService {


    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto create(UserDto in, @Nullable MultipartFile photo, @Nullable MultipartFile cv) {
        // 1) Sanity checks
        if (in == null) {
            throw new IllegalArgumentException("UserDto is required");
        }
        if (in.getId() != null) {
            // bij create hoort geen id vanuit de client
            throw new IllegalArgumentException("New users must not have an id");
        }

        // (optioneel) businessregel: unieke email
        // if (userRepository.existsByEmail(in.getEmail())) {
        //     throw new BusinessRuleException("Email already in use");
        // }

        // 2) Scalars mappen via mapper (collecties/applications worden genegeerd in de mapper)
        User user = userMapper.toEntity(in);

        // 3) Applications expliciet omzetten + back-reference zetten
        if (in.getApplications() != null && !in.getApplications().isEmpty()) {
            List<Application> apps = in.getApplications().stream()
                    .map(d -> {
                        // toegestaan: id null (nieuwe application); als id niet null is, kun je kiezen:
                        //  - negeren (id op null), of
                        //  - weigeren met een fout. Hieronder negeren we 'm.
                        Application a = new Application();
                        a.setMotivation(d.getMotivation());

                        // defensief parsen van status
                        if (d.getStatus() != null) {
                            a.setStatus(ApplicationStatus.valueOf(d.getStatus()));
                        }

                        // reference entity voor Vacancy (geen DB-call nodig)
                        if (d.getVacancyId() != null) {
                            Vacancy v = new Vacancy();
                            v.setId(d.getVacancyId());
                            a.setVacancy(v);
                        }

                        // BELANGRIJK: back-reference naar de user
                        a.setUser(user);
                        return a;
                    })
                    .toList();

            user.setApplications(apps);
        } else {
            // maak expliciet leeg voor voorspelbaar gedrag (null ≠ leeg)
            user.setApplications(List.of());
        }

        // 4) Persist & terug naar DTO
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }


    @Transactional
    public UserDto update(Long id, UserDto patch, @Nullable MultipartFile photo, @Nullable MultipartFile cv) {
        User existing = userRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("User", id)); // scalars
        existing.setFirstName(patch.getFirstName());
        existing.setLastName(patch.getLastName());
        existing.setEmail(patch.getEmail());
        existing.setPassword(patch.getPassword());
        existing.setAge(patch.getAge());
        existing.setPhoto(patch.getPhoto());
        existing.setCv(patch.getCv());
        existing.setRole(patch.getRole());
        existing.setEnabled(Boolean.TRUE.equals(patch.isEnabled()));
        // applications uit patch -> entity + back-reference
        if (patch.getApplications() != null) { List<Application> apps = patch.getApplications().stream()
                .map(d -> {
                    Application a = new Application();
                    a.setId(d.getId());
                    a.setMotivation(d.getMotivation());
                    a.setStatus(ApplicationStatus.valueOf(d.getStatus()));

                    Vacancy v = new Vacancy(); // geen DB call nodig voor de test
                     v.setId(d.getVacancyId());
                     a.setVacancy(v);
                     a.setUser(existing); // BELANGRIJK: back-reference
                     return a; })
                .toList();
            existing.setApplications(apps);
        }
        User saved = userRepository.save(existing);
        return userMapper.toDto(saved);
    }


    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        // Tip: als je mapper LAZY-collecties (applications) aanraakt:
        // overweeg een fetch-join repo-methode (zie verderop).
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("User", id));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        // Prima voor kleine datasets. Voor productie: overweeg paginatie + fetch-join of projecties.
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional
    public void delete(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User", userId));

        // 3 robuuste opties — kies er één:

        // (A) Als je in User -> applications hebt: cascade = CascadeType.ALL en orphanRemoval = true:
        // dan hoef je niets te doen, enkel delete(user).
        // userRepository.delete(user);

        // (B) Zonder orphanRemoval maar met helper:
        user.removeAllApplications();     // zorgt ook voor back-references wegzetten
        userRepository.delete(user);      // delete de geladen (managed) entity i.p.v. deleteById

        // (C) Of expliciet via repo (als je FK-constraints wilt vermijden zonder entity-graph):
        // applicationRepository.deleteAllByUserId(userId);
        // userRepository.deleteById(userId);
    }



}
