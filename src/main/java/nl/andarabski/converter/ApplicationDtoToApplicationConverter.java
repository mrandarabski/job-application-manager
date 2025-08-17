package nl.andarabski.converter;

import nl.andarabski.dto.ApplicationDto;

import nl.andarabski.model.Application;
import nl.andarabski.model.ApplicationStatus;
import nl.andarabski.repository.UserRepository;
import nl.andarabski.repository.VacancyRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;



@Component
public class ApplicationDtoToApplicationConverter implements Converter<ApplicationDto, Application> {

    private final UserRepository userRepository;
    private final VacancyRepository vacancyRepository;

    @Autowired
    public ApplicationDtoToApplicationConverter(UserRepository userRepository, VacancyRepository vacancyRepository) {
        this.userRepository = userRepository;
        this.vacancyRepository = vacancyRepository;
    }

    @Override
    public Application convert(ApplicationDto dto) {
        Application app = new Application();

        app.setId(dto.getId());
        app.setMotivation(dto.getMotivation());
        app.setStatus(ApplicationStatus.PENDING);
        app.setAppliedAt(dto.getAppliedAt());

        // Haal alleen referenties op via ID (lazy loading)
        if (dto.getUserId() != null) {
            app.setUser(userRepository.findById(dto.getUserId()).orElse(null));
        }

        if (dto.getVacancyId() != null) {
            app.setVacancy(vacancyRepository.findById(dto.getVacancyId()).orElse(null));
        }

        return app;
    }
}

/*
@Component
public class ApplicationDtoToApplicationConverter implements Converter<ApplicationDto, Application> {

    private final ModelMapper modelMapper;

    public ApplicationDtoToApplicationConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public Application convert(ApplicationDto source) {

        return modelMapper.map(source, Application.class);
    }
}*/
