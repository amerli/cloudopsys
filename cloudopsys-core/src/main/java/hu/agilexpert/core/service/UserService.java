package hu.agilexpert.core.service;

import hu.agilexpert.core.exception.DuplicateIdentifierException;
import hu.agilexpert.core.model.Theme;
import hu.agilexpert.core.model.User;
import hu.agilexpert.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserProvisioningService userProvisioningService;
    private final UserAssetService userAssetService;
    private final UserApplicationService userApplicationService;

    @Autowired
    public UserService(UserRepository userRepository,
                       UserProvisioningService userProvisioningService,
                       @Lazy UserAssetService userAssetService,
                       @Lazy UserApplicationService userApplicationService) {
        this.userRepository = userRepository;
        this.userProvisioningService = userProvisioningService;
        this.userAssetService = userAssetService;
        this.userApplicationService = userApplicationService;
    }

    public User addUser(String name, String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateIdentifierException("Username '" + username + "' is already taken.");
        }
        User user = userRepository.save(new User(name, username));
        userProvisioningService.applyDefaults(user);
        userRepository.save(user);
        return user;
    }

    public Iterable<User> listUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void updateUser(User user, String newName) {
        user.setName(newName);
        userRepository.save(user);
    }

    public void setTheme(User user, Theme theme) {
        user.setTheme(theme);
        userRepository.save(user);
    }

    public void removeUser(Long id) {
        userRepository.deleteById(id);
    }

    public void deleteUser(Long id) {
        findById(id).ifPresent(user -> {
            userAssetService.removeAllAssetsForUser(user);
            userApplicationService.removeAllForUser(user);
            userRepository.deleteById(id);
        });
    }
}
