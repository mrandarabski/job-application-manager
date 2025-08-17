package nl.andarabski.converter;

import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.dto.VacancyDto;
import nl.andarabski.model.Application;
import nl.andarabski.model.Vacancy;

import nl.andarabski.repository.VacancyRepository;
import org.modelmapper.ModelMapper;

import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
public class VacancyToVacancyDtoConverter implements Converter<Vacancy, VacancyDto> {

    private final ModelMapper modelMapper;
    private final ApplicationToApplicationDtoConverter applicationConverter;

    public VacancyToVacancyDtoConverter(ModelMapper modelMapper,
                                        ApplicationToApplicationDtoConverter applicationConverter) {
        this.modelMapper = modelMapper;
        this.applicationConverter = applicationConverter;
    }

    @Override
    public VacancyDto convert(Vacancy source) {
        if (source == null) return null;

        // Basisvelden (id, title, description, etc.)
        VacancyDto dto = modelMapper.map(source, VacancyDto.class);

        // Applications mappen met de JUISTE converter
        List<ApplicationDto> apps = (source.getApplications() == null)
                ? Collections.emptyList()
                : source.getApplications()
                .stream()
                .map(applicationConverter::convert)   // <-- geen Vacancy-converter!
                .collect(Collectors.toList());

        dto.setApplications(apps);
        return dto;
    }
}


/*    private final ApplicationToApplicationDtoConverter applicationConverter;

    @Autowired
    public VacancyToVacancyDtoConverter(@Lazy ApplicationToApplicationDtoConverter applicationConverter) {
        this.applicationConverter = applicationConverter;
    }

    @Override
    public VacancyDto convert(Vacancy vacancy) {
        VacancyDto dto = new VacancyDto();

        dto.setId(vacancy.getId());
        dto.setTitle(vacancy.getTitle());
        dto.setCompanyName(vacancy.getCompanyName());
        dto.setDescription(vacancy.getDescription());
        dto.setLocation(vacancy.getLocation());
        dto.setPostedAt(vacancy.getPostedAt());

        if (vacancy.getApplications() != null) {
            List<ApplicationDto> applicationDtos = vacancy.getApplications().stream()
                    .map(applicationConverter::convert)
                    .collect(Collectors.toList());
            dto.setApplications(applicationDtos);
        }

        return dto;
    }
}*/


