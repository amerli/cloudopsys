package hu.agilexpert.core.service;

import hu.agilexpert.core.model.Application;
import hu.agilexpert.core.model.User;
import hu.agilexpert.core.model.UserApplication;
import hu.agilexpert.core.repository.UserApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    @Mock
    private UserApplicationRepository userApplicationRepository;
    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private UserApplicationService userApplicationService;

    private User testUser() {
        User user = new User("Alice", "alice");
        user.setId(1L);
        return user;
    }

    private Application testApp() {
        Application app = new Application("MyApp");
        app.setId(10L);
        return app;
    }

    @Test
    void associateApplication_returnsExisting_whenAlreadyAssociated() {
        User user = testUser();
        Application app = testApp();
        UserApplication existing = new UserApplication(user, app);
        existing.setId(100L);
        when(userApplicationRepository.findByUserAndApplication(user, app)).thenReturn(Optional.of(existing));

        UserApplication result = userApplicationService.associateApplication(user, app);

        assertThat(result).isEqualTo(existing);
        verify(userApplicationRepository, never()).save(any());
    }

    @Test
    void associateApplication_savesNew_whenNotYetAssociated() {
        User user = testUser();
        Application app = testApp();
        UserApplication saved = new UserApplication(user, app);
        saved.setId(100L);
        when(userApplicationRepository.findByUserAndApplication(user, app)).thenReturn(Optional.empty());
        when(userApplicationRepository.save(any(UserApplication.class))).thenReturn(saved);

        UserApplication result = userApplicationService.associateApplication(user, app);

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getApplication()).isEqualTo(app);
        verify(userApplicationRepository).save(any(UserApplication.class));
    }

    @Test
    void dissociateApplication_deletesAssociation_whenExists() {
        User user = testUser();
        Application app = testApp();
        UserApplication ua = new UserApplication(user, app);
        ua.setId(100L);
        when(userApplicationRepository.findByUserAndApplication(user, app)).thenReturn(Optional.of(ua));

        userApplicationService.dissociateApplication(user, app);

        verify(userApplicationRepository).deleteById(100L);
    }

    @Test
    void dissociateApplication_doesNothing_whenNotAssociated() {
        User user = testUser();
        Application app = testApp();
        when(userApplicationRepository.findByUserAndApplication(user, app)).thenReturn(Optional.empty());

        userApplicationService.dissociateApplication(user, app);

        verify(userApplicationRepository, never()).deleteById(any());
    }

    @Test
    void getApplicationsForUser_returnsUserApplications() {
        User user = testUser();
        Application app = testApp();
        List<UserApplication> apps = List.of(new UserApplication(user, app));
        when(userApplicationRepository.findByUser(user)).thenReturn(apps);

        List<UserApplication> result = userApplicationService.getApplicationsForUser(user);

        assertThat(result).isEqualTo(apps);
    }

    @Test
    void startApplication_setsRunningTrueAndSaves() {
        User user = testUser();
        Application app = testApp();
        UserApplication ua = new UserApplication(user, app);
        ua.setId(100L);
        when(userApplicationRepository.findByUserAndApplication(user, app)).thenReturn(Optional.of(ua));
        when(userApplicationRepository.save(ua)).thenReturn(ua);

        userApplicationService.startApplication(user, app);

        assertThat(ua.isRunning()).isTrue();
        verify(userApplicationRepository).save(ua);
    }

    @Test
    void stopApplication_setsRunningFalseAndSaves() {
        User user = testUser();
        Application app = testApp();
        UserApplication ua = new UserApplication(user, app);
        ua.setId(100L);
        ua.setRunning(true);
        when(userApplicationRepository.findByUserAndApplication(user, app)).thenReturn(Optional.of(ua));

        userApplicationService.stopApplication(user, app);

        assertThat(ua.isRunning()).isFalse();
        verify(userApplicationRepository).save(ua);
    }

    @Test
    void associateApplicationByName_findsOrCreatesAppAndAssociates() {
        User user = testUser();
        Application app = testApp();
        when(applicationService.addApplicationIfAbsent("MyApp")).thenReturn(app);
        when(userApplicationRepository.findByUserAndApplication(user, app)).thenReturn(Optional.empty());
        UserApplication saved = new UserApplication(user, app);
        saved.setId(100L);
        when(userApplicationRepository.save(any())).thenReturn(saved);

        userApplicationService.associateApplicationByName(user, "MyApp");

        verify(applicationService).addApplicationIfAbsent("MyApp");
        verify(userApplicationRepository).save(any(UserApplication.class));
    }

    @Test
    void dissociateApplicationById_dissociatesWhenAppFound() {
        User user = testUser();
        Application app = testApp();
        UserApplication ua = new UserApplication(user, app);
        ua.setId(100L);
        when(applicationService.findById(10L)).thenReturn(Optional.of(app));
        when(userApplicationRepository.findByUserAndApplication(user, app)).thenReturn(Optional.of(ua));

        userApplicationService.dissociateApplicationById(user, 10L);

        verify(userApplicationRepository).deleteById(100L);
    }

    @Test
    void startApplicationById_startsWhenAppFound() {
        User user = testUser();
        Application app = testApp();
        UserApplication ua = new UserApplication(user, app);
        ua.setId(100L);
        when(applicationService.findById(10L)).thenReturn(Optional.of(app));
        when(userApplicationRepository.findByUserAndApplication(user, app)).thenReturn(Optional.of(ua));
        when(userApplicationRepository.save(ua)).thenReturn(ua);

        userApplicationService.startApplicationById(user, 10L);

        assertThat(ua.isRunning()).isTrue();
    }

    @Test
    void stopApplicationById_stopsWhenAppFound() {
        User user = testUser();
        Application app = testApp();
        UserApplication ua = new UserApplication(user, app);
        ua.setId(100L);
        ua.setRunning(true);
        when(applicationService.findById(10L)).thenReturn(Optional.of(app));
        when(userApplicationRepository.findByUserAndApplication(user, app)).thenReturn(Optional.of(ua));

        userApplicationService.stopApplicationById(user, 10L);

        assertThat(ua.isRunning()).isFalse();
    }

    @Test
    void getApplicationNameById_returnsName_whenFound() {
        Application app = testApp();
        when(applicationService.findById(10L)).thenReturn(Optional.of(app));

        Optional<String> name = userApplicationService.getApplicationNameById(10L);

        assertThat(name).contains("MyApp");
    }

    @Test
    void getApplicationNameById_returnsEmpty_whenNotFound() {
        when(applicationService.findById(99L)).thenReturn(Optional.empty());

        assertThat(userApplicationService.getApplicationNameById(99L)).isEmpty();
    }

    @Test
    void removeAllForUser_deletesAllUserApplications() {
        User user = testUser();

        userApplicationService.removeAllForUser(user);

        verify(userApplicationRepository).deleteByUser(user);
    }
}
