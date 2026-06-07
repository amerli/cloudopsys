package hu.agilexpert.web;

import hu.agilexpert.core.client.CoreApiClient;
import hu.agilexpert.core.dto.UserDto;
import hu.agilexpert.core.model.Theme;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UserController {

    private static final String REDIRECT_USERS = "redirect:/users";
    private static final String REDIRECT_USER_PREFIX = "redirect:/users/";
    private static final String PROFILE_SUFFIX = "/profile";
    private static final String APPS_SUFFIX = "/apps";
    private static final String FLASH_MESSAGE = "message";
    private static final String FLASH_ERROR = "error";

    private final CoreApiClient coreApiClient;

    @GetMapping("/")
    public String index() {
        return REDIRECT_USERS;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", coreApiClient.listUsers());
        return "users";
    }

    @PostMapping("/users/add")
    public String addUser(@RequestParam String name, @RequestParam String username,
                          RedirectAttributes redirectAttributes) {
        try {
            coreApiClient.addUser(name, username);
            redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "User '" + name + "' added with defaults.");
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                redirectAttributes.addFlashAttribute(FLASH_ERROR, "Username '" + username + "' is already taken.");
            } else {
                redirectAttributes.addFlashAttribute(FLASH_ERROR, "Failed to add user: " + e.getMessage());
            }
        }
        return REDIRECT_USERS;
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        coreApiClient.deleteUser(id);
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "User removed.");
        return REDIRECT_USERS;
    }

    @GetMapping("/users/{id}/profile")
    public String profile(@PathVariable Long id, Model model) {
        return coreApiClient.findUserById(id).map(user -> {
            model.addAttribute("user", user);
            model.addAttribute("assets", coreApiClient.getAssetsByUser(id));
            model.addAttribute("availableIcons", coreApiClient.getAvailableIconsForUser(id));
            model.addAttribute("availableBackgrounds", coreApiClient.getAvailableBackgroundsForUser(id));
            model.addAttribute("themes", Theme.values());
            return "profile";
        }).orElse(REDIRECT_USERS);
    }

    @PostMapping("/users/{id}/profile/name")
    public String updateName(@PathVariable Long id, @RequestParam String name,
                             RedirectAttributes redirectAttributes) {
        coreApiClient.updateUserName(id, name);
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "Name updated.");
        return REDIRECT_USER_PREFIX + id + PROFILE_SUFFIX;
    }

    @PostMapping("/users/{id}/profile/theme")
    public String updateTheme(@PathVariable Long id, @RequestParam String theme,
                              RedirectAttributes redirectAttributes) {
        try {
            coreApiClient.updateUserTheme(id, theme);
            redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "Theme updated.");
        } catch (HttpClientErrorException e) {
            redirectAttributes.addFlashAttribute(FLASH_ERROR, "Invalid theme.");
        }
        return REDIRECT_USER_PREFIX + id + PROFILE_SUFFIX;
    }

    @PostMapping("/users/{id}/profile/icon")
    public String addIcon(@PathVariable Long id, @RequestParam String fileName,
                          RedirectAttributes redirectAttributes) {
        try {
            coreApiClient.addIcon(id, fileName);
            redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "Icon added (PRIVATE).");
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                redirectAttributes.addFlashAttribute(FLASH_ERROR, "Asset with fileName '" + fileName + "' already exists.");
            } else {
                redirectAttributes.addFlashAttribute(FLASH_ERROR, "Failed to add icon: " + e.getMessage());
            }
        }
        return REDIRECT_USER_PREFIX + id + PROFILE_SUFFIX;
    }

    @PostMapping("/users/{id}/profile/bg")
    public String addBg(@PathVariable Long id, @RequestParam String fileName,
                        RedirectAttributes redirectAttributes) {
        try {
            coreApiClient.addBackground(id, fileName);
            redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "Background added (PRIVATE).");
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                redirectAttributes.addFlashAttribute(FLASH_ERROR, "Asset with fileName '" + fileName + "' already exists.");
            } else {
                redirectAttributes.addFlashAttribute(FLASH_ERROR, "Failed to add background: " + e.getMessage());
            }
        }
        return REDIRECT_USER_PREFIX + id + PROFILE_SUFFIX;
    }

    @PostMapping("/users/{id}/profile/asset/{assetId}/delete")
    public String deleteAsset(@PathVariable Long id, @PathVariable Long assetId,
                              RedirectAttributes redirectAttributes) {
        coreApiClient.removeAssetFromUser(id, assetId);
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "Asset removed.");
        return REDIRECT_USER_PREFIX + id + PROFILE_SUFFIX;
    }

    @PostMapping("/users/{id}/profile/icon/activate")
    public String activateIcon(@PathVariable Long id, @RequestParam Long assetId,
                               RedirectAttributes redirectAttributes) {
        coreApiClient.setActiveIcon(id, assetId);
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "Active icon updated.");
        return REDIRECT_USER_PREFIX + id + PROFILE_SUFFIX;
    }

    @PostMapping("/users/{id}/profile/bg/activate")
    public String activateBackground(@PathVariable Long id, @RequestParam Long assetId,
                                     RedirectAttributes redirectAttributes) {
        coreApiClient.setActiveBackground(id, assetId);
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "Active background updated.");
        return REDIRECT_USER_PREFIX + id + PROFILE_SUFFIX;
    }

    @PostMapping("/users/{id}/profile/asset/{assetId}/make-public")
    public String makeAssetPublic(@PathVariable Long id, @PathVariable Long assetId,
                                  RedirectAttributes redirectAttributes) {
        coreApiClient.makeAssetPublic(id, assetId);
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "Asset is now PUBLIC.");
        return REDIRECT_USER_PREFIX + id + PROFILE_SUFFIX;
    }

    @GetMapping("/users/{id}/apps")
    public String apps(@PathVariable Long id, Model model) {
        return coreApiClient.findUserById(id).map(user -> {
            model.addAttribute("user", user);
            model.addAttribute("userApps", coreApiClient.getAppsForUser(id));
            model.addAttribute("allApps", coreApiClient.listAllApplications());
            return "apps";
        }).orElse(REDIRECT_USERS);
    }

    @PostMapping("/users/{id}/apps/add")
    public String addApp(@PathVariable Long id, @RequestParam String appName,
                         RedirectAttributes redirectAttributes) {
        coreApiClient.associateApp(id, appName);
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "Application '" + appName + "' associated.");
        return REDIRECT_USER_PREFIX + id + APPS_SUFFIX;
    }

    @PostMapping("/users/{id}/apps/{appId}/remove")
    public String removeApp(@PathVariable Long id, @PathVariable Long appId,
                            RedirectAttributes redirectAttributes) {
        coreApiClient.dissociateApp(id, appId);
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "Application removed.");
        return REDIRECT_USER_PREFIX + id + APPS_SUFFIX;
    }

    @PostMapping("/users/{id}/apps/{appId}/start")
    public String startApp(@PathVariable Long id, @PathVariable Long appId,
                           RedirectAttributes redirectAttributes) {
        coreApiClient.startApp(id, appId);
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "Application started.");
        return REDIRECT_USER_PREFIX + id + APPS_SUFFIX;
    }

    @PostMapping("/users/{id}/apps/{appId}/stop")
    public String stopApp(@PathVariable Long id, @PathVariable Long appId,
                          RedirectAttributes redirectAttributes) {
        coreApiClient.stopApp(id, appId);
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "Application stopped.");
        return REDIRECT_USER_PREFIX + id + APPS_SUFFIX;
    }
}
