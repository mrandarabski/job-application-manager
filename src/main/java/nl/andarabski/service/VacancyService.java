package nl.andarabski.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nl.andarabski.converter.ApplicationToApplicationDtoConverter;
import nl.andarabski.converter.VacancyDtoToVacancyConverter;
import nl.andarabski.converter.VacancyToVacancyDtoConverter;
import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.dto.VacancyDto;
import nl.andarabski.mapper.VacancyMapper;
import nl.andarabski.model.Application;
import nl.andarabski.model.ApplicationStatus;
import nl.andarabski.model.User;
import nl.andarabski.model.Vacancy;
import nl.andarabski.repository.ApplicationRepository;
import nl.andarabski.repository.VacancyRepository;
import nl.andarabski.system.exception.ObjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/vacancies")
@Transactional
public class VacancyService {


    private final VacancyRepository vacancyRepository;
    private final ApplicationRepository applicationRepository;
    private final VacancyMapper vacancyMapper;


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<VacancyDto> findAll(){
        return vacancyRepository.findAll().stream().map(vacancyMapper::toDto).toList();
    }

    public VacancyDto findById(Long vacancyId){
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ObjectNotFoundException("Vacancy", vacancyId));
        return vacancyMapper.toDto(vacancy);
    }

//    // NIEUW: pure DTO -> DTO voor de controller
//    public VacancyDto create(VacancyDto dto) {
//        Vacancy entity = vacancyMapper.toEntity(dto);
//        Vacancy saved  = vacancyRepository.save(entity);          // je bestaande business-logica
//        return vacancyMapper.toDto(saved);                    // gebruikt toVacancyDtoConverter + appConverter
//    }

    @Transactional
    public VacancyDto create(VacancyDto in) {
        // 1) Sanity checks
        if(in == null) {
            throw new IllegalArgumentException("VacancyDto must not be null");
        }
        if (in.getId() != null) {
            // bij create hoort geen id vanuit de client
            throw new IllegalArgumentException("New vacancies must not have an ID");
        }
        // (optioneel) businessregel: unieke email
        // if (userRepository.existsByEmail(in.getEmail())) {
        //     throw new BusinessRuleException("Email already in use");
        // }

        // 2) Scalars mappen via mapper (collecties/applications worden genegeerd in de mapper)
        Vacancy vacancy = vacancyMapper.toEntity(in);

        // 3) Applications expliciet omzetten + back-reference zetten
        if(in.getApplications() != null && !in.getApplications().isEmpty()) {
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
                        if(d.getUserId() != null) {
                            User u = new User();
                            u.setId(d.getUserId());
                            a.setUser(u);
                        }

                        // Belangrijk : back-reference naar de Vacancy
                        a.setVacancy(vacancy);
                        return a;

                    })
                    .collect(Collectors.toList());
            vacancy.setApplications(apps);
        } else {
            // maak expliciet leeg voor voorspelbaar gedrag (null ≠ leeg)
            vacancy.setApplications(List.of());
        }
        // 4) Persist & terug naar DTO
        Vacancy saved = vacancyRepository.save(vacancy);
        return vacancyMapper.toDto(saved);
    }

    @Transactional
    public VacancyDto update(Long id, VacancyDto patch) {
        Vacancy existing = vacancyRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("Vacancy", id));
        existing.setTitle(patch.getTitle());
        existing.setCompanyName(patch.getCompanyName());
        existing.setDescription(patch.getDescription());
        existing.setLocation(patch.getLocation());

        // Gebruik de mapper voor partial update (IGNORE nulls)
        vacancyMapper.update(existing, patch);

        // Optioneel: speciale handling voor applications (als je dat bewust buiten de mapper wilt doen)
        if (patch.getApplications() != null) {
            List<Application> apps = patch.getApplications().stream()
                    .map(d -> {
                        Application a = new Application();
                        a.setId(d.getId());
                        a.setMotivation(d.getMotivation());
                        a.setStatus(ApplicationStatus.valueOf(d.getStatus()));

                        User u = new User();
                        u.setId(d.getUserId());
                        a.setUser(u);
                        a.setVacancy(existing);
                        return a;
                    })
                    .collect(Collectors.toList());
            existing.setApplications(apps);
        }
        Vacancy saved = vacancyRepository.save(existing);
        return vacancyMapper.toDto(saved);
    }

    public void delete(Long vacancyId){
        Vacancy vacancyToBeDeleted = this.vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ObjectNotFoundException("Vacancy", vacancyId));
        vacancyToBeDeleted.removeAllApplications();
        this.vacancyRepository.delete(vacancyToBeDeleted);
    }



}
