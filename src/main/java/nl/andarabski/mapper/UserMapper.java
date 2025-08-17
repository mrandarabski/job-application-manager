package nl.andarabski.mapper;

import nl.andarabski.dto.UserDto;
import nl.andarabski.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(UserDto dto);
}
