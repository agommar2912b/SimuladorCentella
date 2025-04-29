package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.team.TeamPatch;
import org.example.dto.user.*;
import org.example.entity.UserEntity;
import org.example.exceptions.InvalidCredentialsException;
import org.example.exceptions.UserNotFoundNameException;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        UserEntity user =  userService.changeName(request);
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    @PatchMapping("/{id}")
    public UserResponse patchUser(@PathVariable Long id, @Valid @RequestBody UserPatch userPatch){
        UserEntity patchedTeam = userService.patchUser(id, userPatch.getName(), userPatch.getPassword());

        return UserResponse.builder()
                .id(patchedTeam.getId())
                .name(patchedTeam.getName())
                .password(patchedTeam.getPassword())
                .build();

    }
    @DeleteMapping("/{id}")
    public UserResponse deleteUser(@PathVariable Long id){
        UserEntity patchedTeam = userService.deleteUser(id);

        return UserResponse.builder()
                .id(patchedTeam.getId())
                .name(patchedTeam.getName())
                .password(patchedTeam.getPassword())
                .build();

    }
    @PostMapping
    public UserResponse createUser(@Valid @RequestBody UserCreate userCreate){
        UserEntity patchedUser = userService.createUser(userCreate.getName(),userCreate.getPassword());

        return UserResponse.builder()
                .id(patchedUser.getId())
                .name(patchedUser.getName())
                .password(patchedUser.getPassword())
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
                        .build())
                .toList();
    }
}