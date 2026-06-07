package hu.agilexpert.core.mapper;

import hu.agilexpert.core.dto.UserAssetDto;
import hu.agilexpert.core.model.UserAsset;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserAssetMapper {

    UserAssetDto toDto(UserAsset userAsset);
}
