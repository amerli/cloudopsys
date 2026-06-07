package hu.agilexpert.core.client;

import hu.agilexpert.core.dto.ApplicationDto;
import hu.agilexpert.core.dto.UserApplicationDto;
import hu.agilexpert.core.dto.UserAssetDto;
import hu.agilexpert.core.dto.UserDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST client for cloudopsys-core API.
 * Used by cloudopsys-web, cloudopsys-cli-quickstart and cloudopsys-cli-aiprompt
 * to communicate with the core microservice instead of accessing the DB directly.
 */
public class CoreApiClient {

    private final RestClient restClient;

    public CoreApiClient(String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public CoreApiClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    // ── Users ────────────────────────────────────────────────────────────────

    public List<UserDto> listUsers() {
        return restClient.get().uri("/api/users")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public Optional<UserDto> findUserById(Long id) {
        try {
            UserDto dto = restClient.get().uri("/api/users/{id}", id)
                    .retrieve()
                    .body(UserDto.class);
            return Optional.ofNullable(dto);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) return Optional.empty();
            throw e;
        }
    }

    /**
     * @return created UserDto, or empty if username is already taken (409 Conflict)
     * @throws RuntimeException on other errors
     */
    public Optional<UserDto> addUser(String name, String username) {
        try {
            UserDto dto = restClient.post().uri("/api/users")
                    .body(Map.of("name", name, "username", username))
                    .retrieve()
                    .body(UserDto.class);
            return Optional.ofNullable(dto);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) return Optional.empty();
            throw e;
        }
    }

    public void updateUserName(Long id, String newName) {
        restClient.patch().uri("/api/users/{id}/name", id)
                .body(Map.of("name", newName))
                .retrieve()
                .toBodilessEntity();
    }

    public void updateUserTheme(Long id, String theme) {
        restClient.patch().uri("/api/users/{id}/theme", id)
                .body(Map.of("theme", theme))
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteUser(Long id) {
        restClient.delete().uri("/api/users/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    // ── Assets ───────────────────────────────────────────────────────────────

    public List<UserAssetDto> getAssetsByUser(Long userId) {
        return restClient.get().uri("/api/users/{userId}/assets", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<UserAssetDto> getAvailableIconsForUser(Long userId) {
        return restClient.get().uri("/api/users/{userId}/assets/available-icons", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<UserAssetDto> getAvailableBackgroundsForUser(Long userId) {
        return restClient.get().uri("/api/users/{userId}/assets/available-backgrounds", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public Optional<UserAssetDto> addIcon(Long userId, String fileName) {
        try {
            UserAssetDto dto = restClient.post().uri("/api/users/{userId}/assets/icons", userId)
                    .body(Map.of("fileName", fileName))
                    .retrieve()
                    .body(UserAssetDto.class);
            return Optional.ofNullable(dto);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) return Optional.empty();
            throw e;
        }
    }

    public Optional<UserAssetDto> addBackground(Long userId, String fileName) {
        try {
            UserAssetDto dto = restClient.post().uri("/api/users/{userId}/assets/backgrounds", userId)
                    .body(Map.of("fileName", fileName))
                    .retrieve()
                    .body(UserAssetDto.class);
            return Optional.ofNullable(dto);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) return Optional.empty();
            throw e;
        }
    }

    public void removeAssetFromUser(Long userId, Long assetId) {
        restClient.delete().uri("/api/users/{userId}/assets/{assetId}", userId, assetId)
                .retrieve()
                .toBodilessEntity();
    }

    public void makeAssetPublic(Long userId, Long assetId) {
        restClient.patch().uri("/api/users/{userId}/assets/{assetId}/make-public", userId, assetId)
                .retrieve()
                .toBodilessEntity();
    }

    public void setActiveIcon(Long userId, Long assetId) {
        restClient.patch().uri("/api/users/{userId}/assets/active-icon", userId)
                .body(Map.of("assetId", assetId))
                .retrieve()
                .toBodilessEntity();
    }

    public void setActiveBackground(Long userId, Long assetId) {
        restClient.patch().uri("/api/users/{userId}/assets/active-background", userId)
                .body(Map.of("assetId", assetId))
                .retrieve()
                .toBodilessEntity();
    }

    // ── Applications ─────────────────────────────────────────────────────────

    public List<ApplicationDto> listAllApplications() {
        return restClient.get().uri("/api/applications")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<UserApplicationDto> getAppsForUser(Long userId) {
        return restClient.get().uri("/api/users/{userId}/apps", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public void associateApp(Long userId, String appName) {
        restClient.post().uri("/api/users/{userId}/apps", userId)
                .body(Map.of("appName", appName))
                .retrieve()
                .toBodilessEntity();
    }

    public void dissociateApp(Long userId, Long appId) {
        restClient.delete().uri("/api/users/{userId}/apps/{appId}", userId, appId)
                .retrieve()
                .toBodilessEntity();
    }

    public void startApp(Long userId, Long appId) {
        restClient.post().uri("/api/users/{userId}/apps/{appId}/start", userId, appId)
                .retrieve()
                .toBodilessEntity();
    }

    public void stopApp(Long userId, Long appId) {
        restClient.post().uri("/api/users/{userId}/apps/{appId}/stop", userId, appId)
                .retrieve()
                .toBodilessEntity();
    }
}
