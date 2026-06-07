package hu.agilexpert.core.controller;

import hu.agilexpert.core.dto.UserDto;
import hu.agilexpert.core.exception.DuplicateIdentifierException;
import hu.agilexpert.core.mapper.UserMapper;
import hu.agilexpert.core.model.Theme;
import hu.agilexpert.core.model.User;
import hu.agilexpert.core.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRestController.class)
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    private static final UserDto ALICE_DTO = new UserDto(1L, "Alice", "alice", Theme.BRIGHT, null, null);

    // --- GET /api/users ---

    @Test
    void listUsers_returnsEmptyList() throws Exception {
        when(userService.listUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void listUsers_returnsMappedDtos() throws Exception {
        User alice = new User("Alice", "alice");
        when(userService.listUsers()).thenReturn(List.of(alice));
        when(userMapper.toDto(alice)).thenReturn(ALICE_DTO);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].name").value("Alice"));
    }

    // --- GET /api/users/{id} ---

    @Test
    void getUser_returnsDto_whenFound() throws Exception {
        User alice = new User("Alice", "alice");
        when(userService.findById(1L)).thenReturn(Optional.of(alice));
        when(userMapper.toDto(alice)).thenReturn(ALICE_DTO);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void getUser_returns404_whenNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/users ---

    @Test
    void addUser_returns201_withDto() throws Exception {
        User alice = new User("Alice", "alice");
        when(userService.addUser("Alice", "alice")).thenReturn(alice);
        when(userMapper.toDto(alice)).thenReturn(ALICE_DTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"username\":\"alice\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void addUser_returns409_onDuplicate() throws Exception {
        when(userService.addUser("Alice", "alice"))
                .thenThrow(new DuplicateIdentifierException("Username 'alice' is already taken."));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"username\":\"alice\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    // --- PATCH /api/users/{id}/name ---

    @Test
    void updateName_returns200_whenFound() throws Exception {
        User alice = new User("Alice", "alice");
        when(userService.findById(1L)).thenReturn(Optional.of(alice));

        mockMvc.perform(patch("/api/users/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alicia\"}"))
                .andExpect(status().isOk());

        verify(userService).updateUser(alice, "Alicia");
    }

    @Test
    void updateName_returns404_whenNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/users/99/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\"}"))
                .andExpect(status().isNotFound());
    }

    // --- PATCH /api/users/{id}/theme ---

    @Test
    void updateTheme_returns200_onValidTheme() throws Exception {
        User alice = new User("Alice", "alice");
        when(userService.findById(1L)).thenReturn(Optional.of(alice));

        mockMvc.perform(patch("/api/users/1/theme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"theme\":\"DARK\"}"))
                .andExpect(status().isOk());

        verify(userService).setTheme(alice, Theme.DARK);
    }

    @Test
    void updateTheme_returns400_onInvalidTheme() throws Exception {
        User alice = new User("Alice", "alice");
        when(userService.findById(1L)).thenReturn(Optional.of(alice));

        mockMvc.perform(patch("/api/users/1/theme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"theme\":\"RAINBOW\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTheme_returns404_whenNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/users/99/theme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"theme\":\"DARK\"}"))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /api/users/{id} ---

    @Test
    void deleteUser_returns204() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }
}
