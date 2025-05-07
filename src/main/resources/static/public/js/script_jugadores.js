const userId = localStorage.getItem("user_id"); // Obtener el ID del usuario desde localStorage
const token = localStorage.getItem("token"); // Obtener el token desde localStorage

console.log("Token:", token);
if (!token) {
    console.error("No se encontró el token en localStorage.");
}

// Función para cargar equipos
async function loadTeams() {
    const equiposContainer = document.getElementById("equiposContainer");
    const jugadoresContainer = document.getElementById("jugadoresContainer");
    const jugadoresTitle = document.getElementById("jugadoresTitle");

    equiposContainer.innerHTML = "<p>Cargando equipos...</p>";
    jugadoresContainer.style.display = "none";
    jugadoresTitle.style.display = "none";

    try {
        const response = await fetch(`http://localhost:8080/users/${userId}/teams`, {
            headers: {
                Authorization: token,
            },
        });

        if (!response.ok) {
            throw new Error("Error al cargar los equipos.");
        }

        const teams = await response.json();
        equiposContainer.innerHTML = "";

        if (teams.length === 0) {
            equiposContainer.innerHTML = "<p>No se encontraron equipos.</p>";
            return;
        }

        teams.forEach((team) => {
            const teamCard = document.createElement("div");
            teamCard.classList.add("equipo-card");

            teamCard.innerHTML = `
                <h3>${team.name}</h3>
                <p>ID: ${team.id}</p>
            `;

            // Asignar evento onclick para cargar los jugadores del equipo
            teamCard.onclick = () => loadPlayers(team.id, team.name);

            equiposContainer.appendChild(teamCard);
        });
    } catch (error) {
        console.error(error);
        equiposContainer.innerHTML = "<p>Error al cargar los equipos.</p>";
    }
}

let currentTeamId = null; // Variable para almacenar el ID del equipo seleccionado

// Función para abrir el modal de creación de jugadores
function openCreatePlayerModal() {
    document.getElementById("createPlayerModal").style.display = "flex";
}

// Función para cerrar el modal de creación de jugadores
function closeCreatePlayerModal() {
    document.getElementById("createPlayerModal").style.display = "none";
}

// Función para cargar jugadores de un equipo
async function loadPlayers(teamId, teamName) {
    const jugadoresContainer = document.getElementById("jugadoresContainer");
    const jugadoresTitle = document.getElementById("jugadoresTitle");
    const btnCrearJugador = document.getElementById("btnCrearJugador");

    jugadoresContainer.innerHTML = "<p>Cargando jugadores...</p>";
    jugadoresContainer.style.display = "block";
    jugadoresTitle.style.display = "block";
    btnCrearJugador.style.display = "block"; // Mostrar el botón de crear jugador
    jugadoresTitle.textContent = `Jugadores del Equipo: ${teamName}`;
    currentTeamId = teamId; // Guardar el ID del equipo seleccionado

    try {
        const response = await fetch(`http://localhost:8080/users/${userId}/teams/${teamId}/players`, {
            headers: {
                Authorization: token,
            },
        });

        if (!response.ok) {
            jugadoresContainer.innerHTML = "<p>Error: No se pudieron cargar los jugadores. Verifica tu conexión o intenta más tarde.</p>";
            throw new Error("Error al cargar los jugadores.");
        }

        const players = await response.json();
        console.log("Jugadores recibidos:", players);
        jugadoresContainer.innerHTML = "";

        if (players.length === 0) {
            jugadoresContainer.innerHTML = "<p>No se encontraron jugadores.</p>";
            return;
        }

        players.forEach((player) => {
            const playerCard = document.createElement("div");
            playerCard.classList.add("jugador-card");

            playerCard.innerHTML = `
                <h3>${player.name}</h3>
                <p>Posición: ${player.position}</p>
                <p>ID: ${player.id}</p>
            `;

            jugadoresContainer.appendChild(playerCard);
        });
    } catch (error) {
        console.error("Error al cargar los jugadores:", error);
        jugadoresContainer.innerHTML = "<p>Error al cargar los jugadores.</p>";
    }
}

// Lógica para manejar la creación de jugadores
document.getElementById("createPlayerForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    // Obtener los valores de los campos del formulario
    const playerName = document.getElementById("playerName").value.trim();
    const playerSkill = document.getElementById("playerSkill").value.trim();
    const playerPosition = document.getElementById("playerPosition").value.trim();
    const playerHasPlayed = document.getElementById("playerHasPlayed").checked;

    // Validar que los campos no estén vacíos
    if (!playerName || !playerSkill || !playerPosition) {
        alert("Por favor, completa todos los campos.");
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/users/${userId}/teams/${currentTeamId}/players`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: token,
            },
            body: JSON.stringify({
                name: playerName,
                skill: parseInt(playerSkill, 10), // Convertir a número
                position: playerPosition.toUpperCase(), // Convertir a mayúsculas para coincidir con el Enum
                hasPlayed: playerHasPlayed, // Booleano
            }),
        });

        if (!response.ok) {
            throw new Error("Error al crear el jugador.");
        }

        alert("Jugador creado con éxito.");
        closeCreatePlayerModal();
        loadPlayers(currentTeamId, document.getElementById("jugadoresTitle").textContent.split(": ")[1]); // Recargar jugadores
    } catch (error) {
        console.error("Error al crear el jugador:", error);
        alert("Hubo un error al crear el jugador. Intenta nuevamente.");
    }
});

// Cargar equipos al cargar la página
document.addEventListener("DOMContentLoaded", loadTeams);