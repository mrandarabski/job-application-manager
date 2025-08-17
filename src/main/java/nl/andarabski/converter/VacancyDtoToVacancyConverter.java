package nl.andarabski.converter;

import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.dto.VacancyDto;
import nl.andarabski.model.Application;
import nl.andarabski.model.Vacancy;
import nl.andarabski.repository.ApplicationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VacancyDtoToVacancyConverter implements Converter<VacancyDto, Vacancy> {

    private final ModelMapper modelMapper;
    private final ApplicationDtoToApplicationConverter appConverter;

    public VacancyDtoToVacancyConverter(ModelMapper modelMapper, @Lazy ApplicationDtoToApplicationConverter appConverter) {
        this.modelMapper = modelMapper;
        this.appConverter = appConverter;
    }


    @Override
    public Vacancy convert(VacancyDto source) {
        if (source == null) return null;

        // Baisvelden mappem
        Vacancy vacancy = modelMapper.map(source, Vacancy.class);

        // Applications expliciet mappen (null-safe)
        List<ApplicationDto> appsDto = source.getApplications();
        if (appsDto != null && !appsDto.isEmpty()) {
            List<Application> apps = appsDto.stream()
                    .map(appConverter::convert)
                    .toList();
            vacancy.setApplications(apps);

            // (optioneel) back-reference zetten als je bidirectioneel model gebruikt
            // apps.forEach(a -> a.setVacancy(vacancy));
        } else {
            vacancy.setApplications(List.of());
        }
        return vacancy;
    }
}

    /*private final ApplicationDtoToApplicationConverter applicationConverter;
    private final ApplicationRepository applicationRepository;

    @Autowired
    public VacancyDtoToVacancyConverter(
            @Lazy ApplicationDtoToApplicationConverter applicationConverter,
            ApplicationRepository applicationRepository
    ) {
        this.applicationConverter = applicationConverter;
        this.applicationRepository = applicationRepository;
    }

    @Override
    public Vacancy convert(VacancyDto vacancyDto) {
        Vacancy vacancy = new Vacancy();

        vacancy.setId(vacancyDto.getId());
        vacancy.setTitle(vacancyDto.getTitle());
        vacancy.setCompanyName(vacancyDto.getCompanyName());
        vacancy.setDescription(vacancyDto.getDescription());
        vacancy.setLocation(vacancyDto.getLocation());
        vacancy.setPostedAt(vacancyDto.getPostedAt());

        // Ensure Applications are managed entities
        if (vacancyDto.getApplications() != null) {
            List<Application> applications = vacancyDto.getApplications().stream()
                    .map(dto -> applicationRepository.findById(dto.getId())
                            .orElseThrow(() -> new RuntimeException("Application not found with id: " + dto.getId())))
                    .collect(Collectors.toList());
            vacancy.setApplications(applications);
        }

        return vacancy;
    }*/
//}

