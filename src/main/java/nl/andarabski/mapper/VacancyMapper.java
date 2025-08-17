package nl.andarabski.mapper;

import nl.andarabski.dto.VacancyDto;
import nl.andarabski.model.Vacancy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VacancyMapper {
    VacancyDto toDto(Vacancy vacancy);
    Vacancy toEntity(VacancyDto dto);
}