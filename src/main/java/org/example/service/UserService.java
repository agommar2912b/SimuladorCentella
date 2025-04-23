package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.user.UserChangePassword;
import org.example.entity.UserEntity;
import org.example.exceptions.InvalidCredentialsException;
import org.example.exceptions.UserNameExistException;
import org.example.exceptions.UserNotFoundException;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserEntity changePassword(UserChangePassword request) {
        UserEntity user = userRepository.findByName(request.getUsername());

        if (user == null || !user.getPassword().equals(request.getOld_password())) {
            throw new InvalidCredentialsException("Usuario o contraseña incorrectos");
        }

        user.setPassword(request.getNew_password());
        userRepository.save(user);
        return user;
    }

    public UserEntity validateUserCredentials(String name, String password) {
        UserEntity user = userRepository.findByName(name);
        if (user == null||!user.getPassword().equals(password)) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return user;
    }



    public UserEntity patchUser(Long id, String name, String password) {
        UserEntity User = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        UserEntity userWithName = getByName(name);

        if (userWithName!=null && !Objects.equals(userWithName.getId(), id)) {
            throw new UserNameExistException(name);
        }

        if (name != null && !name.trim().isEmpty()) {
            User.setName(name);
        }
        if (password!=null  && !password.trim().isEmpty()) {
            User.setPassword(password);
        }
        return userRepository.save(User);
    }

    public UserEntity deleteUser(Long id) {
        UserEntity User = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(User);
        return User;
    }

    public UserEntity getByName(String name) {
        return userRepository.findByName(name);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public UserEntity createUser(String name, String password) {
        UserEntity userWithName = getByName(name);
        if (userWithName==null) {
            UserEntity user = new UserEntity(name,password);
            return userRepository.save(user);
        } else {
            throw new UserNameExistException(name);
        }
    }
}

