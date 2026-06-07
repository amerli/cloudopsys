package hu.agilexpert.core.controller;

import hu.agilexpert.core.dto.ApplicationDto;
import hu.agilexpert.core.dto.UserApplicationDto;
import hu.agilexpert.core.mapper.ApplicationMapper;
import hu.agilexpert.core.mapper.UserApplicationMapper;
import hu.agilexpert.core.service.ApplicationService;
import hu.agilexpert.core.service.UserApplicationService;
import hu.agilexpert.core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserApplicationRestController {

    private final UserService userService;
    private final UserApplicationService userApplicationService;
    private final ApplicationService applicationService;
    private final ApplicationMapper applicationMapper;
    private final UserApplicationMapper userApplicationMapper;

    @GetMapping("/api/applications")
    public List<ApplicationDto> listAllApplications() {
        return applicationService.listApplications().stream()
                .map(applicationMapper::toDto)
                .toList();
    }

    @GetMapping("/api/users/{userId}/apps")
    public ResponseEntity<List<UserApplicationDto>> getAppsForUser(@PathVariable Long userId) {
        return userService.findById(userId)
                .map(user -> ResponseEntity.ok(userApplicationService.getApplicationsForUser(user).stream()
                        .map(userApplicationMapper::toDto).toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/users/{userId}/apps")
    public ResponseEntity<?> associateApp(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        if (userService.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        userApplicationService.associateApplicationByName(userService.findById(userId).get(), body.get("appName"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/users/{userId}/apps/{appId}")
    public ResponseEntity<?> dissociateApp(@PathVariable Long userId, @PathVariable Long appId) {
        if (userService.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        userApplicationService.dissociateApplicationById(userService.findById(userId).get(), appId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/users/{userId}/apps/{appId}/start")
    public ResponseEntity<?> startApp(@PathVariable Long userId, @PathVariable Long appId) {
        if (userService.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        userApplicationService.startApplicationById(userService.findById(userId).get(), appId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/users/{userId}/apps/{appId}/stop")
    public ResponseEntity<?> stopApp(@PathVariable Long userId, @PathVariable Long appId) {
        if (userService.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        userApplicationService.stopApplicationById(userService.findById(userId).get(), appId);
        return ResponseEntity.ok().build();
    }
}
