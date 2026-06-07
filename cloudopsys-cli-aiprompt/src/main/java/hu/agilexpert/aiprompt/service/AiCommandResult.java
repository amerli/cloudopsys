package hu.agilexpert.aiprompt.service;

public record AiCommandResult(boolean success, String message) {

    public static AiCommandResult ok(String message) {
        return new AiCommandResult(true, message);
    }

    public static AiCommandResult error(String message) {
        return new AiCommandResult(false, message);
    }
}
