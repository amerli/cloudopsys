package hu.agilexpert.core.controller;

import hu.agilexpert.core.dto.UserAssetDto;
import hu.agilexpert.core.exception.DuplicateIdentifierException;
import hu.agilexpert.core.mapper.UserAssetMapper;
import hu.agilexpert.core.model.UserAsset;
import hu.agilexpert.core.model.Visibility;
import hu.agilexpert.core.service.UserAssetService;
import hu.agilexpert.core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/assets")
@RequiredArgsConstructor
public class UserAssetRestController {

    private final UserService userService;
    private final UserAssetService userAssetService;
    private final UserAssetMapper userAssetMapper;

    @GetMapping
    public ResponseEntity<List<UserAssetDto>> getAssets(@PathVariable Long userId) {
        return userService.findById(userId)
                .map(user -> ResponseEntity.ok(userAssetService.getAssetsByUser(user).stream()
                        .map(userAssetMapper::toDto).toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/available-icons")
    public ResponseEntity<List<UserAssetDto>> getAvailableIcons(@PathVariable Long userId) {
        return userService.findById(userId)
                .map(user -> ResponseEntity.ok(userAssetService.getAvailableIconsForUser(user).stream()
                        .map(userAssetMapper::toDto).toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/available-backgrounds")
    public ResponseEntity<List<UserAssetDto>> getAvailableBackgrounds(@PathVariable Long userId) {
        return userService.findById(userId)
                .map(user -> ResponseEntity.ok(userAssetService.getAvailableBackgroundsForUser(user).stream()
                        .map(userAssetMapper::toDto).toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/icons")
    public ResponseEntity<?> addIcon(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        return userService.findById(userId).map(user -> {
            try {
                UserAssetDto dto = userAssetMapper.toDto(
                        userAssetService.addAsset(user, UserAsset.AssetType.ICON, body.get("fileName"), Visibility.PRIVATE));
                return ResponseEntity.status(HttpStatus.CREATED).<Object>body(dto);
            } catch (DuplicateIdentifierException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).<Object>body(Map.of("error", e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/backgrounds")
    public ResponseEntity<?> addBackground(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        return userService.findById(userId).map(user -> {
            try {
                UserAssetDto dto = userAssetMapper.toDto(
                        userAssetService.addAsset(user, UserAsset.AssetType.BACKGROUND, body.get("fileName"), Visibility.PRIVATE));
                return ResponseEntity.status(HttpStatus.CREATED).<Object>body(dto);
            } catch (DuplicateIdentifierException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).<Object>body(Map.of("error", e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{assetId}")
    public ResponseEntity<?> removeAsset(@PathVariable Long userId, @PathVariable Long assetId) {
        if (userService.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        userAssetService.removeAssetFromUser(userService.findById(userId).get(), assetId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{assetId}/make-public")
    public ResponseEntity<Void> makePublic(@PathVariable Long userId, @PathVariable Long assetId) {
        userAssetService.makePublic(assetId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/active-icon")
    public ResponseEntity<?> setActiveIcon(@PathVariable Long userId, @RequestBody Map<String, Long> body) {
        if (userService.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        userAssetService.setActiveIcon(userService.findById(userId).get(), body.get("assetId"));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/active-background")
    public ResponseEntity<?> setActiveBackground(@PathVariable Long userId, @RequestBody Map<String, Long> body) {
        if (userService.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        userAssetService.setActiveBackground(userService.findById(userId).get(), body.get("assetId"));
        return ResponseEntity.ok().build();
    }
}
