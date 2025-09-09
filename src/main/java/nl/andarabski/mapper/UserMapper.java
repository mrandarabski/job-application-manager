package nl.andarabski.mapper;

import nl.andarabski.dto.UserDto;
import nl.andarabski.model.User;
import org.mapstruct.*;


@Mapper(
        componentModel = "spring",
        uses = ApplicationMapper.class,  // belangrijk voor mapping van applications -> ApplicationDto
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserMapper {
    UserDto toDto(User user);

    @Mapping(target = "applications", ignore = true)
    User toEntity(UserDto dto);

    // patch: kopieer alleen niet-null velden uit dta naar entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "applications", ignore = true)
    void update(@MappingTarget User target, UserDto patch);

}
