package hu.agilexpert.core.controller;

import hu.agilexpert.core.dto.UserAssetDto;
import hu.agilexpert.core.exception.DuplicateIdentifierException;
import hu.agilexpert.core.mapper.UserAssetMapper;
import hu.agilexpert.core.model.User;
import hu.agilexpert.core.model.UserAsset;
import hu.agilexpert.core.model.Visibility;
import hu.agilexpert.core.service.UserAssetService;
import hu.agilexpert.core.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserAssetRestController.class)
class UserAssetRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserAssetService userAssetService;

    @MockitoBean
    private UserAssetMapper userAssetMapper;

    private static final User ALICE = new User("Alice", "alice");
    private static final UserAsset ICON = new UserAsset(UserAsset.AssetType.ICON, "icon.png", Visibility.PRIVATE);
    private static final UserAssetDto ICON_DTO = new UserAssetDto(1L, UserAsset.AssetType.ICON, "icon.png", Visibility.PRIVATE);

    // --- GET /api/users/{userId}/assets ---

    @Test
    void getAssets_returnsList_whenUserFound() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(ALICE));
        when(userAssetService.getAssetsByUser(ALICE)).thenReturn(List.of(ICON));
        when(userAssetMapper.toDto(ICON)).thenReturn(ICON_DTO);

        mockMvc.perform(get("/api/users/1/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("icon.png"));
    }

    @Test
    void getAssets_returns404_whenUserNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99/assets"))
                .andExpect(status().isNotFound());
    }

    // --- GET /api/users/{userId}/assets/available-icons ---

    @Test
    void getAvailableIcons_returnsList_whenUserFound() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(ALICE));
        when(userAssetService.getAvailableIconsForUser(ALICE)).thenReturn(List.of(ICON));
        when(userAssetMapper.toDto(ICON)).thenReturn(ICON_DTO);

        mockMvc.perform(get("/api/users/1/assets/available-icons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("ICON"));
    }

    @Test
    void getAvailableIcons_returns404_whenUserNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99/assets/available-icons"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/users/{userId}/assets/icons ---

    @Test
    void addIcon_returns201_whenUserFound() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(ALICE));
        when(userAssetService.addAsset(ALICE, UserAsset.AssetType.ICON, "icon.png", Visibility.PRIVATE))
                .thenReturn(ICON);
        when(userAssetMapper.toDto(ICON)).thenReturn(ICON_DTO);

        mockMvc.perform(post("/api/users/1/assets/icons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"icon.png\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("icon.png"));
    }

    @Test
    void addIcon_returns409_onDuplicate() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(ALICE));
        when(userAssetService.addAsset(any(), any(), any(), any()))
                .thenThrow(new DuplicateIdentifierException("Asset already exists."));

        mockMvc.perform(post("/api/users/1/assets/icons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"icon.png\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void addIcon_returns404_whenUserNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/99/assets/icons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"icon.png\"}"))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /api/users/{userId}/assets/{assetId} ---

    @Test
    void removeAsset_returns204_whenUserFound() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(ALICE));

        mockMvc.perform(delete("/api/users/1/assets/5"))
                .andExpect(status().isNoContent());

        verify(userAssetService).removeAssetFromUser(ALICE, 5L);
    }

    @Test
    void removeAsset_returns404_whenUserNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/users/99/assets/5"))
                .andExpect(status().isNotFound());
    }

    // --- PATCH /api/users/{userId}/assets/active-icon ---

    @Test
    void setActiveIcon_returns200_whenUserFound() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(ALICE));

        mockMvc.perform(patch("/api/users/1/assets/active-icon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assetId\":5}"))
                .andExpect(status().isOk());

        verify(userAssetService).setActiveIcon(ALICE, 5L);
    }

    @Test
    void setActiveIcon_returns404_whenUserNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/users/99/assets/active-icon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assetId\":5}"))
                .andExpect(status().isNotFound());
    }
}
