package hu.agilexpert.core.mapper;

import hu.agilexpert.core.dto.ApplicationDto;
import hu.agilexpert.core.model.Application;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    ApplicationDto toDto(Application application);
}
