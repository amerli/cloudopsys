package hu.agilexpert.core.service;

import hu.agilexpert.core.model.Application;
import hu.agilexpert.core.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public Application addApplicationIfAbsent(String name) {
        return applicationRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> applicationRepository.save(new Application(name)));
    }

    public Optional<Application> findById(Long id) {
        return applicationRepository.findById(id);
    }

    public Optional<Application> findByName(String name) {
        return applicationRepository.findByNameIgnoreCase(name);
    }

    public List<Application> listApplications() {
        return (List<Application>) applicationRepository.findAll();
    }

    public void removeApplication(Long id) {
        applicationRepository.deleteById(id);
    }
}
