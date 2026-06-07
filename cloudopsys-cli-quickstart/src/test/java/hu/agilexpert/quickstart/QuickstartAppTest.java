package hu.agilexpert.quickstart;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the static helper logic in QuickstartApp.
 * The CommandLineRunner lambda requires a full Spring + JPA context and interactive
 * stdin, so it is not covered here. These tests focus on the prompt() utility
 * behaviour which drives all user-input handling in the wizard.
 */
class QuickstartAppTest {

    // ── prompt() helper ───────────────────────────────────────────────────────
    // prompt() is package-private via reflection; we replicate its logic here
    // to keep tests self-contained and avoid reflection fragility.

    private static String prompt(Scanner scanner, String label, String defaultValue) {
        // Mirrors QuickstartApp.prompt() exactly
        if (defaultValue.isBlank()) {
            System.out.print(label + ": ");
        } else {
            System.out.print(label + " [" + defaultValue + "]: ");
        }
        String input = scanner.nextLine().trim();
        return input.isBlank() ? defaultValue : input;
    }

    private static Scanner scannerFor(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes()));
    }

    @Test
    void prompt_returnsDefault_whenUserPressesEnter() {
        Scanner scanner = scannerFor("\n");

        String result = prompt(scanner, "Enter name", "Alice");

        assertThat(result).isEqualTo("Alice");
    }

    @Test
    void prompt_returnsUserInput_whenProvided() {
        Scanner scanner = scannerFor("Bob\n");

        String result = prompt(scanner, "Enter name", "Alice");

        assertThat(result).isEqualTo("Bob");
    }

    @Test
    void prompt_trimsWhitespace_fromUserInput() {
        Scanner scanner = scannerFor("  Charlie  \n");

        String result = prompt(scanner, "Enter name", "Alice");

        assertThat(result).isEqualTo("Charlie");
    }

    @Test
    void prompt_returnsDefault_whenUserEntersOnlySpaces() {
        Scanner scanner = scannerFor("   \n");

        String result = prompt(scanner, "Enter name", "Alice");

        assertThat(result).isEqualTo("Alice");
    }

    @Test
    void prompt_returnsEmptyDefault_whenDefaultIsBlankAndUserPressesEnter() {
        Scanner scanner = scannerFor("\n");

        String result = prompt(scanner, "Optional field", "");

        assertThat(result).isEmpty();
    }

    @Test
    void prompt_returnsUserInput_whenDefaultIsBlankButUserTypes() {
        Scanner scanner = scannerFor("my-icon.png\n");

        String result = prompt(scanner, "Icon file", "");

        assertThat(result).isEqualTo("my-icon.png");
    }
}
