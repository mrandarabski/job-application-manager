package nl.andarabski.converter;

import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.model.Application;
import nl.andarabski.model.ApplicationStatus;
import org.modelmapper.ModelMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


@Component
public class ApplicationToApplicationDtoConverter implements Converter<Application, ApplicationDto> {

    @Override
    public ApplicationDto convert(Application app) {
        ApplicationDto dto = new ApplicationDto();

        dto.setId(app.getId());
        dto.setUserId(app.getUser() != null ? app.getUser().getId() : null);
        dto.setVacancyId(app.getVacancy() != null ? app.getVacancy().getId() : null);
        dto.setMotivation(app.getMotivation());
        //dto.setStatus(String.valueOf(ApplicationStatus.PENDING));
        dto.setStatus(app.getStatus() == null ? null : app.getStatus().name()); // <-- belangrijk
        dto.setAppliedAt(app.getAppliedAt());

        return dto;
    }
}
/*@Component
public class ApplicationToApplicationDtoConverter implements Converter<Application, ApplicationDto> {

    private final ModelMapper modelMapper;

    public ApplicationToApplicationDtoConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public ApplicationDto convert(Application source) {
       return modelMapper.map(source, ApplicationDto.class);
    }
}*/
