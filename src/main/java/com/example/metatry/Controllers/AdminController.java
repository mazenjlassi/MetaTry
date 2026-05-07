package com.example.metatry.Controllers;

import com.example.metatry.Enums.Role;
import com.example.metatry.Models.User;
import com.example.metatry.Services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return adminService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable Long id) {
        return adminService.getUserById(id);
    }

    @PostMapping("/users")
    public User createUser(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String password = request.get("password");
        String roleStr = request.getOrDefault("role", "MARKETING");

        Role role = Role.valueOf(roleStr.toUpperCase());

        return adminService.createUser(name, email, password, role);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @PutMapping("/users/{id}/role")
    public User updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {
        String roleStr = request.get("role");
        Role role = Role.valueOf(roleStr.toUpperCase());
        return adminService.updateUserRole(id, role);
    }
}