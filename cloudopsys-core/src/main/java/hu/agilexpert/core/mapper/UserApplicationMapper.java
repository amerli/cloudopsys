package hu.agilexpert.core.mapper;

import hu.agilexpert.core.dto.UserApplicationDto;
import hu.agilexpert.core.model.UserApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ApplicationMapper.class)
public interface UserApplicationMapper {

    @Mapping(source = "user.id", target = "userId")
    UserApplicationDto toDto(UserApplication userApplication);
}
