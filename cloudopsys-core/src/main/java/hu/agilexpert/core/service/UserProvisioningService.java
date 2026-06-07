package hu.agilexpert.core.service;

import hu.agilexpert.core.model.Theme;
import hu.agilexpert.core.model.User;
import hu.agilexpert.core.model.UserAsset;
import hu.agilexpert.core.model.Visibility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProvisioningService {

    private final UserAssetService userAssetService;
    private final ApplicationService applicationService;
    private final UserApplicationService userApplicationService;

    @Transactional
    public void applyDefaults(User user) {
        user.setTheme(Theme.BRIGHT);
        UserAsset defaultIcon = addAssetIfAbsent(user, UserAsset.AssetType.ICON, "default-icon.png", Visibility.PUBLIC);
        UserAsset defaultBg = addAssetIfAbsent(user, UserAsset.AssetType.BACKGROUND, "default-bg.png", Visibility.PUBLIC);
        user.setActiveIcon(defaultIcon);
        user.setActiveBackground(defaultBg);
        userApplicationService.associateApplication(user, applicationService.addApplicationIfAbsent("Excel"));
        userApplicationService.associateApplication(user, applicationService.addApplicationIfAbsent("Word"));
    }

    private UserAsset addAssetIfAbsent(User user, UserAsset.AssetType type, String fileName, Visibility visibility) {
        return userAssetService.findByFileName(fileName).map(existingAsset -> {
            userAssetService.linkAssetToUser(user, existingAsset);
            return existingAsset;
        }).orElseGet(() -> userAssetService.addAsset(user, type, fileName, visibility));
    }
}
