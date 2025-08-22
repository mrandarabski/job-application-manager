package nl.andarabski.service;

import jakarta.transaction.Transactional;
import nl.andarabski.converter.ApplicationToApplicationDtoConverter;
import nl.andarabski.converter.VacancyToVacancyDtoConverter;
import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.dto.VacancyDto;
import nl.andarabski.mapper.VacancyMapper;
import nl.andarabski.model.Application;
import nl.andarabski.model.Vacancy;
import nl.andarabski.repository.ApplicationRepository;
import nl.andarabski.repository.VacancyRepository;
import nl.andarabski.system.exception.ObjectNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
public class VacancyService {

    private final UserService userService;
    private final ApplicationService applicationService;
    private final VacancyRepository vacancyRepository;
    private final ApplicationRepository applicationRepository;
    private final VacancyToVacancyDtoConverter toVacancyDtoConverter;
    private final ApplicationToApplicationDtoConverter appConverter;

    public VacancyService(UserService userService, ApplicationService applicationService,
                          VacancyRepository vacancyRepository, ApplicationRepository applicationRepository, VacancyMapper vacancyMapper, VacancyToVacancyDtoConverter toVacancyDtoConverter, ApplicationToApplicationDtoConverter appConverter) {
        this.userService = userService;
        this.applicationService = applicationService;
        this.vacancyRepository = vacancyRepository;
        this.applicationRepository = applicationRepository;
        this.toVacancyDtoConverter = toVacancyDtoConverter;
        this.appConverter = appConverter;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<VacancyDto> findAll(){
        return vacancyRepository.findAll().stream().map(this::toDto).toList();
    }

    public VacancyDto findById(Long vacancyId){
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ObjectNotFoundException("Vacancy", vacancyId));
        return toDto(vacancy);
    }

    public Vacancy save(Vacancy vacancy){
        List<Application> managedApplications = new ArrayList<>();
        if(vacancy.getApplications() != null) {
            for(Application app : vacancy.getApplications()) {
               if (app.getId() != null) {
                   Application managedApp = applicationRepository.findById(app.getId())
                           .orElseThrow(() -> new ObjectNotFoundException("Application not foun with Id", app.getId()));
                   managedApp.setVacancy(vacancy);
                   managedApplications.add(managedApp);
               }
            }
        }
        return vacancyRepository.save(vacancy);
    }

    public Vacancy update(Long vacancyId, Vacancy createNewVacancy){
        return this.vacancyRepository.findById(vacancyId)
                .map(oldVacancy -> {
                    oldVacancy.setTitle(createNewVacancy.getTitle());
                    oldVacancy.setCompanyName(createNewVacancy.getCompanyName());
                    oldVacancy.setDescription(createNewVacancy.getDescription());
                    oldVacancy.setLocation(createNewVacancy.getLocation());
                    oldVacancy.setPostedAt(createNewVacancy.getPostedAt());
                    oldVacancy.setApplications(createNewVacancy.getApplications());
                    return vacancyRepository.save(oldVacancy);
                })
                .orElseThrow(() -> new ObjectNotFoundException("Vacancy", vacancyId));
    }

    public void delete(Long vacancyId){
        Vacancy vacancyToBeDeleted = this.vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ObjectNotFoundException("Vacancy", vacancyId));
        vacancyToBeDeleted.removeAllApplications();
        this.vacancyRepository.deleteById(vacancyId);
    }

    private VacancyDto toDto(Vacancy vacancy){
        // Fail-fast als iemand de service zonder converter probeert te gebruiken
        Objects.requireNonNull(toVacancyDtoConverter, "toVacancyDtoConverter is not injected");
        Objects.requireNonNull(appConverter,   "appConverter is not injected");

        VacancyDto dto = toVacancyDtoConverter.convert(vacancy);

        List<ApplicationDto> appDtos = Optional.ofNullable(vacancy.getApplications())
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

    /*
    private VacancyDto toDto(Vacancy vacancy){
        VacancyDto dto = vacancyToVacancyDtoConverter.convert(vacancy);
        var apps = vacancy.getApplications();
        if(apps != null) {
            dto.setApplications(List.of());
            return dto;
        }
        var byAppliedAt = Comparator.comparing(Application::getAppliedAt, Comparator.nullsLast(Date::compareTo));
        var byId       = Comparator.comparing(Application::getId,        Comparator.nullsLast(Long::compareTo));
        List<ApplicationDto> appDtos = apps.stream()
                .sorted(byAppliedAt.thenComparing(byId))
                .map(appConverter::convert)
                .collect(Collectors.toList());
        dto.setApplications(appDtos);
        return dto;

    }
     */

}
