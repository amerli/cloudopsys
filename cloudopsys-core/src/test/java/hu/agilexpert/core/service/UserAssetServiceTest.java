package hu.agilexpert.core.service;

import hu.agilexpert.core.exception.DuplicateIdentifierException;
import hu.agilexpert.core.model.User;
import hu.agilexpert.core.model.UserAsset;
import hu.agilexpert.core.model.Visibility;
import hu.agilexpert.core.repository.UserAssetRepository;
import hu.agilexpert.core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAssetServiceTest {

    @Mock
    private UserAssetRepository userAssetRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAssetService userAssetService;

    private User testUser() {
        User user = new User("Alice", "alice");
        user.setId(1L);
        return user;
    }

    @Test
    void addAsset_savesAndLinksAsset() {
        User user = testUser();
        when(userAssetRepository.findByFileName("icon.png")).thenReturn(Optional.empty());
        UserAsset saved = new UserAsset(UserAsset.AssetType.ICON, "icon.png", Visibility.PRIVATE);
        saved.setId(10L);
        when(userAssetRepository.save(any(UserAsset.class))).thenReturn(saved);

        UserAsset result = userAssetService.addAsset(user, UserAsset.AssetType.ICON, "icon.png", Visibility.PRIVATE);

        assertThat(result.getFileName()).isEqualTo("icon.png");
        assertThat(result.getType()).isEqualTo(UserAsset.AssetType.ICON);
        verify(userRepository).insertUserAssetIfAbsent(1L, 10L);
    }

    @Test
    void addAsset_throwsDuplicateIdentifierException_whenFileNameExists() {
        User user = testUser();
        when(userAssetRepository.findByFileName("icon.png"))
                .thenReturn(Optional.of(new UserAsset(UserAsset.AssetType.ICON, "icon.png", Visibility.PRIVATE)));

        assertThatThrownBy(() -> userAssetService.addAsset(user, UserAsset.AssetType.ICON, "icon.png", Visibility.PRIVATE))
                .isInstanceOf(DuplicateIdentifierException.class)
                .hasMessageContaining("icon.png");

        verify(userAssetRepository, never()).save(any());
    }

    @Test
    void addIcon_delegatesToAddAssetWithIconType() {
        User user = testUser();
        when(userAssetRepository.findByFileName("icon.png")).thenReturn(Optional.empty());
        UserAsset saved = new UserAsset(UserAsset.AssetType.ICON, "icon.png", Visibility.PRIVATE);
        saved.setId(10L);
        when(userAssetRepository.save(any())).thenReturn(saved);

        userAssetService.addIcon(user, "icon.png");

        verify(userAssetRepository).save(argThat(a ->
                a.getType() == UserAsset.AssetType.ICON && a.getVisibility() == Visibility.PRIVATE));
    }

    @Test
    void addBackground_delegatesToAddAssetWithBackgroundType() {
        User user = testUser();
        when(userAssetRepository.findByFileName("bg.jpg")).thenReturn(Optional.empty());
        UserAsset saved = new UserAsset(UserAsset.AssetType.BACKGROUND, "bg.jpg", Visibility.PRIVATE);
        saved.setId(11L);
        when(userAssetRepository.save(any())).thenReturn(saved);

        userAssetService.addBackground(user, "bg.jpg");

        verify(userAssetRepository).save(argThat(a ->
                a.getType() == UserAsset.AssetType.BACKGROUND && a.getVisibility() == Visibility.PRIVATE));
    }

    @Test
    void makePublic_setsVisibilityToPublic() {
        UserAsset asset = new UserAsset(UserAsset.AssetType.ICON, "icon.png", Visibility.PRIVATE);
        asset.setId(10L);
        when(userAssetRepository.findById(10L)).thenReturn(Optional.of(asset));

        userAssetService.makePublic(10L);

        assertThat(asset.getVisibility()).isEqualTo(Visibility.PUBLIC);
        verify(userAssetRepository).save(asset);
    }

    @Test
    void makePublic_doesNothing_whenAssetNotFound() {
        when(userAssetRepository.findById(99L)).thenReturn(Optional.empty());

        userAssetService.makePublic(99L);

        verify(userAssetRepository, never()).save(any());
    }

    @Test
    void removeAssetFromUser_callsDeleteUserAsset() {
        User user = testUser();

        userAssetService.removeAssetFromUser(user, 10L);

        verify(userRepository).deleteUserAsset(1L, 10L);
    }

    @Test
    void setActiveIcon_setsIconOnUserAndSaves() {
        User user = testUser();
        UserAsset icon = new UserAsset(UserAsset.AssetType.ICON, "icon.png", Visibility.PRIVATE);
        icon.setId(10L);
        when(userAssetRepository.findById(10L)).thenReturn(Optional.of(icon));

        userAssetService.setActiveIcon(user, 10L);

        assertThat(user.getActiveIcon()).isEqualTo(icon);
        verify(userRepository).save(user);
    }

    @Test
    void setActiveBackground_setsBackgroundOnUserAndSaves() {
        User user = testUser();
        UserAsset bg = new UserAsset(UserAsset.AssetType.BACKGROUND, "bg.jpg", Visibility.PRIVATE);
        bg.setId(11L);
        when(userAssetRepository.findById(11L)).thenReturn(Optional.of(bg));

        userAssetService.setActiveBackground(user, 11L);

        assertThat(user.getActiveBackground()).isEqualTo(bg);
        verify(userRepository).save(user);
    }

    @Test
    void getAssetsByUser_returnsUserAssets() {
        User user = testUser();
        List<UserAsset> assets = List.of(
                new UserAsset(UserAsset.AssetType.ICON, "icon.png", Visibility.PRIVATE));
        when(userAssetRepository.findByUser(user)).thenReturn(assets);

        List<UserAsset> result = userAssetService.getAssetsByUser(user);

        assertThat(result).isEqualTo(assets);
    }

    @Test
    void getAvailableIconsForUser_includesPrivateAndPublicIcons() {
        User user = testUser();
        UserAsset privateIcon = new UserAsset(UserAsset.AssetType.ICON, "private.png", Visibility.PRIVATE);
        privateIcon.setId(1L);
        UserAsset publicIcon = new UserAsset(UserAsset.AssetType.ICON, "public.png", Visibility.PUBLIC);
        publicIcon.setId(2L);
        UserAsset publicBg = new UserAsset(UserAsset.AssetType.BACKGROUND, "bg.jpg", Visibility.PUBLIC);
        publicBg.setId(3L);

        when(userAssetRepository.findByUser(user)).thenReturn(List.of(privateIcon));
        when(userAssetRepository.findByVisibility(Visibility.PUBLIC)).thenReturn(List.of(publicIcon, publicBg));

        List<UserAsset> result = userAssetService.getAvailableIconsForUser(user);

        assertThat(result).containsExactlyInAnyOrder(privateIcon, publicIcon);
    }

    @Test
    void getAvailableBackgroundsForUser_includesPrivateAndPublicBackgrounds() {
        User user = testUser();
        UserAsset privateBg = new UserAsset(UserAsset.AssetType.BACKGROUND, "private.jpg", Visibility.PRIVATE);
        privateBg.setId(1L);
        UserAsset publicBg = new UserAsset(UserAsset.AssetType.BACKGROUND, "public.jpg", Visibility.PUBLIC);
        publicBg.setId(2L);
        UserAsset publicIcon = new UserAsset(UserAsset.AssetType.ICON, "icon.png", Visibility.PUBLIC);
        publicIcon.setId(3L);

        when(userAssetRepository.findByUser(user)).thenReturn(List.of(privateBg));
        when(userAssetRepository.findByVisibility(Visibility.PUBLIC)).thenReturn(List.of(publicBg, publicIcon));

        List<UserAsset> result = userAssetService.getAvailableBackgroundsForUser(user);

        assertThat(result).containsExactlyInAnyOrder(privateBg, publicBg);
    }

    @Test
    void removeAllAssetsForUser_callsDeleteAllUserAssets() {
        User user = testUser();

        userAssetService.removeAllAssetsForUser(user);

        verify(userRepository).deleteAllUserAssets(1L);
    }
}
