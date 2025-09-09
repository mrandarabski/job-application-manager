package nl.andarabski.mapper;

import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.dto.VacancyDto;
import nl.andarabski.model.Vacancy;
import org.mapstruct.*;

import static java.util.Comparator.naturalOrder;

@Mapper(
        componentModel = "spring",
        uses = ApplicationMapper.class,  // idem: voor collection Vacancy.applications
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface VacancyMapper {
    VacancyDto toDto(Vacancy vacancy);

    @Mapping(target = "applications", ignore = true)
    Vacancy toEntity(VacancyDto dto);

    // patch: kopieer alleen niet-null velden uit dto naar entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Vacancy target, VacancyDto patch);

}