package hu.agilexpert.core.controller;

import hu.agilexpert.core.dto.ApplicationDto;
import hu.agilexpert.core.dto.UserApplicationDto;
import hu.agilexpert.core.mapper.ApplicationMapper;
import hu.agilexpert.core.mapper.UserApplicationMapper;
import hu.agilexpert.core.model.Application;
import hu.agilexpert.core.model.User;
import hu.agilexpert.core.model.UserApplication;
import hu.agilexpert.core.service.ApplicationService;
import hu.agilexpert.core.service.UserApplicationService;
import hu.agilexpert.core.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserApplicationRestController.class)
class UserApplicationRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserApplicationService userApplicationService;

    @MockitoBean
    private ApplicationService applicationService;

    @MockitoBean
    private ApplicationMapper applicationMapper;

    @MockitoBean
    private UserApplicationMapper userApplicationMapper;

    private static final User ALICE = new User("Alice", "alice");
    private static final Application EXCEL = new Application("Excel");
    private static final ApplicationDto EXCEL_DTO = new ApplicationDto(1L, "Excel");
    private static final UserApplication USER_APP = new UserApplication(ALICE, EXCEL);
    private static final UserApplicationDto USER_APP_DTO = new UserApplicationDto(1L, 1L, EXCEL_DTO, false);

    // --- GET /api/applications ---

    @Test
    void listAllApplications_returnsList() throws Exception {
        when(applicationService.listApplications()).thenReturn(List.of(EXCEL));
        when(applicationMapper.toDto(EXCEL)).thenReturn(EXCEL_DTO);

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Excel"));
    }

    @Test
    void listAllApplications_returnsEmptyList() throws Exception {
        when(applicationService.listApplications()).thenReturn(List.of());

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // --- GET /api/users/{userId}/apps ---

    @Test
    void getAppsForUser_returnsList_whenUserFound() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(ALICE));
        when(userApplicationService.getApplicationsForUser(ALICE)).thenReturn(List.of(USER_APP));
        when(userApplicationMapper.toDto(USER_APP)).thenReturn(USER_APP_DTO);

        mockMvc.perform(get("/api/users/1/apps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].application.name").value("Excel"));
    }

    @Test
    void getAppsForUser_returns404_whenUserNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99/apps"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/users/{userId}/apps ---

    @Test
    void associateApp_returns200_whenUserFound() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(ALICE));

        mockMvc.perform(post("/api/users/1/apps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appName\":\"Excel\"}"))
                .andExpect(status().isOk());

        verify(userApplicationService).associateApplicationByName(ALICE, "Excel");
    }

    @Test
    void associateApp_returns404_whenUserNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/99/apps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appName\":\"Excel\"}"))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /api/users/{userId}/apps/{appId} ---

    @Test
    void dissociateApp_returns204_whenUserFound() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(ALICE));

        mockMvc.perform(delete("/api/users/1/apps/1"))
                .andExpect(status().isNoContent());

        verify(userApplicationService).dissociateApplicationById(ALICE, 1L);
    }

    @Test
    void dissociateApp_returns404_whenUserNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/users/99/apps/1"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/users/{userId}/apps/{appId}/start ---

    @Test
    void startApp_returns200_whenUserFound() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(ALICE));

        mockMvc.perform(post("/api/users/1/apps/1/start"))
                .andExpect(status().isOk());

        verify(userApplicationService).startApplicationById(ALICE, 1L);
    }

    @Test
    void startApp_returns404_whenUserNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/99/apps/1/start"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/users/{userId}/apps/{appId}/stop ---

    @Test
    void stopApp_returns200_whenUserFound() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(ALICE));

        mockMvc.perform(post("/api/users/1/apps/1/stop"))
                .andExpect(status().isOk());

        verify(userApplicationService).stopApplicationById(ALICE, 1L);
    }

    @Test
    void stopApp_returns404_whenUserNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/99/apps/1/stop"))
                .andExpect(status().isNotFound());
    }
}
