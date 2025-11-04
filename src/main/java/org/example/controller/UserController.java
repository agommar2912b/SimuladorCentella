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
        crearEquipoInicialCaballerosTemplarios(user);
        crearEquipoInicialOscuridadAncestral(user);
        crearEquipoInicialPolaris(user);
        crearEquipoInicialZan(user);
        crearEquipoInicialOrfeo(user);
        crearEquipoInicialRoyalAcademy(user);
        crearEquipoInicialGigantes(user);
        crearEquipoInicialCaos(user);
        crearEquipoInicialZero(user);
        crearEquipoInicialRaimon(user);
        crearEquipoInicialEmperadoresOscuros(user);
        crearEquipoInicialChronoStorm(user);
        crearEquipoInicialFalamMedius(user);
        crearEquipoInicialCascadaPerfecta(user);
        crearEquipoInicialFlechadeArtemisa(user);



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
        playerService.createPlayer(user.getId(), team.getId(), "Fei", 84, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Byron", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Drakul", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "S.Froste", 84, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tezcarion", 84, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kappaa", 85, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Sagojou", 85, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kokappa", 85, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Vulpeen", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Mai", 85, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kappa", 85, Position.GOALKEEPER, true, true);

        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Taiga", 83, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tortus", 83, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Sierra Mix", 83, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Darehn", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Agnethe", 83, Position.DEFENDER, false, true);

        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Essau", 83, Position.MIDFIELDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Binder", 83, Position.MIDFIELDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Cardson", 83, Position.FORWARD, false, false);
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

    // Jugadores titulares
playerService.createPlayer(user.getId(), team.getId(), "Vikita", 86, Position.FORWARD, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Bailong", 85, Position.FORWARD, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Robingo", 84, Position.FORWARD, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Nosaka", 86, Position.MIDFIELDER, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Emerico", 84, Position.MIDFIELDER, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Ichihosi", 86, Position.MIDFIELDER, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Tomatin", 85, Position.DEFENDER, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Froy", 85, Position.DEFENDER, true, true);
playerService.createPlayer(user.getId(), team.getId(), "R. Del Glovo", 84, Position.DEFENDER, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Hao Li", 84, Position.DEFENDER, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Nishikage", 85, Position.GOALKEEPER, true, true);

// Suplentes
playerService.createPlayer(user.getId(), team.getId(), "Romeo", 83, Position.GOALKEEPER, false, true);
playerService.createPlayer(user.getId(), team.getId(), "Loo", 83, Position.DEFENDER, false, true);
playerService.createPlayer(user.getId(), team.getId(), "Nigel", 83, Position.MIDFIELDER, false, true);
playerService.createPlayer(user.getId(), team.getId(), "Hairy", 83, Position.MIDFIELDER, false, true);
playerService.createPlayer(user.getId(), team.getId(), "Gamma", 83, Position.FORWARD, false, true);


// Desconvocados
playerService.createPlayer(user.getId(), team.getId(), "Mike", 83, Position.FORWARD, false, false);
playerService.createPlayer(user.getId(), team.getId(), "Zabel", 83, Position.FORWARD, false, false);
playerService.createPlayer(user.getId(), team.getId(), "Zhuge", 83, Position.MIDFIELDER, false, false);
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

    // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Camelia", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Hector", 84, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Xene", 85, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Trina", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Quagmire", 85, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Ken", 84, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Quebec", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tango", 85, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Wonderbot", 85, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Jp mixi", 84, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nero", 85, Position.GOALKEEPER, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Axel Adulto", 83, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Hausen", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Jude adulto", 83, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Smiley", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Lina", 83, Position.FORWARD, false, true);

        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Celia", 83, Position.MIDFIELDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Julia", 83, Position.MIDFIELDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Rivers", 83, Position.DEFENDER, false, false);
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

    // Jugadores titulares
playerService.createPlayer(user.getId(), team.getId(), "Sol", 86, Position.FORWARD, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Aiden", 86, Position.FORWARD, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Bailong Mix", 85, Position.FORWARD, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Starla", 85, Position.MIDFIELDER, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Venus", 84, Position.MIDFIELDER, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Icer", 86, Position.MIDFIELDER, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Jove", 84, Position.MIDFIELDER, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Mercury", 85, Position.DEFENDER, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Emake", 84, Position.DEFENDER, true, true);
playerService.createPlayer(user.getId(), team.getId(), "C.Travis", 85, Position.DEFENDER, true, true);
playerService.createPlayer(user.getId(), team.getId(), "Saturn", 85, Position.GOALKEEPER, true, true);

// Suplentes
playerService.createPlayer(user.getId(), team.getId(), "Mimi Ceylon", 83, Position.GOALKEEPER, false, true);
playerService.createPlayer(user.getId(), team.getId(), "Glazier", 83, Position.DEFENDER, false, true);
playerService.createPlayer(user.getId(), team.getId(), "Sparrow", 83, Position.MIDFIELDER, false, true);
playerService.createPlayer(user.getId(), team.getId(), "Kia", 83, Position.MIDFIELDER, false, true);
playerService.createPlayer(user.getId(), team.getId(), "Aria", 83, Position.FORWARD, false, true);

// Desconvocados
playerService.createPlayer(user.getId(), team.getId(), "Spotter", 83, Position.MIDFIELDER, false, false);
playerService.createPlayer(user.getId(), team.getId(), "Mars", 83, Position.MIDFIELDER, false, false);
playerService.createPlayer(user.getId(), team.getId(), "Prendi", 83, Position.FORWARD, false, false);
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
    // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Gaiel", 84, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Paolo", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Krueger", 85, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Malcom", 84, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Noviembre M", 84, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "N Swift", 85, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Downtown", 85, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Goldie", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Feig", 85, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Raika", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Quentin", 85, Position.GOALKEEPER, true, true);
// Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "JP", 83, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Archery", 83, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Cloak", 83, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Miles", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Jax", 83, Position.FORWARD, false, true);
        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Soh ah Song", 83, Position.DEFENDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Peabody", 83, Position.GOALKEEPER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "So yeon", 83, Position.FORWARD, false, false);
}

