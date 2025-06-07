package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.user.UserChangeName;
import org.example.dto.user.UserChangePassword;
import org.example.entity.TeamEntity;
import org.example.entity.UserEntity;
import org.example.exceptions.InvalidCredentialsException;
import org.example.exceptions.TeamNotFoundException;
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

    public UserEntity changeName(UserChangeName request) {
        UserEntity userExist = getByName(request.getNewUsername());
        UserEntity user = getByName(request.getOldUsername());
        if (userExist != null || Objects.equals(request.getNewUsername(), request.getOldUsername())) {
            throw new InvalidCredentialsException("Ese nombre ya existe o es el mismo que ya tenias");
        }else{
            user.setName(request.getNewUsername());
            userRepository.save(user);
            return user;
        }

    }

    public UserEntity changePassword(UserChangePassword request) {
        UserEntity user = getByName(request.getUsername());

        if (user == null) {
            throw new InvalidCredentialsException("Usuario no encontrado");
        }

        // Validar respuesta de seguridad
        if (!user.getSecurityAnswer().equalsIgnoreCase(request.getSecurityAnswer().trim())) {
            throw new InvalidCredentialsException("Respuesta de seguridad incorrecta");
        }

        user.setPassword(request.getNew_password());
        userRepository.save(user);
        return user;
    }

    public UserEntity validateUserCredentials(String name, String password) {
        UserEntity user = getByName(name);
        if (user == null||!user.getPassword().equals(password)) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return user;
    }



    public UserEntity patchUser(Long id, String name, String password , String profilePictureUrl)  {
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
        if (profilePictureUrl != null && !profilePictureUrl.trim().isEmpty()) {
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

    public UserEntity getByName(String name) {
        return userRepository.findByName(name);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public UserEntity createUser(String name, String password, String profilePictureUrl, String securityQuestion, String securityAnswer) {
        UserEntity userWithName = getByName(name);
        if (userWithName == null) {
            UserEntity user = new UserEntity(name, password);
            user.setProfilePictureUrl(profilePictureUrl);
            user.setSecurityQuestion(securityQuestion);
            user.setSecurityAnswer(securityAnswer);
            return userRepository.save(user);
        } else {
            throw new UserNameExistException(name);
        }
    }

    public UserEntity getById(Long userId) {
    return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
}
}

