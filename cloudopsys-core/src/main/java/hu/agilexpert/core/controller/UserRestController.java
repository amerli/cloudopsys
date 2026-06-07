package hu.agilexpert.core.controller;

import hu.agilexpert.core.dto.UserDto;
import hu.agilexpert.core.exception.DuplicateIdentifierException;
import hu.agilexpert.core.mapper.UserMapper;
import hu.agilexpert.core.model.Theme;
import hu.agilexpert.core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public List<UserDto> listUsers() {
        return StreamSupport.stream(userService.listUsers().spliterator(), false)
                .map(userMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(userMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> addUser(@RequestBody Map<String, String> body) {
        try {
            UserDto dto = userMapper.toDto(userService.addUser(body.get("name"), body.get("username")));
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (DuplicateIdentifierException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<?> updateName(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return userService.findById(id).map(user -> {
            userService.updateUser(user, body.get("name"));
            return ResponseEntity.ok().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/theme")
    public ResponseEntity<?> updateTheme(@PathVariable Long id, @RequestBody Map<String, String> body) {
        if (userService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        var user = userService.findById(id).get();
        try {
            userService.setTheme(user, Theme.valueOf(body.get("theme")));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid theme: " + body.get("theme")));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
