package hu.agilexpert.core.service;

import hu.agilexpert.core.model.Application;
import hu.agilexpert.core.repository.ApplicationRepository;
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
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private Application testApp(String name) {
        Application app = new Application(name);
        app.setId(1L);
        return app;
    }

    @Test
    void addApplicationIfAbsent_returnsExisting_whenFound() {
        Application existing = testApp("Excel");
        when(applicationRepository.findByNameIgnoreCase("Excel")).thenReturn(Optional.of(existing));

        Application result = applicationService.addApplicationIfAbsent("Excel");

        assertThat(result).isEqualTo(existing);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void addApplicationIfAbsent_createsNew_whenNotFound() {
        Application saved = testApp("Word");
        when(applicationRepository.findByNameIgnoreCase("Word")).thenReturn(Optional.empty());
        when(applicationRepository.save(any(Application.class))).thenReturn(saved);

        Application result = applicationService.addApplicationIfAbsent("Word");

        assertThat(result).isEqualTo(saved);
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void findById_returnsApplication_whenFound() {
        Application app = testApp("Maps");
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));

        Optional<Application> result = applicationService.findById(1L);

        assertThat(result).contains(app);
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(applicationService.findById(99L)).isEmpty();
    }

    @Test
    void findByName_returnsApplication_whenFound() {
        Application app = testApp("Calendar");
        when(applicationRepository.findByNameIgnoreCase("Calendar")).thenReturn(Optional.of(app));

        assertThat(applicationService.findByName("Calendar")).contains(app);
    }

    @Test
    void findByName_returnsEmpty_whenNotFound() {
        when(applicationRepository.findByNameIgnoreCase("Unknown")).thenReturn(Optional.empty());

        assertThat(applicationService.findByName("Unknown")).isEmpty();
    }

    @Test
    void listApplications_returnsAllApplications() {
        List<Application> apps = List.of(testApp("Excel"), testApp("Word"));
        when(applicationRepository.findAll()).thenReturn(apps);

        List<Application> result = applicationService.listApplications();

        assertThat(result).hasSize(2).containsExactlyElementsOf(apps);
    }

    @Test
    void removeApplication_deletesById() {
        applicationService.removeApplication(5L);

        verify(applicationRepository).deleteById(5L);
    }
}
