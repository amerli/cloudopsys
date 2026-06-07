package hu.agilexpert.web;

import hu.agilexpert.core.client.CoreApiClient;
import hu.agilexpert.core.dto.ApplicationDto;
import hu.agilexpert.core.dto.UserApplicationDto;
import hu.agilexpert.core.dto.UserAssetDto;
import hu.agilexpert.core.dto.UserDto;
import hu.agilexpert.core.model.Theme;
import hu.agilexpert.core.model.UserAsset;
import hu.agilexpert.core.model.Visibility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CoreApiClient coreApiClient;

    private static final UserDto ALICE = new UserDto(1L, "Alice", "alice", Theme.BRIGHT, null, null);
    private static final UserAssetDto ICON_DTO = new UserAssetDto(1L, UserAsset.AssetType.ICON, "icon.png", Visibility.PRIVATE);
    private static final ApplicationDto EXCEL_DTO = new ApplicationDto(1L, "Excel");
    private static final UserApplicationDto USER_APP_DTO = new UserApplicationDto(1L, 1L, EXCEL_DTO, false);

    // --- index ---

    @Test
    void index_redirectsToUsers() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
    }

    // --- listUsers ---

    @Test
    void listUsers_returnsUsersView_withUsersModel() throws Exception {
        when(coreApiClient.listUsers()).thenReturn(List.of(ALICE));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users"));
    }

    // --- addUser ---

    @Test
    void addUser_redirectsToUsers_andSetsFlashMessage() throws Exception {
        when(coreApiClient.addUser("Alice", "alice")).thenReturn(Optional.of(ALICE));

        mockMvc.perform(post("/users/add")
                        .param("name", "Alice")
                        .param("username", "alice"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"))
                .andExpect(flash().attributeExists("message"));

        verify(coreApiClient).addUser("Alice", "alice");
    }

    @Test
    void addUser_setsFlashError_onConflict() throws Exception {
        doThrow(HttpClientErrorException.create(HttpStatus.CONFLICT, "Conflict", null, null, null))
                .when(coreApiClient).addUser("Alice", "alice");

        mockMvc.perform(post("/users/add")
                        .param("name", "Alice")
                        .param("username", "alice"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"))
                .andExpect(flash().attributeExists("error"));
    }

    // --- deleteUser ---

    @Test
    void deleteUser_redirectsToUsers_andSetsFlashMessage() throws Exception {
        mockMvc.perform(post("/users/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"))
                .andExpect(flash().attributeExists("message"));

        verify(coreApiClient).deleteUser(1L);
    }

    // --- profile ---

    @Test
    void profile_returnsProfileView_whenUserFound() throws Exception {
        when(coreApiClient.findUserById(1L)).thenReturn(Optional.of(ALICE));
        when(coreApiClient.getAssetsByUser(1L)).thenReturn(List.of(ICON_DTO));
        when(coreApiClient.getAvailableIconsForUser(1L)).thenReturn(List.of(ICON_DTO));
        when(coreApiClient.getAvailableBackgroundsForUser(1L)).thenReturn(List.of());

        mockMvc.perform(get("/users/1/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user", "assets", "availableIcons", "availableBackgrounds", "themes"));
    }

    @Test
    void profile_redirectsToUsers_whenUserNotFound() throws Exception {
        when(coreApiClient.findUserById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/99/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
    }

    // --- updateName ---

    @Test
    void updateName_updatesAndRedirectsToProfile() throws Exception {
        mockMvc.perform(post("/users/1/profile/name")
                        .param("name", "Alicia"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/1/profile"))
                .andExpect(flash().attributeExists("message"));

        verify(coreApiClient).updateUserName(1L, "Alicia");
    }

    // --- updateTheme ---

    @Test
    void updateTheme_setsThemeAndRedirects() throws Exception {
        mockMvc.perform(post("/users/1/profile/theme")
                        .param("theme", "DARK"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/1/profile"))
                .andExpect(flash().attributeExists("message"));

        verify(coreApiClient).updateUserTheme(1L, "DARK");
    }

    @Test
    void updateTheme_setsFlashError_onClientError() throws Exception {
        doThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null))
                .when(coreApiClient).updateUserTheme(1L, "RAINBOW");

        mockMvc.perform(post("/users/1/profile/theme")
                        .param("theme", "RAINBOW"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/1/profile"))
                .andExpect(flash().attributeExists("error"));
    }

    // --- addIcon ---

    @Test
    void addIcon_addsIconAndRedirects() throws Exception {
        when(coreApiClient.addIcon(1L, "icon.png")).thenReturn(Optional.of(ICON_DTO));

        mockMvc.perform(post("/users/1/profile/icon")
                        .param("fileName", "icon.png"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/1/profile"))
                .andExpect(flash().attributeExists("message"));

        verify(coreApiClient).addIcon(1L, "icon.png");
    }

    @Test
    void addIcon_setsFlashError_onConflict() throws Exception {
        doThrow(HttpClientErrorException.create(HttpStatus.CONFLICT, "Conflict", null, null, null))
                .when(coreApiClient).addIcon(1L, "icon.png");

        mockMvc.perform(post("/users/1/profile/icon")
                        .param("fileName", "icon.png"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/1/profile"))
                .andExpect(flash().attributeExists("error"));
    }

    // --- addBg ---

    @Test
    void addBg_addsBackgroundAndRedirects() throws Exception {
        when(coreApiClient.addBackground(1L, "bg.png")).thenReturn(Optional.of(
                new UserAssetDto(2L, UserAsset.AssetType.BACKGROUND, "bg.png", Visibility.PRIVATE)));

        mockMvc.perform(post("/users/1/profile/bg")
                        .param("fileName", "bg.png"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/1/profile"))
                .andExpect(flash().attributeExists("message"));

        verify(coreApiClient).addBackground(1L, "bg.png");
    }

    // --- deleteAsset ---

    @Test
    void deleteAsset_removesAssetAndRedirects() throws Exception {
        mockMvc.perform(post("/users/1/profile/asset/5/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/1/profile"))
                .andExpect(flash().attributeExists("message"));

        verify(coreApiClient).removeAssetFromUser(1L, 5L);
    }

    // --- activateIcon ---

    @Test
    void activateIcon_setsActiveIconAndRedirects() throws Exception {
        mockMvc.perform(post("/users/1/profile/icon/activate")
                        .param("assetId", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/1/profile"))
                .andExpect(flash().attributeExists("message"));

        verify(coreApiClient).setActiveIcon(1L, 5L);
    }

    // --- activateBackground ---

    @Test
    void activateBackground_setsActiveBackgroundAndRedirects() throws Exception {
        mockMvc.perform(post("/users/1/profile/bg/activate")
                        .param("assetId", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/1/profile"))
                .andExpect(flash().attributeExists("message"));

        verify(coreApiClient).setActiveBackground(1L, 5L);
    }

    // --- makeAssetPublic ---

    @Test
    void makeAssetPublic_makesPublicAndRedirects() throws Exception {
        mockMvc.perform(post("/users/1/profile/asset/5/make-public"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/1/profile"))
                .andExpect(flash().attributeExists("message"));

        verify(coreApiClient).makeAssetPublic(1L, 5L);
    }

    // --- apps ---

    @Test
    void apps_returnsAppsView_whenUserFound() throws Exception {
        when(coreApiClient.findUserById(1L)).thenReturn(Optional.of(ALICE));
        when(coreApiClient.getAppsForUser(1L)).thenReturn(List.of(USER_APP_DTO));
        when(coreApiClient.listAllApplications()).thenReturn(List.of(EXCEL_DTO));

        mockMvc.perform(get("/users/1/apps"))
                .andExpect(status().isOk())
                .andExpect(view().name("apps"))
                .andExpect(model().attributeExists("user", "userApps", "allApps"));
    }

    @Test
    void apps_redirectsToUsers_whenUserNotFound() throws Exception {
        when(coreApiClient.findUserById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/99/apps"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
    }

    // --- addApp ---

    @Test
    void addApp_associatesAppAndRedirects() throws Exception {
        mockMvc.perform(post("/users/1/apps/add")
                        .param("appName", "Excel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/1/apps"))
                .andExpect(flash().attributeExists("message"));

        verify(coreApiClient).associateApp(1L, "Excel");
    }

    // --- removeApp ---

    @Test
    void removeApp_dissociatesAppAndRedirects() throws Exception {
        mockMvc.perform(post("/users/1/apps/2/remove"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/1/apps"))
                .andExpect(flash().attributeExists("message"));

        verify(coreApiClient).dissociateApp(1L, 2L);
    }

    // --- startApp ---

    @Test
    void startApp_startsAppAndRedirects() throws Exception {
        mockMvc.perform(post("/users/1/apps/2/start"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/1/apps"))
                .andExpect(flash().attributeExists("message"));

        verify(coreApiClient).startApp(1L, 2L);
    }

    // --- stopApp ---

    @Test
    void stopApp_stopsAppAndRedirects() throws Exception {
        mockMvc.perform(post("/users/1/apps/2/stop"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/1/apps"))
                .andExpect(flash().attributeExists("message"));

        verify(coreApiClient).stopApp(1L, 2L);
    }
}
