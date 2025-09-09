package nl.andarabski.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nl.andarabski.converter.ApplicationDtoToApplicationConverter;
import nl.andarabski.converter.ApplicationToApplicationDtoConverter;
import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.mapper.ApplicationMapper;
import nl.andarabski.model.Application;
import nl.andarabski.model.ApplicationStatus;
import nl.andarabski.model.User;
import nl.andarabski.model.Vacancy;
import nl.andarabski.repository.ApplicationRepository;
import nl.andarabski.repository.UserRepository;
import nl.andarabski.repository.VacancyRepository;
import nl.andarabski.system.exception.ObjectNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final VacancyRepository vacancyRepository;
    private final UserRepository userRepository;
    private final ApplicationMapper applicationMapper;



    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ApplicationDto> findAll() {
        return applicationRepository.findAll()
                .stream()
                .map(applicationMapper::toDto)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ApplicationDto findById(Long applicationId) {
       Application appl = this.applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ObjectNotFoundException("Application", applicationId));
       return applicationMapper.toDto(appl);
    }

    public ApplicationDto create(ApplicationDto in) {

        // 1) Sanity checks
        if (in == null) {
            throw new IllegalArgumentException("ApplicationDto is required");
        }
        if (in.getId() != null) {
            // bij create hoort geen id vanuit de client
            throw new IllegalArgumentException("New applications must not have an id");
        }
        if (in.getUserId() == null || in.getVacancyId() == null) {
            throw new IllegalArgumentException("New applications must not have an id");
        }

        // 2) Scalars mappen via mapper (collecties/applications worden genegeerd in de mapper)
        Application app = applicationMapper.toEntity(in);

        // 3) Applications expliciet omzetten + back-reference zetten
        User userRef = new User();
        userRef.setId(in.getId());
        app.setUser(userRef);

        Vacancy vacancyRef = new Vacancy();
        vacancyRef.setId(in.getVacancyId());
        app.setVacancy(vacancyRef);

        // 3) Defaults/validatie
        if(app.getStatus() == null){
            app.setStatus(ApplicationStatus.valueOf(in.getStatus()));
        }
        if(app.getStatus() == null){
            app.setAppliedAt(Date.from(java.time.Instant.now()));
        }
        Application saved = applicationRepository.save(app);
        return applicationMapper.toDto(saved);
    }

    public ApplicationDto update(Long applicationId, ApplicationDto patch) {
        Application existing = this.applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ObjectNotFoundException("Application is with Id: ", applicationId));
        // 3) Applications expliciet omzetten + back-reference zetten
        if (patch.getUserId() != null) {
            User userRef = new User();
            userRef.setId(patch.getId());
            existing.setUser(userRef);
        }

        if (patch.getVacancyId() != null) {
            Vacancy vacancyRef = new Vacancy();
            vacancyRef.setId(patch.getVacancyId());
            existing.setVacancy(vacancyRef);
        }

        if(patch.getMotivation() != null) {
            existing.setMotivation(patch.getMotivation());
        }

        // 3) Defaults/validatie
        if(patch.getStatus() == null){
            try {
                existing.setStatus(ApplicationStatus.valueOf(patch.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + patch.getStatus());
            }
        }

        if(patch.getAppliedAt() == null){
            existing.setAppliedAt(patch.getAppliedAt());
        } else if (existing.getAppliedAt() == null) {
            existing.setAppliedAt(Date.from(java.time.Instant.now()));
        }

        Application saved = applicationRepository.save(existing);
        return applicationMapper.toDto(saved);

    }

    public void delete(Long applicationId) {
       if(applicationId == null) {
           throw new IllegalArgumentException("ApplicationId is required");
       }
       try {
           applicationRepository.deleteById(applicationId);
       } catch (EmptyResultDataAccessException e) {
           throw new ObjectNotFoundException("Application with Id: " + applicationId + " not found");
       }
    }

    public Application applyToVacancy(Long userId, Long vacancyId, String motivation) {
        // Check of user and vacancy exist
        // Check of user already has applied
        // Bouw nieuwe Application en sla op
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User", userId));
        Vacancy vacancy = this.vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ObjectNotFoundException("Vacancy", vacancyId));

        // Bestaat al?
        if (applicationRepository.existsByUserIdAndVacancyId(userId, vacancyId)) {
            throw new IllegalArgumentException("User already applied to this vacancy");
        }

        // Nieuwe application
        Application application = new Application();
        application.setUser(user);
        application.setVacancy(vacancy);
        application.setMotivation(motivation);
        application.setAppliedAt(java.sql.Date.from(java.time.Instant.now()));
        application.setStatus(ApplicationStatus.PENDING);

        // (optioneel) backrefs bijhouden in-memory
        if (user.getApplications() != null) {
            user.getApplications().add(application);
        }
        if (vacancy.getApplications() != null) {
            vacancy.getApplications().add(application);
        }

        return applicationRepository.save(application);

    }

}
