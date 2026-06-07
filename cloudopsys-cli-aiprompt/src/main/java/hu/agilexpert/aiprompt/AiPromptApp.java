package hu.agilexpert.aiprompt;

import hu.agilexpert.core.client.CoreApiClient;
import hu.agilexpert.aiprompt.service.AiCommandResult;
import hu.agilexpert.aiprompt.service.AiCommandService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class AiPromptApp {

    public static void main(String[] args) {
        SpringApplication.run(AiPromptApp.class, args);
    }

    @Bean
    public CoreApiClient coreApiClient(@Value("${core.api.base-url}") String baseUrl) {
        return new CoreApiClient(baseUrl);
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public CommandLineRunner commandLineRunner(AiCommandService aiCommandService) {
        return args -> {
            Scanner scanner = new Scanner(System.in);

            System.out.println("=== CloudOpsys AI Prompt Console ===");
            System.out.println("Type your prompt in Hungarian or English (or 'exit' to quit).");
            System.out.println("Examples:");
            System.out.println("  indítsd el az Excel alkalmazást Alice felhasználónak");
            System.out.println("  stop Word for Bob");
            System.out.println("  listázd Alice alkalmazásait");
            System.out.println("  set theme for Alice to DARK");
            System.out.println("  listázd a felhasználókat");
            System.out.println("  add user Alice with username alice");
            System.out.println();
            System.out.println("Type 'szimuláció' or 'simulation' to auto-generate and load test data.");
            System.out.println();

            while (true) {
                System.out.print("> ");
                String prompt = scanner.nextLine();

                if (prompt == null || prompt.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                if (prompt.isBlank()) {
                    continue;
                }

                AiCommandResult result = aiCommandService.processPrompt(prompt);
                if (result.success()) {
                    System.out.println("[OK] " + result.message());
                } else {
                    System.out.println("[ERROR] " + result.message());
                }
                System.out.println();
            }
        };
    }
}
