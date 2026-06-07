package hu.agilexpert.core.dto;

import hu.agilexpert.core.model.UserAsset;
import hu.agilexpert.core.model.Visibility;

public record UserAssetDto(Long id, UserAsset.AssetType type, String fileName, Visibility visibility) {
}
