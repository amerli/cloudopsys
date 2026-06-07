package hu.agilexpert.core.mapper;

import hu.agilexpert.core.dto.UserDto;
import hu.agilexpert.core.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "activeIcon.id", target = "activeIconId")
    @Mapping(source = "activeBackground.id", target = "activeBackgroundId")
    UserDto toDto(User user);
}
