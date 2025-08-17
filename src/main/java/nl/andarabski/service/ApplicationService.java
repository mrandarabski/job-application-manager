package nl.andarabski.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
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

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final VacancyRepository vacancyRepository;
    private final UserRepository userRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public ApplicationService(ApplicationRepository applicationRepository, VacancyRepository vacancyRepository, UserRepository userRepository) {
        this.applicationRepository = applicationRepository;

        this.vacancyRepository = vacancyRepository;
        this.userRepository = userRepository;
    }

    public List<Application> findAll() {
        return applicationRepository.findAll();
    }

    public Application findById(Long applicationId) {
        return this.applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ObjectNotFoundException("Application", applicationId));
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
        // Check of user en vacancy bestaan
        // Check of user al gesolliciteerd heeft
        // Bouw nieuwe Application en sla op
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User", userId));
        Vacancy vacancy = this.vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ObjectNotFoundException("Vacancy", vacancyId));

        // Check of deze user al gesolliciteerd heeft op deze vacature
        System.out.println("DEBUG: aantal sollicitaties gevonden: " + applicationRepository.findAll().size());
        boolean alreadyApplied = applicationRepository
                .findAll()
                .stream()
                .anyMatch(app -> app.getUser().getId().equals(userId)
                                        && app.getVacancy().getId().equals(vacancyId));

        if (alreadyApplied) {
            throw new IllegalArgumentException("Vacancy already applied to this vacancy");
        }

        Application application = new Application();
        application.setUser(user);
        user.getApplications().add(application);
        vacancy.getApplications().add(application);
        application.setVacancy(vacancy);
        application.setMotivation(motivation);
        application.setAppliedAt(new Date());
        application.setStatus(ApplicationStatus.APPLIED);
        application.setMotivation("Your application has been rejected.");
        return this.applicationRepository.save(application);

    }
}
