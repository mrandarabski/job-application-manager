package nl.andarabski.mapper;

import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.model.Application;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {
    ApplicationDto toDto(Application app);
    Application toEntity(ApplicationDto dto);
}