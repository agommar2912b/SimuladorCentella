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
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
            .getContextHolderStrategy();
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody UserCreate loginRequest, HttpServletRequest request,
            HttpServletResponse response) {
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
        UserEntity user = userService.changePassword(request);
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
    public UserResponse deleteUser(@PathVariable Long id) {
        UserEntity user = userService.deleteUser(id);

        // Eliminar la imagen asociada si existe
        String profilePictureUrl = user.getProfilePictureUrl();
        if (profilePictureUrl != null && !profilePictureUrl.isBlank()) {
            try {
                String relativePath = profilePictureUrl.startsWith("/") ? profilePictureUrl.substring(1)
                        : profilePictureUrl;
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

    @PostMapping(consumes = { "multipart/form-data" })
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
        crearEquipoInicialAlpino(user);
        crearEquipoInicialUnicorn(user);
        crearEquipoInicialCaballerosTemplarios(user);
        crearEquipoInicialDesesperdidos(user);
        crearEquipoInicialPolaris(user);
        crearEquipoInicialBigBang(user);
        crearEquipoInicialZeus(user);
        crearEquipoInicialRoyalAcademy(user);
        crearEquipoInicialGigantes(user);
        crearEquipoInicialCaos(user);
        crearEquipoInicialZero(user);
        crearEquipoInicialRaimon(user);
        crearEquipoInicialEmperadoresOscuros(user);
        crearEquipoInicialChronoStorm(user);
        crearEquipoInicialFalamMedius(user);
        crearEquipoInicialCascadaPerfecta(user);
        crearEquipoInicialCalaPirata(user);
        crearEquipoInicialPlenilunio(user);
        crearEquipoInicialBarcelonaOrb(user);
        crearEquipoInicialTerracota(user);
        crearEquipoInicialRobots(user);

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
        playerService.createPlayer(user.getId(), team.getId(), "Hiroto", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Byron", 89, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Drakul", 89, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Hortensia", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kappa", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kappaa", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Sagojou", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kokappa", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Vulpeen", 89, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Hermana", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Terry", 88, Position.GOALKEEPER, true, true);

        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Lern", 83, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Fei", 85, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Mera", 87, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Ambrossia", 87, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Uzume", 87, Position.MIDFIELDER, false, true);
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
        playerService.createPlayer(user.getId(), team.getId(), "Vikita", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Bailong", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tezcarion", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nosaka", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Emerico", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Ichihosi", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tomatin", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Froy", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "R. Del Glovo", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Haizaki", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nishikage", 87, Position.GOALKEEPER, true, true);

        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Romeo", 84, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Loo", 84, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nigel", 84, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Hairy", 84, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Hao Li", 85, Position.DEFENDER, false, true);
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
        playerService.createPlayer(user.getId(), team.getId(), "camelia", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "hector", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Helio", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Trina", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Xene", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Ken", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Quebec", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tango", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "wonderbot", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Jp mixi", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nero", 87, Position.GOALKEEPER, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "axel Adulto", 83, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Hausen", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Celia", 83, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Smiley", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Quagmire", 85, Position.FORWARD, false, true);

        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Jude adulto", 83, Position.MIDFIELDER, false, false);
    }

    private void crearEquipoInicialAlpino(UserEntity user) throws Exception {
        String initialTeamName = "Alpino";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        Path defaultImagePath = Paths.get("images/defaults/Alpino.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Saturn", 88, Position.GOALKEEPER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Downtown", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Emake", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Icer", 89, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Shawn", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Jove", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Anastasia", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kia", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Sol", 89, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Bai Long Mixi", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Aiden", 88, Position.FORWARD, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Merapil", 84, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Maddie", 84, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Spotter", 84, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Aoba", 84, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Victorio", 84, Position.FORWARD, false, false);
        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Arion niño", 83, Position.MIDFIELDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Falco", 83, Position.FORWARD, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Grizzark", 84, Position.FORWARD, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Frank", 83, Position.DEFENDER, false, false);
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

        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Shinohara", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Krueger", 89, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Noviembre M ", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Octavia ", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Paolo ", 89, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Goldie ", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Krystal", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Gaiel", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Carvallo ", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Feig ", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Quentin", 89, Position.GOALKEEPER, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Whyte", 85, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Archery", 86, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Mai", 86, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Bobby", 84, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "So-Yeon", 86, Position.FORWARD, false, true);
        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Yuh ", 84, Position.FORWARD, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Regina", 84, Position.GOALKEEPER, false, false);
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
        playerService.createPlayer(user.getId(), team.getId(), "Josh Amber", 88, Position.GOALKEEPER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Acker", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Gabi", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "David Samford.A", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Jude Sharp", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Paul Rush", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Feirano", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Caleb", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Violet", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Okita", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Takeribi", 87, Position.FORWARD, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Strata", 86, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tom Dark", 86, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Ciara", 85, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Darren.A", 87, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Chief", 86, Position.MIDFIELDER, false, false);
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
        playerService.createPlayer(user.getId(), team.getId(), "Arthur", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Despina", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nae ", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Hyperion", 89, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Seok ", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Zaphod", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "DrakulV", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tabit", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Wanli", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Delisle", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "King", 88, Position.GOALKEEPER, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Zell", 86, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Vanguard", 86, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Vissi", 85, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Robingo", 84, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Master", 85, Position.MIDFIELDER, false, true);
        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Angello", 84, Position.MIDFIELDER, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Alnitak", 85, Position.MIDFIELDER, false, false);
    }

    private void crearEquipoInicialDesesperdidos(UserEntity user) throws Exception {
        String initialTeamName = "Desesperdidos";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        Path defaultImagePath = Paths.get("images/defaults/Desesperdidos.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Canthrup", 88, Position.GOALKEEPER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "PapaSor", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Alice", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Hurley", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Canterburi", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Zanark X2", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Yurkeh", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Bay Laurel", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Canis", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tezcat", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Ruger", 88, Position.FORWARD, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Ribriel", 84, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Melvil", 85, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Venus", 84, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Mason", 85, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Catora", 86, Position.FORWARD, false, true);
    }

    private void crearEquipoInicialBigBang(UserEntity user) throws Exception {
        String initialTeamName = "Big Bang";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        Path defaultImagePath = Paths.get("images/defaults/Big_Bang.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Zander", 88, Position.GOALKEEPER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Quintet", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Mitya", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Mustah", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Suruga", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Arion", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Roma", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Sakurazaki", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Ultranark", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Zabel", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Torch", 90, Position.FORWARD, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Hephestus", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Langvil", 84, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Arkew", 84, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "David", 84, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Zetoh", 83, Position.FORWARD, false, false);
        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Chameleon", 84, Position.MIDFIELDER, false, false);
    }

    private void crearEquipoInicialZeus(UserEntity user) throws Exception {
        String initialTeamName = "Zeus";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        Path defaultImagePath = Paths.get("images/defaults/Zeus.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Simeon", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nakata", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Sol Miximax", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Mecamark", 87, Position.GOALKEEPER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Gandares", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Sor", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Caleb Redux", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Cabrajal", 89, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Choi", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nikas", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Cuba Libre", 87, Position.DEFENDER, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Keenan", 83, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Zohen", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Faythe Heeler", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Acuto", 83, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Cypress", 85, Position.FORWARD, false, true);
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
        playerService.createPlayer(user.getId(), team.getId(), "Aster", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Samford", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Janus", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Dionysus ", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Bellatrix", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Thiago", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kalil", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Zanark", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "J Wallside ", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Black Flora ", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Il Grande", 88, Position.GOALKEEPER, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Alonso", 83, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "A Hobbes", 86, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "H Kane", 85, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Thor", 83, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Zack", 83, Position.FORWARD, false, true);
        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Infinity", 83, Position.FORWARD, false, false);
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
        playerService.createPlayer(user.getId(), team.getId(), "King Redux", 87, Position.GOALKEEPER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Gabi X Juana", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Lycus", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Agnethe", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Billy", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Sonny", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Max", 89, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Max Oscuro", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Wulfgang", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Gazelle", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Lucian", 87, Position.FORWARD, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Albizzi", 84, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Zippy", 85, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Maximino", 85, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Wenel", 85, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Handy", 85, Position.FORWARD, false, false);
        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Kayon", 83, Position.FORWARD, false, false);
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
        playerService.createPlayer(user.getId(), team.getId(), "Mark", 87, Position.GOALKEEPER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Aitriel", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Bomber", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nathan O", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Lean", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Soundtown", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Samford R", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Heat", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Ark", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Artist", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Skipper", 88, Position.FORWARD, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Houdini", 84, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Wittz", 85, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Krypto", 85, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Martin", 85, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Grent", 85, Position.GOALKEEPER, false, false);

        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Zell O", 84, Position.FORWARD, false, false);
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
        playerService.createPlayer(user.getId(), team.getId(), "Darren", 87, Position.GOALKEEPER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Averin", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Lus Kasim", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Obscura", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Aitor Cazador", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Steve", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Proude", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Karina", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Shaxel", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Thaddeus", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Langford", 87, Position.FORWARD, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Calanark", 86, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Chitoh", 83, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Erik", 86, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nathan Swift", 86, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Yale", 85, Position.DEFENDER, false, false);
        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Perseus", 86, Position.FORWARD, false, false);

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
        playerService.createPlayer(user.getId(), team.getId(), "Skie", 87, Position.GOALKEEPER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Amelia", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Clear", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Scotty", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tori", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Plink", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Jordan", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Riccardo", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Mehr", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Xavier", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Njord", 86, Position.FORWARD, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Keenan", 84, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Yurika", 86, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nelly", 84, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Norika", 84, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Malik", 84, Position.MIDFIELDER, false, false);
        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Sepia", 83, Position.MIDFIELDER, false, false);
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
        playerService.createPlayer(user.getId(), team.getId(), "Harper", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tsukikage", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Xing Zhou ", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Malcom oscuro ", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Roma mixmax ", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Romanark ", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Apollo", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "kevin", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Subaru ", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "eddy ", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Mark adulto", 89, Position.GOALKEEPER, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "gigi", 85, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "laymark", 85, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "shawn adulto ", 86, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "jp", 86, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Adriano", 86, Position.FORWARD, false, true);
        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Austin ", 86, Position.FORWARD, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "calmers", 86, Position.FORWARD, false, false);
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
        playerService.createPlayer(user.getId(), team.getId(), "Beta", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Flora", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Vladimir M", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Flappable", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Sapta", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Sierra M", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Sapta M", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Roleia", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tyrah", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Meteo", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Phobos", 88, Position.GOALKEEPER, true, true);

        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Legatus", 85, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Mohrir", 87, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Cloak", 84, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Victor", 86, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Rodh", 86, Position.MIDFIELDER, false, true);
        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Stoker", 83, Position.FORWARD, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Yuri", 84, Position.FORWARD, false, true);
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
        playerService.createPlayer(user.getId(), team.getId(), "Romica", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nelly", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Celeste", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kakehashi", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Jazmine", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Zhuge", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Juana", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Vinda", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Goldie", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Camellia", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Arbustillo", 88, Position.GOALKEEPER, true, true);

        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Noviembre", 84, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Mimi", 83, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Lina", 84, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Julia", 84, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Mercury", 84, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Juno", 84, Position.DEFENDER, false, true);

    }

    private void crearEquipoInicialCalaPirata(UserEntity user) throws Exception {
        String initialTeamName = "Cala Pirata";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        Path defaultImagePath = Paths.get("images/defaults/Cala_Pirata.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;
        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Samguk", 88, Position.GOALKEEPER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nathan", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Jack Wallside", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Zephyr Vittese", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Dakkar", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kisoji", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Arth", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "B Love", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Arculus Orbes", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Gamma", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Ozrock", 87, Position.FORWARD, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Dvalin", 86, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Meridia", 85, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Mountain", 86, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "C Stonewall", 86, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Rex Remington", 85, Position.FORWARD, false, true);
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

        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Axel", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Bash Lancer", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Shadow ", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Ar Ecks", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Ghiris ", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Rondula", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nobucardo", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Jim Oscuro", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Archer", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Malcolm", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Poseidon", 87, Position.GOALKEEPER, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Auttum", 86, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Al Woo", 86, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Rubu", 85, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Metron", 86, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Dvalin Plus", 85, Position.FORWARD, false, true);

        // Desconvocados
        playerService.createPlayer(user.getId(), team.getId(), "Edgar Partinus", 83, Position.FORWARD, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Owl", 86, Position.MIDFIELDER, false, false);
    }

    private void crearEquipoInicialPlenilunio(UserEntity user) throws Exception {
        String initialTeamName = "Plenilunio";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        // Copiar imagen por defecto
        Path defaultImagePath = Paths.get("images/defaults/Plenilunio.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;

        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Baictor", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Yukio", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Vladimir", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Layton", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Katie", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Starla", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Albleicher", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Faith", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Oleander", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Viorian", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Peabody", 87, Position.GOALKEEPER, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Mickey", 86, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Nenel", 86, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Kiburn", 85, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Clarisse", 84, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Aurelia", 84, Position.MIDFIELDER, false, true);
    }

    private void crearEquipoInicialBarcelonaOrb(UserEntity user) throws Exception {
        String initialTeamName = "Barcelona Orb";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        // Copiar imagen por defecto
        Path defaultImagePath = Paths.get("images/defaults/Barcelona_Orb.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;

        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Lancer (Zanark)", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Joan Asensi", 87, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Xavier (Adulto)", 88, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Clario Orvan", 88, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Sierra", 87, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Cronus Fourseasons", 87, Position.MIDFIELDER, true,
                true);
        playerService.createPlayer(user.getId(), team.getId(), "Domelgo Domínguez", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Aeron Rushmore", 88, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Zanark (Cao Cao)", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Pierre Godin", 87, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Dvalin (Zanark)", 88, Position.GOALKEEPER, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Kiona Blackwell", 85, Position.GOALKEEPER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Banda", 86, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Tulip", 86, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Canon Evans", 87, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Dryden Zephyr", 86, Position.MIDFIELDER, false, true);

        playerService.createPlayer(user.getId(), team.getId(), "SIlvain Hache", 86, Position.FORWARD, false, false);
        playerService.createPlayer(user.getId(), team.getId(), "Dave Dreem", 86, Position.DEFENDER, false, false);
    }

    private void crearEquipoInicialRobots(UserEntity user) throws Exception {
        String initialTeamName = "Robots";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        // Copiar imagen por defecto
        Path defaultImagePath = Paths.get("images/defaults/Robots.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;

        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Robots 1", 86, Position.GOALKEEPER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Robots 2", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Robots 3", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Robots 4", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Robots 5", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Robots 6", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Robots 7", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Robots 8", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Robots 9", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Robots 10", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Robots 11", 86, Position.FORWARD, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Robots 12", 86, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Robots 13", 86, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Robots 14", 86, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Robots 15", 86, Position.GOALKEEPER, false, true);

    }

    private void crearEquipoInicialTerracota(UserEntity user) throws Exception {
        String initialTeamName = "Terracota";
        String safeTeamName = initialTeamName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String teamImageDir = "images/" + user.getId();
        if (!Files.exists(Paths.get(teamImageDir))) {
            Files.createDirectories(Paths.get(teamImageDir));
        }
        String teamImageFile = safeTeamName + ".jpg";
        Path teamImagePath = Paths.get(teamImageDir, teamImageFile);

        // Copiar imagen por defecto
        Path defaultImagePath = Paths.get("images/defaults/Terracota.jpg");
        Files.copy(defaultImagePath, teamImagePath, StandardCopyOption.REPLACE_EXISTING);

        String teamProfilePictureUrl = "/images/" + user.getId() + "/" + teamImageFile;

        TeamEntity team = teamService.createTeam(user.getId(), initialTeamName, teamProfilePictureUrl);

        // Jugadores titulares
        playerService.createPlayer(user.getId(), team.getId(), "Terracota 1", 86, Position.GOALKEEPER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Terracota 2", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Terracota 3", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Terracota 4", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Terracota 5", 86, Position.DEFENDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Terracota 6", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Terracota 7", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Terracota 8", 86, Position.MIDFIELDER, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Terracota 9", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Terracota 10", 86, Position.FORWARD, true, true);
        playerService.createPlayer(user.getId(), team.getId(), "Terracota 11", 86, Position.FORWARD, true, true);
        // Suplentes
        playerService.createPlayer(user.getId(), team.getId(), "Terracota 12", 86, Position.DEFENDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Terracota 13", 86, Position.MIDFIELDER, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Terracota 14", 86, Position.FORWARD, false, true);
        playerService.createPlayer(user.getId(), team.getId(), "Terracota 15", 86, Position.GOALKEEPER, false, true);

    }

    @GetMapping
    public List<UserResponse> getUsers(@RequestParam(required = false) String name) {
        List<UserEntity> users;
        UserEntity user;

        if (name != null && !name.isBlank()) {
            user = userService.getByName(name);
            if (user == null) {
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