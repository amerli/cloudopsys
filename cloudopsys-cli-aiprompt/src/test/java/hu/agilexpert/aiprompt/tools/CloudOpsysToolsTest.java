package hu.agilexpert.aiprompt.tools;

import hu.agilexpert.core.client.CoreApiClient;
import hu.agilexpert.core.dto.ApplicationDto;
import hu.agilexpert.core.dto.UserApplicationDto;
import hu.agilexpert.core.dto.UserDto;
import hu.agilexpert.core.model.Theme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudOpsysToolsTest {

    @Mock
    private CoreApiClient coreApiClient;

    @InjectMocks
    private CloudOpsysTools cloudOpsysTools;

    private UserDto testUser() {
        return new UserDto(1L, "Alice", "alice", null, null, null);
    }

    private ApplicationDto testApp(String name) {
        return new ApplicationDto(10L, name);
    }

    private UserApplicationDto testUserApp(String appName, boolean running) {
        return new UserApplicationDto(100L, 1L, testApp(appName), running);
    }

    // ── addUser ──────────────────────────────────────────────────────────────

    @Test
    void addUser_returnsSuccessMessage_whenCreated() {
        when(coreApiClient.addUser("Alice", "alice")).thenReturn(Optional.of(testUser()));

        String result = cloudOpsysTools.addUser("Alice", "alice");

        assertThat(result).contains("Alice").contains("@alice").contains("created");
    }

    @Test
    void addUser_returnsErrorMessage_whenEmpty() {
        when(coreApiClient.addUser("Alice", "alice")).thenReturn(Optional.empty());

        String result = cloudOpsysTools.addUser("Alice", "alice");

        assertThat(result).contains("already taken");
    }

    @Test
    void addUser_returnsErrorMessage_whenExceptionThrown() {
        when(coreApiClient.addUser("Alice", "alice")).thenThrow(new RuntimeException("duplicate"));

        String result = cloudOpsysTools.addUser("Alice", "alice");

        assertThat(result).contains("Could not create user").contains("duplicate");
    }

    // ── listUsers ─────────────────────────────────────────────────────────────

    @Test
    void listUsers_returnsFormattedList_whenUsersExist() {
        UserDto user = new UserDto(1L, "Alice", "alice", Theme.DARK, null, null);
        when(coreApiClient.listUsers()).thenReturn(List.of(user));

        String result = cloudOpsysTools.listUsers();

        assertThat(result).contains("Alice").contains("@alice").contains("DARK");
    }

    @Test
    void listUsers_returnsNoUsersMessage_whenEmpty() {
        when(coreApiClient.listUsers()).thenReturn(List.of());

        String result = cloudOpsysTools.listUsers();

        assertThat(result).isEqualTo("No users found.");
    }

    // ── startApplication ─────────────────────────────────────────────────────

    @Test
    void startApplication_returnsSuccess_whenUserAndAppFound() {
        UserApplicationDto ua = testUserApp("Excel", false);
        when(coreApiClient.listUsers()).thenReturn(List.of(testUser()));
        when(coreApiClient.getAppsForUser(1L)).thenReturn(List.of(ua));

        String result = cloudOpsysTools.startApplication("Excel", "alice");

        assertThat(result).contains("Started").contains("Excel").contains("Alice");
        verify(coreApiClient).startApp(1L, 10L);
    }

    @Test
    void startApplication_returnsSuccess_whenAppNotYetAssociated() {
        UserApplicationDto ua = testUserApp("Excel", false);
        when(coreApiClient.listUsers()).thenReturn(List.of(testUser()));
        // first call returns empty (not associated), after associate returns the app
        when(coreApiClient.getAppsForUser(1L))
                .thenReturn(List.of())
                .thenReturn(List.of(ua));

        String result = cloudOpsysTools.startApplication("Excel", "alice");

        assertThat(result).contains("Started").contains("Excel").contains("Alice");
        verify(coreApiClient).associateApp(1L, "Excel");
        verify(coreApiClient).startApp(1L, 10L);
    }

    @Test
    void startApplication_returnsError_whenUserNotFound() {
        when(coreApiClient.listUsers()).thenReturn(List.of());

        String result = cloudOpsysTools.startApplication("Excel", "unknown");

        assertThat(result).contains("User not found").contains("unknown");
    }

    // ── stopApplication ───────────────────────────────────────────────────────

    @Test
    void stopApplication_returnsSuccess_whenUserAndAppFound() {
        UserApplicationDto ua = testUserApp("Word", true);
        when(coreApiClient.listUsers()).thenReturn(List.of(testUser()));
        when(coreApiClient.getAppsForUser(1L)).thenReturn(List.of(ua));

        String result = cloudOpsysTools.stopApplication("Word", "alice");

        assertThat(result).contains("Stopped").contains("Word").contains("Alice");
        verify(coreApiClient).stopApp(1L, 10L);
    }

    @Test
    void stopApplication_returnsNeutral_whenAppNotAssociated() {
        when(coreApiClient.listUsers()).thenReturn(List.of(testUser()));
        when(coreApiClient.getAppsForUser(1L)).thenReturn(List.of());

        String result = cloudOpsysTools.stopApplication("Word", "alice");

        assertThat(result).contains("not running").contains("nothing to stop");
    }

    // ── getApplicationStatus ──────────────────────────────────────────────────

    @Test
    void getApplicationStatus_returnsRunning_whenRunning() {
        when(coreApiClient.listUsers()).thenReturn(List.of(testUser()));
        when(coreApiClient.getAppsForUser(1L)).thenReturn(List.of(testUserApp("Excel", true)));

        String result = cloudOpsysTools.getApplicationStatus("Excel", "alice");

        assertThat(result).contains("RUNNING");
    }

    @Test
    void getApplicationStatus_returnsStopped_whenStopped() {
        when(coreApiClient.listUsers()).thenReturn(List.of(testUser()));
        when(coreApiClient.getAppsForUser(1L)).thenReturn(List.of(testUserApp("Excel", false)));

        String result = cloudOpsysTools.getApplicationStatus("Excel", "alice");

        assertThat(result).contains("STOPPED");
    }

    @Test
    void getApplicationStatus_returnsNotAssociated_whenAbsent() {
        when(coreApiClient.listUsers()).thenReturn(List.of(testUser()));
        when(coreApiClient.getAppsForUser(1L)).thenReturn(List.of());

        String result = cloudOpsysTools.getApplicationStatus("Excel", "alice");

        assertThat(result).contains("not associated").contains("STOPPED");
    }

    // ── listApplicationsForUser ───────────────────────────────────────────────

    @Test
    void listApplicationsForUser_returnsFormattedList_whenAppsExist() {
        when(coreApiClient.listUsers()).thenReturn(List.of(testUser()));
        when(coreApiClient.getAppsForUser(1L)).thenReturn(List.of(testUserApp("Excel", true)));

        String result = cloudOpsysTools.listApplicationsForUser("alice");

        assertThat(result).contains("Excel").contains("RUNNING");
    }

    @Test
    void listApplicationsForUser_returnsNoApps_whenEmpty() {
        when(coreApiClient.listUsers()).thenReturn(List.of(testUser()));
        when(coreApiClient.getAppsForUser(1L)).thenReturn(List.of());

        String result = cloudOpsysTools.listApplicationsForUser("alice");

        assertThat(result).contains("No applications");
    }

    @Test
    void listApplicationsForUser_returnsError_whenUserNotFound() {
        when(coreApiClient.listUsers()).thenReturn(List.of());

        String result = cloudOpsysTools.listApplicationsForUser("ghost");

        assertThat(result).contains("User not found").contains("ghost");
    }

    // ── setTheme ──────────────────────────────────────────────────────────────

    @Test
    void setTheme_returnsSuccess_whenValidTheme() {
        when(coreApiClient.listUsers()).thenReturn(List.of(testUser()));

        String result = cloudOpsysTools.setTheme("alice", "DARK");

        assertThat(result).contains("DARK").contains("Alice");
        verify(coreApiClient).updateUserTheme(1L, "DARK");
    }

    @Test
    void setTheme_returnsError_whenInvalidTheme() {
        when(coreApiClient.listUsers()).thenReturn(List.of(testUser()));

        String result = cloudOpsysTools.setTheme("alice", "RAINBOW");

        assertThat(result).contains("Unknown theme").contains("RAINBOW");
    }

    @Test
    void setTheme_returnsError_whenUserNotFound() {
        when(coreApiClient.listUsers()).thenReturn(List.of());

        String result = cloudOpsysTools.setTheme("ghost", "DARK");

        assertThat(result).contains("User not found").contains("ghost");
    }
}
