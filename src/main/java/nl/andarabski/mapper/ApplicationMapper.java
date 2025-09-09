package nl.andarabski.mapper;

import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.model.Application;
import nl.andarabski.model.ApplicationStatus;
import org.mapstruct.*;
import static org.mapstruct.ReportingPolicy.ERROR;

@Mapper(
        componentModel = "spring",
        uses = {},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ERROR
)
public interface ApplicationMapper {

    // entity -> dto
    @Mapping(source = "user.id",    target = "userId")
    @Mapping(source = "vacancy.id", target = "vacancyId")
    @Mapping(target = "status", expression =
            "java(application.getStatus() != null ? application.getStatus().name() : null)")
    ApplicationDto toDto(Application application);

    // dto -> entity (relaties in service zetten)
    @Mapping(target = "user",    ignore = true)
    @Mapping(target = "vacancy", ignore = true)
    @Mapping(source = "status", target = "status")
    Application toEntity(ApplicationDto dto);

    // patch: alleen niet-null velden kopiëren
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user",    ignore = true)
    @Mapping(target = "vacancy", ignore = true)
    @Mapping(target = "status", ignore = true) // status apart doen, zie @AfterMapping
    void update(@MappingTarget Application target, ApplicationDto patch);

    @AfterMapping
    default void patchStatus(ApplicationDto patch, @MappingTarget Application target) {
        if (patch.getStatus() != null) {
            target.setStatus(ApplicationStatus.valueOf(patch.getStatus()));
        }
    }
}
