package org.example.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.user.*;
import org.example.entity.UserEntity;
import org.example.exceptions.InvalidCredentialsException;
import org.example.exceptions.UserNotFoundNameException;
import org.example.service.PlayerService;
import org.example.service.TeamService;
import org.example.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.StandardCopyOption;
import org.example.entity.TeamEntity;
import org.example.Position;

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
    private final TeamService teamService;      
    private final PlayerService playerService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody UserCreate loginRequest , HttpServletRequest request, HttpServletResponse response) {
        UserEntity user = userService.validateUserCredentials(loginRequest.getName(), loginRequest.getPassword());
        if (user == null) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.getName(), loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);

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

        UserEntity updatedUser = userService.changeName(request);

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
                    userService.patchUser(updatedUser.getId(), updatedUser.getName(), null, newProfilePictureUrl);
                    updatedUser.setProfilePictureUrl(newProfilePictureUrl);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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

        // --- DATOS INICIALES AUTOMÁTICOS ---
        crearEquipoInicialCriaturasDeLaNoche(user);
        crearEquipoInicialDragonLink(user);
        crearEquipoInicialInazumaJapon(user);
        crearEquipoInicialUniversal(user);
        crearEquipoInicialUnicorn(user);
        crearEquipoInicialRoyalRedux(user);
        crearEquipoInicialZan(user);
        crearEquipoInicialOrfeo(user);
        crearEquipoInicialMaryTimes(user);
        crearEquipoInicialGigantes(user);
        crearEquipoInicialDragonesDeFuego(user);
        crearEquipoInicialZero(user);



        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }

    private void crearEquipoInicialCriaturasDeLaNoche(UserEntity user) throws Exception {
        String initialTeamName = "Criaturas de la Noche";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        // Copiar imagen por defecto
        Path defaultImagePath = Paths.get("images/defaults/Criaturas_de_la_Noche.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;

        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Aiden", 92, Position.FORWARD, true);
        playerService.createPlayer(user.getId(), team.getId(), "Laurel", 87, Position.MIDFIELDER, true);
        playerService.createPlayer(user.getId(), team.getId(), "Ar ecks", 87, Position.MIDFIELDER, true);
        playerService.createPlayer(user.getId(), team.getId(), "Shawn", 89, Position.DEFENDER, true);
        playerService.createPlayer(user.getId(), team.getId(), "Byron", 92, Position.MIDFIELDER, true);
        playerService.createPlayer(user.getId(), team.getId(), "Samford R", 86, Position.MIDFIELDER, true);
        playerService.createPlayer(user.getId(), team.getId(), "Erik", 88, Position.MIDFIELDER, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kalil", 85, Position.DEFENDER, true);
        playerService.createPlayer(user.getId(), team.getId(), "Hurley", 89, Position.DEFENDER, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nathan 0", 87, Position.DEFENDER, true);
        playerService.createPlayer(user.getId(), team.getId(), "Phobos", 87, Position.GOALKEEPER, true);

        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Peabody", 85, Position.GOALKEEPER, false);
        playerService.createPlayer(user.getId(), team.getId(), "Kappa", 84, Position.MIDFIELDER, false);
        playerService.createPlayer(user.getId(), team.getId(), "Garcia", 84, Position.DEFENDER, false);
        playerService.createPlayer(user.getId(), team.getId(), "Perseus", 82, Position.FORWARD, false);
        playerService.createPlayer(user.getId(), team.getId(), "Leung", 83, Position.MIDFIELDER, false);
    }

    private void crearEquipoInicialDragonLink(UserEntity user) throws Exception {
    String initialTeamName = "Dragon Link";
    String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    String teamImageDir = "images/" + user.getId();
    if (!Files.exists(Paths.get(teamImageDir))) {
        Files.createDirectories(Paths.get(teamImageDir));
    }
    String teamImageFile = safeTeamName + ".jpg";
    Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

    // Copiar imagen por defecto
    Path defaultImagePath = Paths.get("images/defaults/Dragon_Link.jpg");
    Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

    String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
    TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

    // Titulares
    playerService.createPlayer(user.getId(), team.getId(), "Bai Long", 89, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Mitsuru", 88, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Tezcat", 89, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Nosaka", 89, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Yurika", 88, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Darren", 86, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Sael", 87, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Hermana", 88, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Destra", 87, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Froy", 88, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Nishikage", 88, Position.GOALKEEPER, true);

    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Keenan", 84, Position.GOALKEEPER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Mike", 84, Position.FORWARD, false);
    playerService.createPlayer(user.getId(), team.getId(), "Tomatin", 85, Position.DEFENDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Nae", 86, Position.FORWARD, false);
    playerService.createPlayer(user.getId(), team.getId(), "Kozomaru", 85, Position.FORWARD, false);
}


private void crearEquipoInicialInazumaJapon(UserEntity user) throws Exception {
    String initialTeamName = "Inazuma Japon";
    String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    String teamImageDir = "images/" + user.getId();
    if (!Files.exists(Paths.get(teamImageDir))) {
        Files.createDirectories(Paths.get(teamImageDir));
    }
    String teamImageFile = safeTeamName + ".jpg";
    Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

    Path defaultImagePath = Paths.get("images/defaults/Inazuma_Japon.jpg");
    Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

    String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
    TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

    // Titulares
    playerService.createPlayer(user.getId(), team.getId(), "Hector", 88, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Robingo", 88, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Alpha", 87, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Harper", 88, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Xene", 87, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Nakata", 90, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Quagmire", 86, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Trina", 87, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Quebec", 85, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Camelia", 86, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Nero", 86, Position.GOALKEEPER, true);

    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "David", 87, Position.GOALKEEPER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Bomber", 83, Position.DEFENDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Leonardo", 83, Position.MIDFIELDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Janus", 85, Position.MIDFIELDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Tsukikage", 85, Position.FORWARD, false);
}
private void crearEquipoInicialUniversal(UserEntity user) throws Exception {
    String initialTeamName = "Universal";
    String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    String teamImageDir = "images/" + user.getId();
    if (!Files.exists(Paths.get(teamImageDir))) {
        Files.createDirectories(Paths.get(teamImageDir));
    }
    String teamImageFile = safeTeamName + ".jpg";
    Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

    Path defaultImagePath = Paths.get("images/defaults/Universal.jpg");
    Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

    String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
    TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

    // Titulares
    playerService.createPlayer(user.getId(), team.getId(), "Victor", 89, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Hiroto", 87, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Ruger", 88, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Falco", 87, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Drakul", 89, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Sol", 87, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Ghiris", 87, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Houdini", 86, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Jack", 88, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Icer", 86, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Jp", 87, Position.GOALKEEPER, true);

    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Saturn", 82, Position.GOALKEEPER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Malcolm", 84, Position.DEFENDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Kia", 84, Position.MIDFIELDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Arthur", 84, Position.FORWARD, false);
    playerService.createPlayer(user.getId(), team.getId(), "Dakkar", 84, Position.MIDFIELDER, false);
}

private void crearEquipoInicialUnicorn(UserEntity user) throws Exception {
    String initialTeamName = "Unicorn";
    String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    String teamImageDir = "images/" + user.getId();
    if (!Files.exists(Paths.get(teamImageDir))) {
        Files.createDirectories(Paths.get(teamImageDir));
    }
    String teamImageFile = safeTeamName + ".jpg";
    Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

    Path defaultImagePath = Paths.get("images/defaults/Unicorn.jpg");
    Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

    String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
    TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

    // Titulares
    playerService.createPlayer(user.getId(), team.getId(), "Beta", 88, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Edgar", 87, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Turner", 86, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Rondula", 86, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Paolo", 89, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Krueger", 88, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Scotty", 86, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Tori", 87, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Nathan", 89, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Goldie", 89, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Quentin", 89, Position.GOALKEEPER, true);

    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Gigi", 86, Position.GOALKEEPER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Tasuke", 83, Position.DEFENDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Garreu", 83, Position.MIDFIELDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Zephyr", 85, Position.DEFENDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Zack", 85, Position.FORWARD, false);
}

private void crearEquipoInicialRoyalRedux(UserEntity user) throws Exception {
    String initialTeamName = "Royal Academy Redux";
    String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    String teamImageDir = "images/" + user.getId();
    if (!Files.exists(Paths.get(teamImageDir))) {
        Files.createDirectories(Paths.get(teamImageDir));
    }
    String teamImageFile = safeTeamName + ".jpg";
    Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

    Path defaultImagePath = Paths.get("images/defaults/Royal_Academy_Redux.jpg");
    Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

    String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
    TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

    // Titulares
    playerService.createPlayer(user.getId(), team.getId(), "Vulpeen", 89, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Haizaki", 87, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Vladimir", 87, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Austin", 88, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Petronio", 86, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Caleb", 91, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Jude", 89, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Acker", 85, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Gabi", 89, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Bobby", 85, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "King", 86, Position.GOALKEEPER, true);

    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Salvador", 86, Position.GOALKEEPER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Billy", 83, Position.DEFENDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Kirina", 85, Position.MIDFIELDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Acuto", 85, Position.MIDFIELDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Lucian", 82, Position.FORWARD, false);
}

private void crearEquipoInicialZan(UserEntity user) throws Exception {
    String initialTeamName = "Zan";
    String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    String teamImageDir = "images/" + user.getId();
    if (!Files.exists(Paths.get(teamImageDir))) {
        Files.createDirectories(Paths.get(teamImageDir));
    }
    String teamImageFile = safeTeamName + ".jpg";
    Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

    Path defaultImagePath = Paths.get("images/defaults/Zan.jpg");
    Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

    String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
    TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

    // Titulares
    playerService.createPlayer(user.getId(), team.getId(), "Torch", 88, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Arion", 89, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Davy", 85, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Fei", 87, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Buddy", 84, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Xing Zhou", 83, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Quintet", 86, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Iggie", 83, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Ozrock", 88, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Mitya", 80, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Terry", 88, Position.GOALKEEPER, true);

    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Astaroth", 85, Position.GOALKEEPER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Yang", 83, Position.MIDFIELDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Syon", 83, Position.FORWARD, false);
    playerService.createPlayer(user.getId(), team.getId(), "Plink", 84, Position.MIDFIELDER, false);
}

private void crearEquipoInicialOrfeo(UserEntity user) throws Exception {
    String initialTeamName = "Orfeo";
    String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    String teamImageDir = "images/" + user.getId();
    if (!Files.exists(Paths.get(teamImageDir))) {
        Files.createDirectories(Paths.get(teamImageDir));
    }
    String teamImageFile = safeTeamName + ".jpg";
    Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

    Path defaultImagePath = Paths.get("images/defaults/Orfeo.jpg");
    Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

    String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
    TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

    // Titulares
    playerService.createPlayer(user.getId(), team.getId(), "Simeon", 91, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Lancer", 87, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Gamma", 87, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Gandares", 88, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Cronus", 87, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Caleb Redux", 87, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Choi", 85, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Yale", 86, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Sor", 87, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Aster", 85, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "MECAMARK", 88, Position.GOALKEEPER, true);

    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Luceafar", 86, Position.GOALKEEPER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Zohen", 82, Position.DEFENDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Adé", 85, Position.MIDFIELDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Maximiano", 84, Position.MIDFIELDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Lump", 82, Position.DEFENDER, false);
}

private void crearEquipoInicialMaryTimes(UserEntity user) throws Exception {
    String initialTeamName = "Mary Times";
    String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    String teamImageDir = "images/" + user.getId();
    if (!Files.exists(Paths.get(teamImageDir))) {
        Files.createDirectories(Paths.get(teamImageDir));
    }
    String teamImageFile = safeTeamName + ".jpg";
    Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

    Path defaultImagePath = Paths.get("images/defaults/Mary_Times.jpg");
    Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

    String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
    TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

    // Titulares
    playerService.createPlayer(user.getId(), team.getId(), "Dylan", 86, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Zanark", 88, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Flora", 89, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Clario", 89, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Bellatrix", 87, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Samford", 87, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Sonny", 87, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Thor", 86, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Thiago", 89, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Malcom", 85, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Il grande", 88, Position.GOALKEEPER, true);

    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Dvalin", 85, Position.GOALKEEPER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Mountain", 84, Position.GOALKEEPER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Steve", 83, Position.MIDFIELDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Kevin", 85, Position.FORWARD, false);
    playerService.createPlayer(user.getId(), team.getId(), "Tatsumi", 84, Position.MIDFIELDER, false);
}

private void crearEquipoInicialGigantes(UserEntity user) throws Exception {
    String initialTeamName = "Gigantes";
    String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    String teamImageDir = "images/" + user.getId();
    if (!Files.exists(Paths.get(teamImageDir))) {
        Files.createDirectories(Paths.get(teamImageDir));
    }
    String teamImageFile = safeTeamName + ".jpg";
    Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

    Path defaultImagePath = Paths.get("images/defaults/Gigantes.jpg");
    Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

    String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
    TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

    // Titulares
    playerService.createPlayer(user.getId(), team.getId(), "Gazelle", 88, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Max", 86, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Skipper", 83, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Max oscuro", 86, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Tom Dark", 88, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Subaru", 83, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Arculus", 87, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Master", 84, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Tod", 85, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Shadow oscuro", 83, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "King Redux", 88, Position.GOALKEEPER, true);

    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Alvicci", 80, Position.GOALKEEPER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Galliano", 82, Position.DEFENDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Steve", 84, Position.MIDFIELDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Gabrini", 82, Position.MIDFIELDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Michael", 82, Position.FORWARD, false);
}

private void crearEquipoInicialDragonesDeFuego(UserEntity user) throws Exception {
    String initialTeamName = "Dragones de Fuego";
    String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    String teamImageDir = "images/" + user.getId();
    if (!Files.exists(Paths.get(teamImageDir))) {
        Files.createDirectories(Paths.get(teamImageDir));
    }
    String teamImageFile = safeTeamName + ".jpg";
    Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

    Path defaultImagePath = Paths.get("images/defaults/Dragones_de_fuego.jpg");
    Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

    String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
    TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

    // Titulares
    playerService.createPlayer(user.getId(), team.getId(), "Dvalin", 86, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Bala Gasgula", 86, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Soundtown", 86, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Hao Li", 87, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Roma", 87, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Malik", 85, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Banda", 86, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Zippy Lerner", 86, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Archer", 87, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Keenan Sharpe", 86, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Mark", 90, Position.GOALKEEPER, true);

    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Preston", 80, Position.GOALKEEPER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Kiburn", 83, Position.DEFENDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Hairy", 84, Position.MIDFIELDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Lus Kasim", 84, Position.DEFENDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Canon Evans", 85, Position.FORWARD, false);
}

private void crearEquipoInicialZero(UserEntity user) throws Exception {
    String initialTeamName = "Zero";
    String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    String teamImageDir = "images/" + user.getId();
    if (!Files.exists(Paths.get(teamImageDir))) {
        Files.createDirectories(Paths.get(teamImageDir));
    }
    String teamImageFile = safeTeamName + ".jpg";
    Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

    Path defaultImagePath = Paths.get("images/defaults/Zero.jpg");
    Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

    String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
    TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

    // Titulares
    playerService.createPlayer(user.getId(), team.getId(), "Axel", 89, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Njord", 87, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Xavier", 89, Position.FORWARD, true);
    playerService.createPlayer(user.getId(), team.getId(), "Aitor", 86, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Mehr", 87, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Jordan", 86, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Riccardo", 89, Position.MIDFIELDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Frank", 85, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Clear", 84, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Ogar", 84, Position.DEFENDER, true);
    playerService.createPlayer(user.getId(), team.getId(), "Skie Blue", 84, Position.GOALKEEPER, true);

    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Samguk", 83, Position.GOALKEEPER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Cerise", 84, Position.MIDFIELDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Gozu", 83, Position.DEFENDER, false);
    playerService.createPlayer(user.getId(), team.getId(), "Yuri Rodina", 83, Position.FORWARD, false);
    playerService.createPlayer(user.getId(), team.getId(), "Dolphin", 83, Position.MIDFIELDER, false);
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().invalidate();

        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); 
        response.addCookie(cookie);

        return ResponseEntity.ok().body("Sesión cerrada y cookie eliminada");
    }
}