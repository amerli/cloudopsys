package hu.agilexpert.core.service;

import hu.agilexpert.core.model.Application;
import hu.agilexpert.core.model.Theme;
import hu.agilexpert.core.model.User;
import hu.agilexpert.core.model.UserAsset;
import hu.agilexpert.core.model.Visibility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProvisioningServiceTest {

    @Mock
    private UserAssetService userAssetService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private UserApplicationService userApplicationService;

    @InjectMocks
    private UserProvisioningService userProvisioningService;

    private User testUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Alice");
        user.setUsername("alice");
        return user;
    }

    private UserAsset testAsset(String fileName) {
        UserAsset asset = new UserAsset();
        asset.setId(10L);
        asset.setFileName(fileName);
        return asset;
    }

    @Test
    void applyDefaults_setsThemeToBright() {
        User user = testUser();
        UserAsset icon = testAsset("default-icon.png");
        UserAsset bg = testAsset("default-bg.png");
        Application excel = new Application("Excel");
        Application word = new Application("Word");

        when(userAssetService.findByFileName("default-icon.png")).thenReturn(Optional.empty());
        when(userAssetService.findByFileName("default-bg.png")).thenReturn(Optional.empty());
        when(userAssetService.addAsset(user, UserAsset.AssetType.ICON, "default-icon.png", Visibility.PUBLIC)).thenReturn(icon);
        when(userAssetService.addAsset(user, UserAsset.AssetType.BACKGROUND, "default-bg.png", Visibility.PUBLIC)).thenReturn(bg);
        when(applicationService.addApplicationIfAbsent("Excel")).thenReturn(excel);
        when(applicationService.addApplicationIfAbsent("Word")).thenReturn(word);

        userProvisioningService.applyDefaults(user);

        assertThat(user.getTheme()).isEqualTo(Theme.BRIGHT);
    }

    @Test
    void applyDefaults_setsActiveIconAndBackground() {
        User user = testUser();
        UserAsset icon = testAsset("default-icon.png");
        UserAsset bg = testAsset("default-bg.png");
        Application excel = new Application("Excel");
        Application word = new Application("Word");

        when(userAssetService.findByFileName("default-icon.png")).thenReturn(Optional.empty());
        when(userAssetService.findByFileName("default-bg.png")).thenReturn(Optional.empty());
        when(userAssetService.addAsset(user, UserAsset.AssetType.ICON, "default-icon.png", Visibility.PUBLIC)).thenReturn(icon);
        when(userAssetService.addAsset(user, UserAsset.AssetType.BACKGROUND, "default-bg.png", Visibility.PUBLIC)).thenReturn(bg);
        when(applicationService.addApplicationIfAbsent("Excel")).thenReturn(excel);
        when(applicationService.addApplicationIfAbsent("Word")).thenReturn(word);

        userProvisioningService.applyDefaults(user);

        assertThat(user.getActiveIcon()).isEqualTo(icon);
        assertThat(user.getActiveBackground()).isEqualTo(bg);
    }

    @Test
    void applyDefaults_associatesExcelAndWord() {
        User user = testUser();
        UserAsset icon = testAsset("default-icon.png");
        UserAsset bg = testAsset("default-bg.png");
        Application excel = new Application("Excel");
        Application word = new Application("Word");

        when(userAssetService.findByFileName("default-icon.png")).thenReturn(Optional.empty());
        when(userAssetService.findByFileName("default-bg.png")).thenReturn(Optional.empty());
        when(userAssetService.addAsset(user, UserAsset.AssetType.ICON, "default-icon.png", Visibility.PUBLIC)).thenReturn(icon);
        when(userAssetService.addAsset(user, UserAsset.AssetType.BACKGROUND, "default-bg.png", Visibility.PUBLIC)).thenReturn(bg);
        when(applicationService.addApplicationIfAbsent("Excel")).thenReturn(excel);
        when(applicationService.addApplicationIfAbsent("Word")).thenReturn(word);

        userProvisioningService.applyDefaults(user);

        verify(userApplicationService).associateApplication(user, excel);
        verify(userApplicationService).associateApplication(user, word);
    }

    @Test
    void applyDefaults_linksExistingAssets_whenAlreadyPresent() {
        User user = testUser();
        UserAsset existingIcon = testAsset("default-icon.png");
        UserAsset existingBg = testAsset("default-bg.png");
        Application excel = new Application("Excel");
        Application word = new Application("Word");

        when(userAssetService.findByFileName("default-icon.png")).thenReturn(Optional.of(existingIcon));
        when(userAssetService.findByFileName("default-bg.png")).thenReturn(Optional.of(existingBg));
        when(applicationService.addApplicationIfAbsent("Excel")).thenReturn(excel);
        when(applicationService.addApplicationIfAbsent("Word")).thenReturn(word);

        userProvisioningService.applyDefaults(user);

        verify(userAssetService).linkAssetToUser(user, existingIcon);
        verify(userAssetService).linkAssetToUser(user, existingBg);
        verify(userAssetService, never()).addAsset(any(), any(), any(), any());
    }
}
