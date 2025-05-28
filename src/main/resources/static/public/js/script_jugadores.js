const userId = localStorage.getItem("user_id"); // Obtener el ID del usuario desde localStorage
const token = localStorage.getItem("token"); // Obtener el token desde localStorage

console.log("Token:", token);
if (!token) {
    console.error("No se encontró el token en localStorage.");
}

// Función para cargar equipos
async function loadTeams(name = "") {
    const equiposContainer = document.getElementById("equiposContainer");
    const jugadoresTitle = document.getElementById("jugadoresTitle");

    equiposContainer.innerHTML = "<p>Cargando equipos...</p>";
    jugadoresTitle.style.display = "none"; // Ocultar el título de jugadores

    try {
        const response = await fetch(`http://localhost:8080/users/${userId}/teams`, {
            headers: {
                Authorization: token,
            },
        });

        if (!response.ok) {
            throw new Error("Error al cargar los equipos.");
        }

        let teams = await response.json();

        // Filtrado parcial en frontend (insensible a mayúsculas/minúsculas)
        if (name) {
            const search = name.toLowerCase();
            teams = teams.filter(team => team.name.toLowerCase().includes(search));
        }

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

// Función para abrir el modal de un jugador
function openPlayerModal(player) {
    const playerModal = document.getElementById("playerModal");
    const playerInfo = document.getElementById("playerInfo");
    const editPlayerButton = document.getElementById("editPlayerButton");
    const deletePlayerButton = document.getElementById("deletePlayerButton");

    // Mostrar información del jugador
    playerInfo.textContent = `Jugador: ${player.name} (ID: ${player.id})\nHabilidad: ${player.skill}\nPosición: ${player.position}\n¿Es titular?: ${player.hasPlayed ? "Sí" : "No"}`;

    // Configurar acciones de los botones
    editPlayerButton.onclick = () => openEditPlayerModal(player); // Abrir el nuevo modal de edición
    deletePlayerButton.onclick = () => deletePlayer(player.id); // Eliminar el jugador

    playerModal.style.display = "flex"; // Mostrar el modal
}

// Función para cerrar el modal del jugador
function closePlayerModal() {
    const playerModal = document.getElementById("playerModal");
    playerModal.style.display = "none";
}

// Función para eliminar un jugador
async function deletePlayer(playerId) {
    if (!confirm("¿Estás seguro de que deseas eliminar este jugador?")) {
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/users/${userId}/teams/${currentTeamId}/players/${playerId}`, {
            method: "DELETE",
            headers: {
                Authorization: token,
            },
        });

        if (!response.ok) {
            throw new Error("Error al eliminar el jugador.");
        }

        alert("Jugador eliminado con éxito.");
        closePlayerModal();
        loadPlayers(currentTeamId, document.getElementById("jugadoresTitle").textContent.split(": ")[1]); // Recargar jugadores
    } catch (error) {
        console.error("Error al eliminar el jugador:", error);
        alert("Hubo un error al eliminar el jugador.");
    }
}

// Función para abrir el formulario de edición de un jugador
function openEditPlayerForm(player) {
    closePlayerModal(); // Cerrar el modal principal
    openCreatePlayerModal(); // Reutilizar el modal de creación para edición

    // Rellenar los campos del formulario con los datos del jugador
    document.getElementById("playerName").value = player.name;
    document.getElementById("playerSkill").value = player.skill;
    document.getElementById("playerPosition").value = player.position;
    document.getElementById("playerHasPlayed").checked = player.hasPlayed;

    // Configurar el evento de envío del formulario
    const createPlayerForm = document.getElementById("createPlayerForm");
    createPlayerForm.onsubmit = async (event) => {
        event.preventDefault();

        const updatedName = document.getElementById("playerName").value.trim();
        const updatedSkill = parseInt(document.getElementById("playerSkill").value.trim(), 10);
        const updatedPosition = document.getElementById("playerPosition").value.trim();
        const updatedHasPlayed = document.getElementById("playerHasPlayed").checked;

        if (!updatedName || isNaN(updatedSkill) || !updatedPosition) {
            alert("Por favor, completa todos los campos.");
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/users/${userId}/teams/${currentTeamId}/players/${player.id}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: token,
                },
                body: JSON.stringify({
                    name: updatedName,
                    skill: updatedSkill,
                    position: updatedPosition,
                    hasPlayed: updatedHasPlayed,
                }),
            });

            if (!response.ok) {
                throw new Error("Error al editar el jugador.");
            }

            alert("Jugador editado con éxito.");
            closeCreatePlayerModal();
            loadPlayers(currentTeamId, document.getElementById("jugadoresTitle").textContent.split(": ")[1]); // Recargar jugadores
        } catch (error) {
            console.error("Error al editar el jugador:", error);
            alert("Hubo un error al editar el jugador.");
        }
    };
}

// Función para abrir el modal de edición de un jugador
function openEditPlayerModal(player) {
    const editPlayerModal = document.getElementById("editPlayerModal");

    // Rellenar los campos del formulario con los datos del jugador
    document.getElementById("editPlayerName").value = player.name;
    document.getElementById("editPlayerSkill").value = player.skill;

    // Seleccionar la posición actual del jugador
    const positionSelect = document.getElementById("editPlayerPosition");
    positionSelect.value = player.position; // Asegúrate de que el valor coincida con las opciones del select

    document.getElementById("editPlayerHasPlayed").checked = player.hasPlayed;

    // Guardar el ID del jugador en un atributo del formulario
    document.getElementById("editPlayerForm").dataset.playerId = player.id;

    editPlayerModal.style.display = "flex"; // Mostrar el modal
}

// Función para cerrar el modal de edición de un jugador
function closeEditPlayerModal() {
    const editPlayerModal = document.getElementById("editPlayerModal");
    editPlayerModal.style.display = "none";
}

// Función para ordenar jugadores: titulares primero, luego por posición
function ordenarJugadores(jugadores) {
    const posiciones = ["GOALKEEPER", "DEFENDER", "MIDFIELDER", "FORWARD"];
    return jugadores.slice().sort((a, b) => {
        if (a.hasPlayed !== b.hasPlayed) return b.hasPlayed - a.hasPlayed;
        return posiciones.indexOf(a.position) - posiciones.indexOf(b.position);
    });
}

// Función para cargar jugadores de un equipo
async function loadPlayers(teamId, teamName) {
    const jugadoresTable = document.getElementById("jugadoresTable");
    const jugadoresTitle = document.getElementById("jugadoresTitle");
    const btnCrearJugador = document.getElementById("btnCrearJugador");
    const jugadoresTableBody = jugadoresTable.querySelector("tbody");

    // Ocultar la tabla y mostrar un mensaje de carga
    jugadoresTableBody.innerHTML = "<tr><td colspan='5'>Cargando jugadores...</td></tr>";
    jugadoresTable.style.display = "table"; // Mostrar la tabla
    jugadoresTitle.style.display = "block"; // Mostrar el título
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
            jugadoresTableBody.innerHTML = "<tr><td colspan='5'>Error: No se pudieron cargar los jugadores.</td></tr>";
            throw new Error("Error al cargar los jugadores.");
        }

        let players = await response.json();
        jugadoresTableBody.innerHTML = "";

        if (players.length === 0) {
            jugadoresTableBody.innerHTML = "<tr><td colspan='5'>No se encontraron jugadores.</td></tr>";
            return;
        }

        // Ordenar jugadores: titulares primero, luego por posición
        players = ordenarJugadores(players);

        players.forEach((player) => {
            const row = document.createElement("tr");

            row.innerHTML = `
                <td style="border: 1px solid #ccc; padding: 8px;">${player.id}</td>
                <td style="border: 1px solid #ccc; padding: 8px;">${player.name}</td>
                <td style="border: 1px solid #ccc; padding: 8px;">${player.skill}</td>
                <td style="border: 1px solid #ccc; padding: 8px;">${player.position}</td>
                <td style="border: 1px solid #ccc; padding: 8px;">${player.hasPlayed ? "Sí" : "No"}</td>
            `;

            // Asignar evento onclick para abrir el modal del jugador
            row.onclick = () => openPlayerModal(player);

            jugadoresTableBody.appendChild(row);
        });
    } catch (error) {
        console.error("Error al cargar los jugadores:", error);
        jugadoresTableBody.innerHTML = "<tr><td colspan='5'>Error al cargar los jugadores.</td></tr>";
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

// Manejar la edición de un jugador
document.getElementById("editPlayerForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const playerId = event.target.dataset.playerId; // Obtener el ID del jugador
    const updatedName = document.getElementById("editPlayerName").value.trim();
    const updatedSkill = parseInt(document.getElementById("editPlayerSkill").value.trim(), 10);
    const updatedPosition = document.getElementById("editPlayerPosition").value; // Obtener el valor seleccionado
    const updatedHasPlayed = document.getElementById("editPlayerHasPlayed").checked;

    if (!updatedName || isNaN(updatedSkill) || !updatedPosition) {
        alert("Por favor, completa todos los campos.");
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/users/${userId}/teams/${currentTeamId}/players/${playerId}`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                Authorization: token,
            },
            body: JSON.stringify({
                name: updatedName,
                skill: updatedSkill,
                position: updatedPosition, // Enviar la posición seleccionada
                hasPlayed: updatedHasPlayed,
            }),
        });

        if (!response.ok) {
            throw new Error("Error al editar el jugador.");
        }

        alert("Jugador editado con éxito.");
        closeEditPlayerModal(); // Cerrar el modal de edición
        closePlayerModal(); // Cerrar el modal con los botones
        loadPlayers(currentTeamId, document.getElementById("jugadoresTitle").textContent.split(": ")[1]); // Recargar jugadores
    } catch (error) {
        console.error("Error al editar el jugador:", error);
        alert("Hubo un error al editar el jugador.");
    }
});

// Evento para el buscador
document.addEventListener("DOMContentLoaded", () => {
    const searchInput = document.getElementById("searchInput");
    const searchButton = document.getElementById("searchButton");
    const jugadoresTable = document.getElementById("jugadoresTable");
    const jugadoresTitle = document.getElementById("jugadoresTitle");
    const btnCrearJugador = document.getElementById("btnCrearJugador");

    if (searchButton && searchInput) {
        searchButton.addEventListener("click", () => {
            const name = searchInput.value.trim();
            // Oculta la tabla y el título de jugadores al buscar equipos
            jugadoresTable.style.display = "none";
            jugadoresTitle.style.display = "none";
            btnCrearJugador.style.display = "none";
            loadTeams(name);
        });
    }

    // Cargar todos los equipos al inicio
    loadTeams();

    // Ocultar la tabla al cargar la página
    jugadoresTable.style.display = "none";
    jugadoresTitle.style.display = "none";
    btnCrearJugador.style.display = "none";
});