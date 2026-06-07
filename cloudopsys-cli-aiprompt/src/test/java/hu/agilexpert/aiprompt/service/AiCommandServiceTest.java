package hu.agilexpert.aiprompt.service;

import hu.agilexpert.aiprompt.tools.CloudOpsysTools;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiCommandServiceTest {

    @Mock
    private ChatClient chatClient;
    @Mock
    private CloudOpsysTools cloudOpsysTools;

    @InjectMocks
    private AiCommandService aiCommandService;

    @Test
    void processPrompt_returnsError_whenPromptIsNull() {
        AiCommandResult result = aiCommandService.processPrompt(null);

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("Empty prompt");
    }

    @Test
    void processPrompt_returnsError_whenPromptIsBlank() {
        AiCommandResult result = aiCommandService.processPrompt("   ");

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("Empty prompt");
    }

    // ── dispatchSimulationCommand (via handleSimulation path) ─────────────────

    @Test
    void dispatchSimulationCommand_addUser_callsToolAndReturnsOk() {
        when(cloudOpsysTools.addUser("Alice", "alice")).thenReturn("User 'Alice' (@alice) created with defaults.");

        // Simulate the dispatch directly by calling processPrompt with a mocked ChatClient
        // that returns a single ADD_USER command line
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("ADD_USER|Alice|alice");

        AiCommandResult result = aiCommandService.processPrompt("simulation");

        assertThat(result.success()).isTrue();
        assertThat(result.message()).contains("OK");
        verify(cloudOpsysTools).addUser("Alice", "alice");
    }

    @Test
    void dispatchSimulationCommand_listUsers_callsToolAndReturnsOk() {
        when(cloudOpsysTools.listUsers()).thenReturn("Users: Alice (@alice)");

        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("LIST_USERS");

        AiCommandResult result = aiCommandService.processPrompt("simulation");

        assertThat(result.success()).isTrue();
        verify(cloudOpsysTools).listUsers();
    }

    @Test
    void dispatchSimulationCommand_unknownCommand_reportsError() {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("UNKNOWN_CMD|foo");

        AiCommandResult result = aiCommandService.processPrompt("szimulacio");

        assertThat(result.success()).isTrue();
        assertThat(result.message()).contains("ERROR").contains("Unrecognised");
    }

    @Test
    void dispatchSimulationCommand_malformedAddUser_reportsError() {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("ADD_USER|OnlyOnePart");

        AiCommandResult result = aiCommandService.processPrompt("simulation");

        assertThat(result.success()).isTrue();
        assertThat(result.message()).contains("ERROR").contains("Malformed");
    }

    @Test
    void handleSimulation_returnsError_whenLlmReturnsNull() {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn(null);

        AiCommandResult result = aiCommandService.processPrompt("simulation");

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("no simulation commands");
    }

    @Test
    void handleSimulation_returnsError_whenLlmThrows() {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(new RuntimeException("LLM unavailable"));

        AiCommandResult result = aiCommandService.processPrompt("simulation");

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("LLM call failed");
    }
}
