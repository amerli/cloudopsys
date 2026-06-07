package hu.agilexpert.quickstart;

import hu.agilexpert.core.client.CoreApiClient;
import hu.agilexpert.core.dto.UserDto;
import hu.agilexpert.core.model.Theme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

/**
 * CloudOpsys CLI Quickstart — a beginner-friendly wizard that walks a new user
 * through the initial setup of their profile step by step.
 */
@SpringBootApplication
public class QuickstartApp {

    public static void main(String[] args) {
        SpringApplication.run(QuickstartApp.class, args);
    }

    @Bean
    public CoreApiClient coreApiClient(@Value("${core.api.base-url}") String baseUrl) {
        return new CoreApiClient(baseUrl);
    }

    @Bean
    public CommandLineRunner commandLineRunner(CoreApiClient coreApiClient) {
        return args -> {
            Scanner scanner = new Scanner(System.in);
            printBanner();
            System.out.println("This wizard will help you set up your initial CloudOpsys configuration.");
            System.out.println("Just follow the steps — press Enter to accept the default value shown in [brackets].");
            System.out.println();

            // ── Step 1: Create a user ────────────────────────────────────────────────────
            printStep(1, "Create your user profile");
            String name = prompt(scanner, "  Your full name", "New User");
            String username = prompt(scanner, "  Choose a username (unique, no spaces)", "user1");

            UserDto user;
            try {
                user = coreApiClient.addUser(name, username)
                        .orElseThrow(() -> new RuntimeException("Username already taken"));
                printSuccess("User '" + name + "' (@" + username + ") created with default settings.");
            } catch (RuntimeException e) {
                printWarning("Username '" + username + "' is already taken. Loading first existing user...");
                user = coreApiClient.listUsers().stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("No users found and could not create one."));
                printSuccess("Continuing with existing user: " + user.name() + " (@" + user.username() + ")");
            }
            System.out.println();

            // ── Step 2: Choose a theme ───────────────────────────────────────────────────
            printStep(2, "Choose your UI theme");
            System.out.println("  Available themes: BRIGHT, DARK");
            String themeInput = prompt(scanner, "  Your preferred theme", "BRIGHT");
            try {
                Theme.valueOf(themeInput.trim().toUpperCase());
                coreApiClient.updateUserTheme(user.id(), themeInput.trim().toUpperCase());
                printSuccess("Theme set to " + themeInput.trim().toUpperCase() + ".");
            } catch (IllegalArgumentException e) {
                printWarning("Unknown theme '" + themeInput + "', keeping default (BRIGHT).");
            }
            System.out.println();

            // ── Step 3: Add a profile icon ───────────────────────────────────────────────
            printStep(3, "Set your profile icon");
            System.out.println("  A default icon (default-icon.png) has already been assigned.");
            String iconFileName = prompt(scanner, "  Add a custom icon file name (or press Enter to skip)", "");
            if (!iconFileName.isBlank()) {
                if (coreApiClient.addIcon(user.id(), iconFileName).isPresent()) {
                    printSuccess("Icon '" + iconFileName + "' added (PRIVATE).");
                } else {
                    printWarning("An asset with that file name already exists — skipping.");
                }
            } else {
                System.out.println("  Skipped — using default icon.");
            }
            System.out.println();

            // ── Step 4: Add a background picture ────────────────────────────────────────
            printStep(4, "Set your background picture");
            System.out.println("  A default background (default-bg.png) has already been assigned.");
            String bgFileName = prompt(scanner, "  Add a custom background file name (or press Enter to skip)", "");
            if (!bgFileName.isBlank()) {
                if (coreApiClient.addBackground(user.id(), bgFileName).isPresent()) {
                    printSuccess("Background '" + bgFileName + "' added (PRIVATE).");
                } else {
                    printWarning("An asset with that file name already exists — skipping.");
                }
            } else {
                System.out.println("  Skipped — using default background.");
            }
            System.out.println();

            // ── Step 5: Associate applications ──────────────────────────────────────────
            printStep(5, "Associate applications");
            System.out.println("  Excel and Word are already associated by default.");
            System.out.println("  You can add more applications now (e.g. 'Maps', 'Calendar').");
            while (true) {
                String appName = prompt(scanner, "  Enter an application name to add (or press Enter to finish)", "");
                if (appName.isBlank()) break;
                coreApiClient.associateApp(user.id(), appName);
                printSuccess("'" + appName + "' associated.");
            }
            System.out.println();

            // ── Summary ──────────────────────────────────────────────────────────────────
            printStep(6, "Setup complete! Here is your profile summary");
            System.out.println();

            // Reload user to get fresh data
            user = coreApiClient.findUserById(user.id()).orElse(user);
            System.out.println("  Name     : " + user.name());
            System.out.println("  Username : @" + user.username());
            System.out.println("  Theme    : " + (user.theme() != null ? user.theme() : "BRIGHT"));
            System.out.println();

            System.out.println("  Assets:");
            coreApiClient.getAssetsByUser(user.id()).forEach(asset ->
                    System.out.println("    [" + asset.type() + "] " + asset.fileName()
                            + " (" + asset.visibility() + ")"));
            System.out.println();

            System.out.println("  Applications:");
            coreApiClient.getAppsForUser(user.id()).forEach(ua ->
                    System.out.println("    - " + ua.application().name()
                            + " [" + (ua.running() ? "RUNNING" : "STOPPED") + "]"));
            System.out.println();

            System.out.println("════════════════════════════════════════════════════════════════════════");
            System.out.println("  All done! You can now use the web UI or the AI prompt console");
            System.out.println("  to manage your CloudOpsys configuration further.");
            System.out.println("════════════════════════════════════════════════════════════════════════");
            System.out.println();
        };
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private static void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║          CloudOpsys CLI Quickstart — Initial Setup Wizard           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void printStep(int number, String title) {
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("  Step " + number + ": " + title);
        System.out.println("──────────────────────────────────────────────────────────────────────");
    }

    private static void printSuccess(String message) {
        System.out.println("  ✔ " + message);
    }

    private static void printWarning(String message) {
        System.out.println("  ⚠ " + message);
    }

    private static String prompt(Scanner scanner, String label, String defaultValue) {
        if (defaultValue.isBlank()) {
            System.out.print(label + ": ");
        } else {
            System.out.print(label + " [" + defaultValue + "]: ");
        }
        String input = scanner.nextLine().trim();
        return input.isBlank() ? defaultValue : input;
    }
}
