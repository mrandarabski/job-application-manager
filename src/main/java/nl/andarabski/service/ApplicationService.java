package nl.andarabski.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
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
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final VacancyRepository vacancyRepository;
    private final UserRepository userRepository;
    private final ApplicationToApplicationDtoConverter toApplicationDtoConverter;
    private final ApplicationDtoToApplicationConverter dtoToApplicationConverter;

    @PersistenceContext
    private EntityManager entityManager;

    public ApplicationService(ApplicationRepository applicationRepository, VacancyRepository vacancyRepository, UserRepository userRepository, ApplicationToApplicationDtoConverter toApplicationDtoConverter, ApplicationDtoToApplicationConverter dtoToApplicationConverter) {
        this.applicationRepository = applicationRepository;

        this.vacancyRepository = vacancyRepository;
        this.userRepository = userRepository;
        this.toApplicationDtoConverter = toApplicationDtoConverter;
        this.dtoToApplicationConverter = dtoToApplicationConverter;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ApplicationDto> findAll() {
        return applicationRepository.findAll().stream().map(toApplicationDtoConverter::convert).collect(Collectors.toList());
    }

    public ApplicationDto findById(Long applicationId) {
       Application appl = this.applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ObjectNotFoundException("Application", applicationId));
       return toApplicationDtoConverter.convert(appl);
    }

    public Application save(Application application) {
        return this.applicationRepository.save(application);
    }

    public Application update(Long applicationId, Application application) {
        Application existing = this.applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ObjectNotFoundException("Application is with Id: ", applicationId));
        existing.setUser(application.getUser());
        existing.setVacancy(application.getVacancy());
        existing.setMotivation(application.getMotivation());
        existing.setStatus(application.getStatus());
        existing.setAppliedAt(application.getAppliedAt());
        return this.applicationRepository.save(existing);

    }

    public void delete(Long applicationId) {
      this.applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ObjectNotFoundException("Application", applicationId));
       this.applicationRepository.deleteById(applicationId);
    }

    public Application applyToVacancy(Long userId, Long vacancyId, String motivation) {
        // Check of user and vacancy exist
        // Check of user already has applied
        // Bouw nieuwe Application en sla op
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User", userId));
        Vacancy vacancy = this.vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ObjectNotFoundException("Vacancy", vacancyId));

        // Check if this user has already applied for this vacancy
        var apps = applicationRepository.findAll();
        System.out.println("DEBUG: aantal sollicitaties gevonden: " + applicationRepository.findAll().size());
        boolean alreadyApplied = apps
                .stream()
                .anyMatch(app -> Objects.equals( app.getUser().getId(), userId)
                                        && Objects.equals(app.getVacancy().getId(), vacancyId));

        if (alreadyApplied) {
            throw new IllegalArgumentException("Vacancy already applied to this vacancy");
        }

        Application application = new Application();
        application.setUser(user);
        application.setVacancy(vacancy);
        application.setMotivation(motivation);
        application.setAppliedAt(new Date());
        application.setStatus(ApplicationStatus.PENDING);
        // update bidirectional collections (null-safe)
        if (user.getApplications() != null) {
            user.getApplications().add(application);
        }
        if (vacancy.getApplications() != null) {
            vacancy.getApplications().add(application);
        }

        return this.applicationRepository.save(application);

    }

}
