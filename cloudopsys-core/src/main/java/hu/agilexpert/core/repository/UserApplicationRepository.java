package hu.agilexpert.core.repository;

import hu.agilexpert.core.model.Application;
import hu.agilexpert.core.model.User;
import hu.agilexpert.core.model.UserApplication;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserApplicationRepository extends CrudRepository<UserApplication, Long> {
    List<UserApplication> findByUser(User user);
    Optional<UserApplication> findByUserAndApplication(User user, Application application);
    void deleteByUser(User user);
}
