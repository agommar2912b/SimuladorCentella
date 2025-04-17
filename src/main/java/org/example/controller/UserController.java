package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.team.TeamPatch;
import org.example.dto.user.UserCreate;
import org.example.dto.user.UserResponse;
import org.example.entity.UserEntity;
import org.example.exceptions.UserNotFoundNameException;
import org.example.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PatchMapping("/{id}")
    public UserResponse patchUser(@PathVariable Long id, @Valid @RequestBody TeamPatch team){
        UserEntity patchedTeam = userService.patchUser(id, team.getName(), team.getProfilePictureUrl());

        return UserResponse.builder()
                .id(patchedTeam.getId())
                .name(patchedTeam.getName())
                .profilePictureUrl(patchedTeam.getProfilePictureUrl())
                .build();

    }
    @DeleteMapping("/{id}")
    public UserResponse deleteUser(@PathVariable Long id){
        UserEntity patchedTeam = userService.deleteUser(id);

        return UserResponse.builder()
                .id(patchedTeam.getId())
                .name(patchedTeam.getName())
                .profilePictureUrl(patchedTeam.getProfilePictureUrl())
                .build();

    }
    @PostMapping
    public UserResponse createUser(@Valid @RequestBody UserCreate userCreate){
        UserEntity patchedUser = userService.createUser(userCreate.getName(),userCreate.getProfilePictureUrl());

        return UserResponse.builder()
                .id(patchedUser.getId())
                .name(patchedUser.getName())
                .profilePictureUrl(patchedUser.getProfilePictureUrl())
                .build();

    }

    @GetMapping
    public List<UserResponse> getUsers(@RequestParam(required = false)String name) {
        List<UserEntity> users;

        if (name != null && !name.isBlank()) {
            users = userService.getByName(name);
            if (users.isEmpty()) {
                throw new UserNotFoundNameException(name);
            }
        } else {
            users = userService.getAllUsers();
        }
        return users.stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .profilePictureUrl(user.getProfilePictureUrl())
                        .build())
                .toList();
    }
}