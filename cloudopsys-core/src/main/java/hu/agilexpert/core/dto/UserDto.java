package hu.agilexpert.core.dto;

import hu.agilexpert.core.model.Theme;

public record UserDto(Long id, String name, String username, Theme theme,
                      Long activeIconId, Long activeBackgroundId) {
}
