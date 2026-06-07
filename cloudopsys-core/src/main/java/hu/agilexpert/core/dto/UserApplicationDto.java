package hu.agilexpert.core.dto;

public record UserApplicationDto(Long id, Long userId, ApplicationDto application, boolean running) {
}