private void crearEquipoInicialCaballerosTemplarios(UserEntity user) throws Exception {
    String initialTeamName = "Caballeros Templarios";
    String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    String teamImageDir = "images/" + user.getId();
    if (!Files.exists(Paths.get(teamImageDir))) {
        Files.createDirectories(Paths.get(teamImageDir));
    }
    String teamImageFile = safeTeamName + ".jpg";
    Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

    Path defaultImagePath = Paths.get("images/defaults/Caballeros_Templarios.jpg");
    Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

    String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
    TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

    // Jugadores titulares
    playerService.createPlayer(user.getId(), team.getId(), "Caleb", 86, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Jude", 86, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Gabi", 86, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Acker", 85, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Paul Rush", 84, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Ciara", 85, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Archer", 84, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Fei-tirano", 85, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Okita", 85, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Volet", 84, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Preston", 85, Position.GOALKEEPER, true, true);

    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Sanchez", 83, Position.GOALKEEPER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Kid-arion", 83, Position.MIDFIELDER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Chief", 83, Position.MIDFIELDER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Drent", 83, Position.DEFENDER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Bala", 83, Position.FORWARD, false, true);

    // Desconvocados
    playerService.createPlayer(user.getId(), team.getId(), "Eddy", 83, Position.MIDFIELDER, false, false);
    playerService.createPlayer(user.getId(), team.getId(), "Kraken", 83, Position.DEFENDER, false, false);
    playerService.createPlayer(user.getId(), team.getId(), "Takeribi", 83, Position.FORWARD, false, false);
}

    private void crearEquipoInicialPolaris(UserEntity user) throws Exception {
        String initialTeamName = "Polaris";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        Path defaultImagePath = Paths.get("images/defaults/Polaris.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Arthur", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "King", 86, Position.GOALKEEPER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "DrakulV", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Hyperion", 85, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Aitor", 85, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Master", 84, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Shiratori", 85, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Katya", 85, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Vissi", 84, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tabit", 84, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Seok", 84, Position.MIDFIELDER, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Vee Wai", 83, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kibitsu", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kay", 83, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Alfa", 83, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Petronio", 83, Position.FORWARD, false, true);
        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Satan", 83, Position.MIDFIELDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Turner", 83, Position.FORWARD, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Xing Zhou", 83, Position.MIDFIELDER, false, false);
    }

    private void crearEquipoInicialOscuridadAncestral(UserEntity user) throws Exception {
        String initialTeamName = "Oscuridad Ancestral";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        Path defaultImagePath = Paths.get("images/defaults/Oscuridad_Ancestral.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Tezcat", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Catora", 85, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Ruger", 84, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "B Laurel", 84, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Melvil", 85, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Yurkeh", 85, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Sor", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Hurley", 85, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Canterburi", 84, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Alice", 85, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Canthrup", 86, Position.GOALKEEPER, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Bump", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Lump", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Luceafar", 83, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Haizaki", 83, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kozomaru", 83, Position.FORWARD, false, true);

        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "La Ruze", 83, Position.MIDFIELDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Yang", 83, Position.MIDFIELDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Virginia G", 83, Position.MIDFIELDER, false, false);
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

    // Jugadores titulares
    playerService.createPlayer(user.getId(), team.getId(), "Gaiel", 84, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Paolo", 86, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Krueger", 85, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Malcom", 84, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Noviembre M", 84, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "N Swift", 85, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Downtown", 85, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Goldie", 86, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Feig", 85, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Raika", 86, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Quentin", 85, Position.GOALKEEPER, true, true);
    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Terry", 83, Position.GOALKEEPER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Styre", 83, Position.MIDFIELDER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Zetoh", 83, Position.FORWARD, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Hephestus", 83, Position.DEFENDER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Verity", 83, Position.FORWARD, false, true);
    // Desconvocados
    playerService.createPlayer(user.getId(), team.getId(), "Yukio", 83, Position.FORWARD, false, false);
    playerService.createPlayer(user.getId(), team.getId(), "Dolphin", 83, Position.MIDFIELDER, false, false);
    playerService.createPlayer(user.getId(), team.getId(), "Lightfoot", 83, Position.FORWARD, false, false);
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

    // Jugadores titulares
    playerService.createPlayer(user.getId(), team.getId(), "Simeon", 86, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Nakata", 86, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Sol Miximax", 86, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Mecamark", 85, Position.GOALKEEPER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Gandares", 85, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Sor", 85, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Caleb Redux", 85, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Cabrajal", 85, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Choi", 84, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Ozrock", 84, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Cuba Libre", 84, Position.DEFENDER, true, true);
    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Keenan", 83, Position.GOALKEEPER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Faythe Heeler", 83, Position.DEFENDER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Acuto", 83, Position.MIDFIELDER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Cypress", 83, Position.FORWARD, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Buddy", 83, Position.MIDFIELDER, false, true);
    // Desconvocados
    playerService.createPlayer(user.getId(), team.getId(), "Hermes", 83, Position.MIDFIELDER, false, false);
    playerService.createPlayer(user.getId(), team.getId(), "Drago", 83, Position.FORWARD, false, false);
    playerService.createPlayer(user.getId(), team.getId(), "Zohen", 83, Position.DEFENDER, false, false);
}

private void crearEquipoInicialRoyalAcademy(UserEntity user) throws Exception {
    String initialTeamName = "Royal Academy";
    String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    String teamImageDir = "images/" + user.getId();
    if (!Files.exists(Paths.get(teamImageDir))) {
        Files.createDirectories(Paths.get(teamImageDir));
    }
    String teamImageFile = safeTeamName + ".jpg";
    Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

    Path defaultImagePath = Paths.get("images/defaults/Royal_Academy.jpg");
    Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

    String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
    TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

    // Jugadores titulares
    playerService.createPlayer(user.getId(), team.getId(), "Aster", 85, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Bellatrix", 86, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Samford", 85, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "H Kane", 84, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "A Hobbes", 85, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Thiago", 85, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "J Wallside", 84, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Kalil", 84, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Black Flora", 85, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Zanark", 86, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Il Grande", 86, Position.GOALKEEPER, true, true);
// Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Thor", 83, Position.MIDFIELDER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Michinari", 83, Position.MIDFIELDER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Zack", 83, Position.FORWARD, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Frank", 83, Position.DEFENDER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Infinity", 83, Position.FORWARD, false, true);
// Desconvocados
    playerService.createPlayer(user.getId(), team.getId(), "Alonso", 83, Position.GOALKEEPER, false, false);
    playerService.createPlayer(user.getId(), team.getId(), "Clario", 83, Position.MIDFIELDER, false, false);
    playerService.createPlayer(user.getId(), team.getId(), "Asensi", 83, Position.MIDFIELDER, false, false);
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

    // Jugadores titulares
    playerService.createPlayer(user.getId(), team.getId(), "Gazelle", 86, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "King Redux", 84, Position.GOALKEEPER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Max", 86, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Sonny", 85, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Zippy", 85, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Handy", 85, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Lucian", 85, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Max Oscuro", 86, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Billy", 84, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Lycus", 85, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Gabi X Juana", 84, Position.DEFENDER, true, true);
    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Racerit", 83, Position.GOALKEEPER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Maximino", 83, Position.MIDFIELDER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Spring", 83, Position.MIDFIELDER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Blitzen", 83, Position.FORWARD, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Bonachea", 83, Position.FORWARD, false, true);
    // Desconvocados
    playerService.createPlayer(user.getId(), team.getId(), "Nango", 83, Position.MIDFIELDER, false, false);
    playerService.createPlayer(user.getId(), team.getId(), "Alsop", 83, Position.MIDFIELDER, false, false);
    playerService.createPlayer(user.getId(), team.getId(), "Tasuke", 83, Position.DEFENDER, false, false);
}


private void crearEquipoInicialCaos(UserEntity user) throws Exception {
    String initialTeamName = "Caos";
    String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    String teamImageDir = "images/" + user.getId();
    if (!Files.exists(Paths.get(teamImageDir))) {
        Files.createDirectories(Paths.get(teamImageDir));
    }
    String teamImageFile = safeTeamName + ".jpg";
    Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

    Path defaultImagePath = Paths.get("images/defaults/Caos.jpg");
    Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

    String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
    TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

    // Jugadores titulares
    playerService.createPlayer(user.getId(), team.getId(), "Mark", 86, Position.GOALKEEPER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Bomber", 85, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Aitriel", 86, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Nathan Oscuro", 84, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Lean", 85, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Heat", 84, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Soundtown", 85, Position. MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Krypto", 84, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Samford Redux", 85, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Wittz", 85, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Artist", 86, Position.FORWARD, true, true);

    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Beluga", 83, Position.GOALKEEPER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Droll", 83, Position.MIDFIELDER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Zell", 83, Position.FORWARD, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Gocker", 83, Position.DEFENDER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Maserati", 83, Position.DEFENDER, false, true);
    // Desconvocados
    playerService.createPlayer(user.getId(), team.getId(), "Steve O", 83, Position.MIDFIELDER, false, false);
    playerService.createPlayer(user.getId(), team.getId(), "Timmy O", 83, Position.MIDFIELDER, false, false);
    playerService.createPlayer(user.getId(), team.getId(), "Summer", 83, Position.DEFENDER, false, false);
}

    private void crearEquipoInicialEmperadoresOscuros(UserEntity user) throws Exception {
        String initialTeamName = "Emperadores Oscuros";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        Path defaultImagePath = Paths.get("images/defaults/Emperadores_Oscuros.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Perseus", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Yuri Rodina", 85, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Shadow", 85, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Syon", 84, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kirina", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Proude", 85, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Yale", 84, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Shaxel", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Lus kasim", 84, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Averin", 84, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Shisendo", 85, Position.GOALKEEPER, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Chitoh", 83, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Gachinski", 83, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Destra", 83, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Obscura", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Sael", 83, Position.FORWARD, false, true);
// Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Oleander", 83, Position.DEFENDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Lazarev", 83, Position.MIDFIELDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Thompson", 83, Position.DEFENDER, false, false);
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
    // Jugadores titulares
    playerService.createPlayer(user.getId(), team.getId(), "Xavier", 86, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Yurika", 85, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Njord", 85, Position.FORWARD, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Jordan", 85, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Riccardo", 86, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Mehr", 86, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Plink", 84, Position.MIDFIELDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Clear", 85, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Keenan", 84, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Scotty", 84, Position.DEFENDER, true, true);
    playerService.createPlayer(user.getId(), team.getId(), "Skie", 85, Position.GOALKEEPER, true, true);
    // Suplentes
    playerService.createPlayer(user.getId(), team.getId(), "Whyte", 83, Position.GOALKEEPER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Malik", 83, Position.MIDFIELDER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Nelly", 83, Position.FORWARD, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Trevor", 83, Position.DEFENDER, false, true);
    playerService.createPlayer(user.getId(), team.getId(), "Doug", 83, Position.FORWARD, false, true);

    // Desconvocados
    playerService.createPlayer(user.getId(), team.getId(), "Ribriel", 83, Position.MIDFIELDER, false, false);
    playerService.createPlayer(user.getId(), team.getId(), "Sepia", 83, Position.MIDFIELDER, false, false);
    playerService.createPlayer(user.getId(), team.getId(), "Subaru", 83, Position.DEFENDER, false, false);
}

    private void crearEquipoInicialRaimon(UserEntity user) throws Exception {
        String initialTeamName = "Raimon";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        Path defaultImagePath = Paths.get("images/defaults/Raimon.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);
        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Harper", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tsukikage", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Laymark ", 85, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Malcom oscuro ", 84, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Roma mixmax ", 85, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Hermana tezcat ", 85, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Apollo", 85, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kodaki", 84, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Hinano" ,85, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Austin ", 84, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Mark adulto", 86, Position.GOALKEEPER, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "D Lachance", 83, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Janus", 83, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Houdini", 83, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Bobby", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tassman", 83, Position.FORWARD, false, true);
        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Marvin", 83, Position.FORWARD, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Pants", 83, Position.DEFENDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Wolfy", 83, Position.MIDFIELDER, false, false);
    }

    private void crearEquipoInicialFalamMedius(UserEntity user) throws Exception {
        String initialTeamName = "Falam Medius";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        Path defaultImagePath = Paths.get("images/defaults/Falam_Medius.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);
        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Victor", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Flora", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Arculus", 85, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Malphas", 84, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Sierra", 84, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Roleia", 85, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Meteo", 85, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tyrah", 85, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Vladimir M", 85, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Beta", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Phobos", 85, Position.GOALKEEPER, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Romeo", 83, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Rondula", 83, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Despina", 83, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Belal", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Lewis", 83, Position.FORWARD, false, true);
        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Betelgeuse", 83, Position.FORWARD, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Banda", 83, Position.MIDFIELDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Viorian", 83, Position.DEFENDER, false, false);
    }

    private void crearEquipoInicialChronoStorm(UserEntity user) throws Exception {
        String initialTeamName = "Chrono Storm";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        Path defaultImagePath = Paths.get("images/defaults/Chrono_Storm.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);
        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Vladimir", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nelly", 85, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Shawn", 86, Position.FORWARD, true, true);

        playerService.createPlayer(user.getId(), team.getId(), "Kakehashi", 85, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tori", 85, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Noviembre", 84, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Mercury", 84, Position.MIDFIELDER, true, true);

        playerService.createPlayer(user.getId(), team.getId(), "Juana", 84, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Reina", 85, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Goldie", 86, Position.DEFENDER, true, true);

        playerService.createPlayer(user.getId(), team.getId(), "Sandra", 85, Position.GOALKEEPER, true, true);

// Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Cerise", 83, Position.MIDFIELDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Mason", 83, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Readingplace", 83, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Manta", 83, Position.MIDFIELDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Jazmine", 83, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Erik", 83, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Diana", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Glacia", 83, Position.FORWARD, false, false);
    }

    private void crearEquipoInicialFlechadeArtemisa(UserEntity user) throws Exception {
        String initialTeamName = "Flecha de Artemisa";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        Path defaultImagePath = Paths.get("images/defaults/Flecha_de_Artemisa.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);
        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "B Love", 84, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Samguk", 86, Position.GOALKEEPER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nathan", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Cedric", 85, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Jack Wallside", 85, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Mountain", 84, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Falco", 85, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Gamma", 85, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Arth", 85, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Ghiris", 83, Position.MIDFIELDER, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Canon", 83, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Darren", 83, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Zephyr Vittese", 84, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Van Yamano", 83, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Leung", 83, Position.MIDFIELDER, false, true);
        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Arkew", 83, Position.DEFENDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Wanli", 83, Position.DEFENDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Steve", 83, Position.MIDFIELDER, false, false);
    }

    private void crearEquipoInicialCascadaPerfecta(UserEntity user) throws Exception {
        String initialTeamName = "Cascada Perfecta";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        Path defaultImagePath = Paths.get("images/defaults/Cascada_Perfecta.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);
        playerService.createPlayer(user.getId(), team.getId(), "Lancer", 85, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Metron", 85, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Axel", 85, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Ar", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Riccardo Miximax", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Cao Cao", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Woo", 85, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Artemis", 84, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Gwennady", 84, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Bael", 84, Position.DEFENDER, true, true);


        playerService.createPlayer(user.getId(), team.getId(), "Falcon", 85, Position.GOALKEEPER, true, true);
// Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Eka", 83, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Foxtrot", 83, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Jones", 83, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Partinus", 83, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Owl", 83, Position.MIDFIELDER, false, true);

        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Sirius", 83, Position.DEFENDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Xenon", 83, Position.DEFENDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Plumber", 83, Position.FORWARD, false, false);
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