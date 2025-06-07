package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.team.TeamPatch;
import org.example.dto.user.*;
import org.example.entity.UserEntity;
import org.example.exceptions.InvalidCredentialsException;
import org.example.exceptions.UserNotFoundNameException;
import org.example.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody UserCreate loginRequest) {
        UserEntity user = userService.validateUserCredentials(loginRequest.getName(), loginRequest.getPassword());
        if (user == null) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    @PutMapping("/changePassword")
    public UserResponse changePassword(@RequestBody UserChangePassword request) {
        UserEntity user =  userService.changePassword(request);
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    @PutMapping("/changeName")
    public UserResponse changeName(@RequestBody UserChangeName request) {
        UserEntity user = userService.getByName(request.getOldUsername());
        if (user == null) {
            throw new InvalidCredentialsException("Usuario no encontrado");
        }

        String oldName = user.getName();
        String newName = request.getNewUsername();

        // Renombrar la imagen si existe
        String oldProfilePictureUrl = user.getProfilePictureUrl();
        String newProfilePictureUrl = null;
        if (oldProfilePictureUrl != null && !oldProfilePictureUrl.isBlank()) {
            try {
                String oldFileName = oldName.replaceAll("[^a-zA-Z0-9\\-_]", "_") + ".jpg";
                String newFileName = newName.replaceAll("[^a-zA-Z0-9\\-_]", "_") + ".jpg";
                Path imagesDir = Paths.get("images", "users");
                Path oldImagePath = imagesDir.resolve(oldFileName);
                Path newImagePath = imagesDir.resolve(newFileName);

                if (Files.exists(oldImagePath)) {
                    Files.move(oldImagePath, newImagePath);
                    newProfilePictureUrl = "/images/users/" + newFileName;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Cambiar el nombre y la url de la imagen en la base de datos
        UserEntity updatedUser = userService.changeName(request);
        if (newProfilePictureUrl != null) {
            updatedUser.setProfilePictureUrl(newProfilePictureUrl);
            userService.patchUser(updatedUser.getId(), updatedUser.getName(), null, newProfilePictureUrl);
        }

        return UserResponse.builder()
                .id(updatedUser.getId())
                .name(updatedUser.getName())
                .profilePictureUrl(updatedUser.getProfilePictureUrl())
                .build();
    }
    @DeleteMapping("/{id}")
    public UserResponse deleteUser(@PathVariable Long id){
        UserEntity user = userService.deleteUser(id);

        // Eliminar la imagen asociada si existe
        String profilePictureUrl = user.getProfilePictureUrl();
        if (profilePictureUrl != null && !profilePictureUrl.isBlank()) {
            try {
                String relativePath = profilePictureUrl.startsWith("/") ? profilePictureUrl.substring(1) : profilePictureUrl;
                Path imagePath = Paths.get(relativePath);
                if (Files.exists(imagePath)) {
                    Files.delete(imagePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }
    @PostMapping(consumes = {"multipart/form-data"})
    public UserResponse createUser(
            @RequestParam("name") String name,
            @RequestParam("password") String password,
            @RequestParam("image") MultipartFile image,
            @RequestParam("securityQuestion") String securityQuestion,
            @RequestParam("securityAnswer") String securityAnswer) throws Exception { 

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("La imagen es obligatoria");
        }

        String imagesDir = "images/users";
        if (!Files.exists(Paths.get(imagesDir))) {
            Files.createDirectories(Paths.get(imagesDir));
        }
        String safeName = name.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String profilePictureUrl = null;
        String fileName = safeName + ".jpg";
        Path imagePath = Paths.get(imagesDir, fileName);
        Files.write(imagePath, image.getBytes());
        profilePictureUrl = "/images/users/" + fileName;

        UserEntity user = userService.createUser(name, password, profilePictureUrl, securityQuestion, securityAnswer);

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }


    @GetMapping
    public List<UserResponse> getUsers(@RequestParam(required = false)String name) {
        List<UserEntity> users;
        UserEntity user;

        if (name != null && !name.isBlank()) {
            user = userService.getByName(name);
            if (user==null) {
                throw new UserNotFoundNameException(name);
            }
            users = new ArrayList<>();
            users.add(user);

        } else {
            users = userService.getAllUsers();
        }
        return users.stream()
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .password(u.getPassword())
                        .profilePictureUrl(u.getProfilePictureUrl())
                        .build())
                .toList();
    }

    @GetMapping("/images/{imageName:.+}")
    public ResponseEntity<Resource> getUserImage(@PathVariable String imageName) throws Exception {
        Path imagePath = Paths.get("images", "users", imageName);
        Resource resource = new UrlResource(imagePath.toUri());
        if (resource.exists() && resource.isReadable()) {
            return ResponseEntity.ok().body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/security-question")
    public ResponseEntity<?> getSecurityQuestion(@RequestParam String username) {
        UserEntity user = userService.getByName(username);
        if (user == null) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }
        return ResponseEntity.ok().body(user.getSecurityQuestion());
    }
}