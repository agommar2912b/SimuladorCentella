package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.UserEntity;
import org.example.exceptions.UserNotFoundException;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserEntity patchUser(Long id, String name, String profilePictureUrl) {
        UserEntity User = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if (name != null && !name.trim().isEmpty()) {
            User.setName(name);
        }
        if (!profilePictureUrl.trim().isEmpty()) {
            User.setProfilePictureUrl(profilePictureUrl);
        }
        return userRepository.save(User);
    }

    public UserEntity deleteUser(Long id) {
        UserEntity User = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(User);
        return User;
    }

    public List<UserEntity> getByName(String name) {
        return userRepository.findByName(name);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public UserEntity createUser(String name, String profilePictureUrl) {
        UserEntity User = new UserEntity(name, profilePictureUrl);
        return userRepository.save(User);
    }
}

