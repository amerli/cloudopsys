package hu.agilexpert.aiprompt.service;

import hu.agilexpert.aiprompt.tools.CloudOpsysTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Processes natural language prompts (Hungarian or English) using Spring AI Tool Calling.
 * Instead of asking the LLM to return a pipe-delimited command string and manually
 * parsing/dispatching it, the LLM is given a set of @Tool-annotated methods
 * (CloudOpsysTools) and autonomously decides which tool to invoke with what arguments.
 * Spring AI handles the full tool-call lifecycle: schema generation, LLM round-trip,
 * argument binding, and result collection.
 */
@Service
@RequiredArgsConstructor
public class AiCommandService {

    private final ChatClient chatClient;
    private final CloudOpsysTools cloudOpsysTools;

    private static final String SYSTEM_PROMPT = """
            You are an assistant for a user-management system called CloudOpsys.
            The user will give you instructions in Hungarian or English.
            Use the available tools to fulfil the request.
            Always call exactly one tool per request.
            For theme-related requests: map "sötét"/"dark" → DARK, "világos"/"bright" → BRIGHT.
            """;

    private static final String SIMULATION_PROMPT = """
            You are a test-data generator for a user-management system called CloudOpsys.
            Generate a realistic set of example commands that together cover ALL of the following command types:
              ADD_USER|<name>|<username>
              START_APP|<appName>|<userIdentifier>
              STOP_APP|<appName>|<userIdentifier>
              APP_STATUS|<appName>|<userIdentifier>
              LIST_APPS|<userIdentifier>
              SET_THEME|<userIdentifier>|<DARK|BRIGHT>
              LIST_USERS

            Rules:
            - Invent 3–4 fictional users with realistic Hungarian or English names and usernames.
            - Use a variety of application names (e.g. Excel, Word, Maps, Notepad, Chrome).
            - Include at least one of every command type listed above.
            - Reply with ONLY the command lines, one per line, no explanation, no numbering, no extra text.
            - ADD_USER commands must come before any command that references that user.
            - START_APP for a given user+app must come before any STOP_APP or APP_STATUS for the same user+app.
            """;

    public AiCommandResult processPrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return AiCommandResult.error("Empty prompt.");
        }

        String trimmed = prompt.trim();
        if (trimmed.equalsIgnoreCase("szimuláció") || trimmed.equalsIgnoreCase("szimulacio")
                || trimmed.equalsIgnoreCase("simulation")) {
            return handleSimulation();
        }

        try {
            String response = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(trimmed)
                    .tools(cloudOpsysTools)
                    .call()
                    .content();
            return AiCommandResult.ok(response != null ? response : "Done.");
        } catch (Exception exception) {
            return AiCommandResult.error("LLM call failed: " + exception.getMessage());
        }
    }

    /**
     * Simulation: LLM generates a batch of pipe-delimited commands which are
     * then dispatched one-by-one through the tool layer via individual prompts.
     */
     private AiCommandResult handleSimulation() {
        String generatedCommands;
        try {
            generatedCommands = chatClient.prompt()
                    .user(SIMULATION_PROMPT)
                    .call()
                    .content();
            if (generatedCommands == null) {
                return AiCommandResult.error("LLM returned no simulation commands.");
            }
            generatedCommands = generatedCommands.trim();
        } catch (Exception exception) {
            return AiCommandResult.error("LLM call failed during simulation: " + exception.getMessage());
        }

        StringBuilder report = new StringBuilder("=== Simulation Results ===\n");
        int successCount = 0;
        int errorCount = 0;

        for (String line : generatedCommands.split("\\r?\\n")) {
            String commandLine = line.trim();
            if (commandLine.isBlank()) {
                continue;
            }
            AiCommandResult result = dispatchSimulationCommand(commandLine);
            if (result.success()) {
                report.append("  [OK]    ").append(result.message()).append("\n");
                successCount++;
            } else {
                report.append("  [ERROR] ").append(result.message())
                      .append(" (cmd: ").append(commandLine).append(")\n");
                errorCount++;
            }
        }

        report.append("=== Done: ").append(successCount).append(" succeeded, ")
              .append(errorCount).append(" failed ===");
        return AiCommandResult.ok(report.toString());
    }

    /**
     * Executes a single pipe-delimited simulation command directly via the tool layer,
     * bypassing the LLM for efficiency (the LLM already generated the commands).
     */
    private AiCommandResult dispatchSimulationCommand(String commandLine) {
        String[] parts = commandLine.split("\\|", -1);
        String command = parts[0].trim().toUpperCase();

        try {
            return switch (command) {
                case "ADD_USER"   -> parts.length >= 3
                        ? AiCommandResult.ok(cloudOpsysTools.addUser(parts[1].trim(), parts[2].trim()))
                        : AiCommandResult.error("Malformed ADD_USER command.");
                case "START_APP"  -> parts.length >= 3
                        ? AiCommandResult.ok(cloudOpsysTools.startApplication(parts[1].trim(), parts[2].trim()))
                        : AiCommandResult.error("Malformed START_APP command.");
                case "STOP_APP"   -> parts.length >= 3
                        ? AiCommandResult.ok(cloudOpsysTools.stopApplication(parts[1].trim(), parts[2].trim()))
                        : AiCommandResult.error("Malformed STOP_APP command.");
                case "APP_STATUS" -> parts.length >= 3
                        ? AiCommandResult.ok(cloudOpsysTools.getApplicationStatus(parts[1].trim(), parts[2].trim()))
                        : AiCommandResult.error("Malformed APP_STATUS command.");
                case "LIST_APPS"  -> parts.length >= 2
                        ? AiCommandResult.ok(cloudOpsysTools.listApplicationsForUser(parts[1].trim()))
                        : AiCommandResult.error("Malformed LIST_APPS command.");
                case "SET_THEME"  -> parts.length >= 3
                        ? AiCommandResult.ok(cloudOpsysTools.setTheme(parts[1].trim(), parts[2].trim()))
                        : AiCommandResult.error("Malformed SET_THEME command.");
                case "LIST_USERS" -> AiCommandResult.ok(cloudOpsysTools.listUsers());
                default           -> AiCommandResult.error("Unrecognised simulation command: " + commandLine);
            };
        } catch (Exception exception) {
            return AiCommandResult.error(exception.getMessage());
        }
    }
}
