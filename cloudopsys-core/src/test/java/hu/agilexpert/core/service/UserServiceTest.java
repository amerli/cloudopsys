package hu.agilexpert.core.service;

import hu.agilexpert.core.exception.DuplicateIdentifierException;
import hu.agilexpert.core.model.Theme;
import hu.agilexpert.core.model.User;
import hu.agilexpert.core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProvisioningService userProvisioningService;
    @Mock
    private UserAssetService userAssetService;
    @Mock
    private UserApplicationService userApplicationService;

    @InjectMocks
    private UserService userService;

    @Test
    void addUser_savesAndReturnsUser() {
        when(userRepository.findByUsername("jdoe")).thenReturn(Optional.empty());
        User saved = new User("John Doe", "jdoe");
        saved.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.addUser("John Doe", "jdoe");

        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getUsername()).isEqualTo("jdoe");
        verify(userProvisioningService).applyDefaults(saved);
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void addUser_throwsDuplicateIdentifierException_whenUsernameAlreadyTaken() {
        when(userRepository.findByUsername("jdoe")).thenReturn(Optional.of(new User("Existing", "jdoe")));

        assertThatThrownBy(() -> userService.addUser("John Doe", "jdoe"))
                .isInstanceOf(DuplicateIdentifierException.class)
                .hasMessageContaining("jdoe");

        verify(userRepository, never()).save(any());
    }

    @Test
    void listUsers_returnsAllUsers() {
        List<User> users = List.of(new User("Alice", "alice"), new User("Bob", "bob"));
        when(userRepository.findAll()).thenReturn(users);

        Iterable<User> result = userService.listUsers();

        assertThat(result).containsExactlyElementsOf(users);
    }

    @Test
    void findById_returnsUser_whenExists() {
        User user = new User("Alice", "alice");
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(1L);

        assertThat(result).isPresent().contains(user);
    }

    @Test
    void findById_returnsEmpty_whenNotExists() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(userService.findById(99L)).isEmpty();
    }

    @Test
    void updateUser_setsNameAndSaves() {
        User user = new User("Old Name", "alice");

        userService.updateUser(user, "New Name");

        assertThat(user.getName()).isEqualTo("New Name");
        verify(userRepository).save(user);
    }

    @Test
    void setTheme_setsThemeAndSaves() {
        User user = new User("Alice", "alice");

        userService.setTheme(user, Theme.DARK);

        assertThat(user.getTheme()).isEqualTo(Theme.DARK);
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_removesAssetsAppsAndUser_whenUserExists() {
        User user = new User("Alice", "alice");
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userAssetService).removeAllAssetsForUser(user);
        verify(userApplicationService).removeAllForUser(user);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_doesNothing_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        userService.deleteUser(99L);

        verify(userAssetService, never()).removeAllAssetsForUser(any());
        verify(userRepository, never()).deleteById(any());
    }
}
