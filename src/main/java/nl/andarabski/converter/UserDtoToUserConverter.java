package nl.andarabski.converter;

import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.dto.UserDto;
import nl.andarabski.model.Application;
import nl.andarabski.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDtoToUserConverter implements Converter<UserDto, User> {

    private final ModelMapper modelMapper;
    private final ApplicationDtoToApplicationConverter appConverter;

    public UserDtoToUserConverter(ModelMapper modelMapper, ApplicationDtoToApplicationConverter appConverter) {
        this.modelMapper = modelMapper;
        this.appConverter = appConverter;
    }

    @Override
    public User convert(UserDto source) {
        if (source == null) return null;

        // Basisvelden mappen
        User user = modelMapper.map(source, User.class);

        // Applications expliciet mappen (null-safe)
        List<ApplicationDto> appDtos = source.getApplications();
        if (appDtos != null && !appDtos.isEmpty()) {
            List<Application> apps = appDtos.stream()
                    .map(appConverter::convert)
                    .toList();
            user.setApplications(apps);

            // (optioneel) back-reference zetten als je bidirectioneel model gebruikt
            // apps.forEach(a -> a.setUser(user));
        } else {
            user.setApplications(List.of());
        }

        return user;
    }
}
