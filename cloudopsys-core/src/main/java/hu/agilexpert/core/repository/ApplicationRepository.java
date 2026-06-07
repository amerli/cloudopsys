package hu.agilexpert.core.repository;

import hu.agilexpert.core.model.Application;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ApplicationRepository extends CrudRepository<Application, Long> {
    Optional<Application> findByNameIgnoreCase(String name);
}
