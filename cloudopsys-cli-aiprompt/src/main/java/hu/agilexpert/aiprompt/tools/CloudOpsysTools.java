package hu.agilexpert.aiprompt.tools;

import hu.agilexpert.core.client.CoreApiClient;
import hu.agilexpert.core.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CloudOpsysTools {

    private final CoreApiClient coreApiClient;

    @Tool(description = "Add a new user to the system with a display name and a unique username.")
    public String addUser(
            @ToolParam(description = "The display name of the user, e.g. 'Alice'") String name,
            @ToolParam(description = "The unique username/login, e.g. 'alice'") String username) {
        try {
            return coreApiClient.addUser(name, username)
                    .map(u -> "User '" + u.name() + "' (@" + u.username() + ") created with defaults.")
                    .orElse("Could not create user '" + name + "': username '" + username + "' is already taken.");
        } catch (Exception e) {
            return "Could not create user '" + name + "': " + e.getMessage();
        }
    }

    @Tool(description = "List all users registered in the system.")
    public String listUsers() {
        List<String> userLines = coreApiClient.listUsers().stream()
                .map(u -> u.name() + " (@" + u.username() + ")"
                        + (u.theme() != null ? " [" + u.theme() + "]" : ""))
                .toList();
        if (userLines.isEmpty()) {
            return "No users found.";
        }
        return "Users: " + String.join(", ", userLines);
    }

    @Tool(description = "Start an application for a specific user.")
    public String startApplication(
            @ToolParam(description = "The name of the application, e.g. 'Excel'") String appName,
            @ToolParam(description = "The user's name or username") String userIdentifier) {
        Optional<UserDto> userOpt = resolveUser(userIdentifier);
        if (userOpt.isEmpty()) return "User not found: '" + userIdentifier + "'";
        UserDto user = userOpt.get();

        Optional<Long> appId = resolveAppId(user.id(), appName);
        if (appId.isEmpty()) {
            // associate first, then find again
            coreApiClient.associateApp(user.id(), appName);
            appId = resolveAppId(user.id(), appName);
        }
        appId.ifPresent(id -> coreApiClient.startApp(user.id(), id));
        return "Started '" + appName + "' for user '" + user.name() + "'.";
    }

    @Tool(description = "Stop a running application for a specific user.")
    public String stopApplication(
            @ToolParam(description = "The name of the application, e.g. 'Word'") String appName,
            @ToolParam(description = "The user's name or username") String userIdentifier) {
        Optional<UserDto> userOpt = resolveUser(userIdentifier);
        if (userOpt.isEmpty()) return "User not found: '" + userIdentifier + "'";
        UserDto user = userOpt.get();

        Optional<Long> appId = resolveAppId(user.id(), appName);
        if (appId.isEmpty()) {
            return "'" + appName + "' was not running for user '" + user.name() + "' — nothing to stop.";
        }
        coreApiClient.stopApp(user.id(), appId.get());
        return "Stopped '" + appName + "' for user '" + user.name() + "'.";
    }

    @Tool(description = "Check whether an application is currently running or stopped for a specific user.")
    public String getApplicationStatus(
            @ToolParam(description = "The name of the application") String appName,
            @ToolParam(description = "The user's name or username") String userIdentifier) {
        Optional<UserDto> userOpt = resolveUser(userIdentifier);
        if (userOpt.isEmpty()) return "User not found: '" + userIdentifier + "'";
        UserDto user = userOpt.get();

        return coreApiClient.getAppsForUser(user.id()).stream()
                .filter(ua -> ua.application().name().equalsIgnoreCase(appName))
                .findFirst()
                .map(ua -> "'" + appName + "' is " + (ua.running() ? "RUNNING" : "STOPPED")
                        + " for user '" + user.name() + "'.")
                .orElse("'" + appName + "' is not associated with user '" + user.name() + "' (STOPPED).");
    }

    @Tool(description = "List all applications associated with a specific user, including their running status.")
    public String listApplicationsForUser(
            @ToolParam(description = "The user's name or username") String userIdentifier) {
        Optional<UserDto> userOpt = resolveUser(userIdentifier);
        if (userOpt.isEmpty()) return "User not found: '" + userIdentifier + "'";
        UserDto user = userOpt.get();

        List<String> appLines = coreApiClient.getAppsForUser(user.id()).stream()
                .map(ua -> ua.application().name() + " [" + (ua.running() ? "RUNNING" : "STOPPED") + "]")
                .toList();
        if (appLines.isEmpty()) return "No applications associated with user '" + user.name() + "'.";
        return "Applications for '" + user.name() + "': " + String.join(", ", appLines);
    }

    @Tool(description = "Set the UI theme (DARK or BRIGHT) for a specific user.")
    public String setTheme(
            @ToolParam(description = "The user's name or username") String userIdentifier,
            @ToolParam(description = "The theme to apply: DARK or BRIGHT") String theme) {
        Optional<UserDto> userOpt = resolveUser(userIdentifier);
        if (userOpt.isEmpty()) return "User not found: '" + userIdentifier + "'";
        UserDto user = userOpt.get();

        try {
            String normalized = theme.toUpperCase();
            // validate locally before calling API
            hu.agilexpert.core.model.Theme.valueOf(normalized);
            coreApiClient.updateUserTheme(user.id(), normalized);
            return "Theme set to " + normalized + " for user '" + user.name() + "'.";
        } catch (IllegalArgumentException e) {
            return "Unknown theme: '" + theme + "'. Use DARK or BRIGHT.";
        }
    }

    private Optional<UserDto> resolveUser(String identifier) {
        return coreApiClient.listUsers().stream()
                .filter(u -> u.username().equalsIgnoreCase(identifier)
                        || u.name().equalsIgnoreCase(identifier))
                .findFirst();
    }

    private Optional<Long> resolveAppId(Long userId, String appName) {
        return coreApiClient.getAppsForUser(userId).stream()
                .filter(ua -> ua.application().name().equalsIgnoreCase(appName))
                .map(ua -> ua.application().id())
                .findFirst();
    }
}
