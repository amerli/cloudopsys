package hu.agilexpert.core.service;

import hu.agilexpert.core.model.Application;
import hu.agilexpert.core.model.User;
import hu.agilexpert.core.model.UserApplication;
import hu.agilexpert.core.repository.UserApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserApplicationRepository userApplicationRepository;
    private final ApplicationService applicationService;

    public UserApplication associateApplication(User user, Application application) {
        return userApplicationRepository.findByUserAndApplication(user, application)
                .orElseGet(() -> userApplicationRepository.save(new UserApplication(user, application)));
    }

    public void dissociateApplication(User user, Application application) {
        userApplicationRepository.findByUserAndApplication(user, application)
                .ifPresent(userApplication -> userApplicationRepository.deleteById(userApplication.getId()));
    }

    public List<UserApplication> getApplicationsForUser(User user) {
        return userApplicationRepository.findByUser(user);
    }

    public Optional<UserApplication> findByUserAndApplication(User user, Application application) {
        return userApplicationRepository.findByUserAndApplication(user, application);
    }

    public void startApplication(User user, Application application) {
        UserApplication userApplication = associateApplication(user, application);
        userApplication.setRunning(true);
        userApplicationRepository.save(userApplication);
    }

    public void stopApplication(User user, Application application) {
        userApplicationRepository.findByUserAndApplication(user, application).ifPresent(userApplication -> {
            userApplication.setRunning(false);
            userApplicationRepository.save(userApplication);
        });
    }

    @Transactional
    public void removeAllForUser(User user) {
        userApplicationRepository.deleteByUser(user);
    }

    public void associateApplicationByName(User user, String appName) {
        Application application = applicationService.addApplicationIfAbsent(appName);
        associateApplication(user, application);
    }

    public void dissociateApplicationById(User user, Long appId) {
        applicationService.findById(appId)
                .ifPresent(application -> dissociateApplication(user, application));
    }

    public void startApplicationById(User user, Long appId) {
        applicationService.findById(appId)
                .ifPresent(application -> startApplication(user, application));
    }

    public void stopApplicationById(User user, Long appId) {
        applicationService.findById(appId)
                .ifPresent(application -> stopApplication(user, application));
    }

    public Optional<String> getApplicationNameById(Long appId) {
        return applicationService.findById(appId).map(Application::getName);
    }

    public Iterable<Application> listAllApplications() {
        return applicationService.listApplications();
    }
}
