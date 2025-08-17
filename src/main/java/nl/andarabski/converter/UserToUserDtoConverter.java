package nl.andarabski.converter;

import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.dto.UserDto;
import nl.andarabski.model.Application;
import nl.andarabski.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class UserToUserDtoConverter implements Converter<User, UserDto> {

    private final ModelMapper modelMapper;
    private final ApplicationToApplicationDtoConverter appConverter;

    public UserToUserDtoConverter(ModelMapper modelMapper,
                                  @Lazy ApplicationToApplicationDtoConverter appConverter) {
        this.modelMapper = modelMapper;
        this.appConverter = appConverter;
    }

    @Override
    public UserDto convert(User source) {
        if (source == null) return null;

        // Basisvelden mappen
        UserDto dto = modelMapper.map(source, UserDto.class);

        // Applications expliciet mappen (null-safe + deterministische volgorde)
        List<Application> apps = source.getApplications();
        if (apps != null && !apps.isEmpty()) {
            List<ApplicationDto> appDtos = apps.stream()
                    .sorted(Comparator
                            .comparing(Application::getAppliedAt, Comparator.nullsLast(java.util.Date::compareTo))
                            .thenComparing(Application::getId, Comparator.nullsLast(Long::compareTo)))
                    .map(appConverter::convert)
                    .toList();

            dto.setApplications(appDtos);
        } else {
            dto.setApplications(List.of());
        }

        return dto;
    }
}
