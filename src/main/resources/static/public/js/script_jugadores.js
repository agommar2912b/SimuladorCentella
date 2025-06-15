const userId = localStorage.getItem('user_id');
const nombreUsuario = localStorage.getItem('nombre_usuario');

async function validarUsuario() {
    if (!userId || !nombreUsuario) {
        window.location.href = 'Iniciar_sesion.html';
        return;
    }
    try {
        const res = await fetch(`/users?name=${encodeURIComponent(nombreUsuario)}`, {
            credentials: 'include'
        });
        const users = await res.json();
        if (!Array.isArray(users) || users.length === 0 || users[0].id != userId) {
            window.location.href = 'Iniciar_sesion.html';
        }
    } catch {
        window.location.href = 'Iniciar_sesion.html';
    }
}

validarUsuario();


// Función para cargar equipos
async function loadTeams(name = "") {
    const equiposContainer = document.getElementById("equiposContainer");
    const jugadoresTitle = document.getElementById("jugadoresTitle");

    equiposContainer.innerHTML = "<p>Cargando equipos...</p>";
    jugadoresTitle.style.display = "none"; 

    try {
        const response = await fetch(`/users/${userId}/teams`, {
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error("Error al cargar los equipos.");
        }

        let teams = await response.json();

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

            const imageUrl = team.profilePictureUrl
                ? `/users/${userId}/teams/images/${userId}/${team.profilePictureUrl.split('/').pop()}?t=${Date.now()}`
                : "img/default_team.png";

            teamCard.innerHTML = `
                <img src="${imageUrl}" alt="Imagen del equipo" class="equipo-img" style="cursor:pointer;" />
                <h3>${team.name}</h3>
            `;

            teamCard.onclick = () => loadPlayers(team.id, team.name);

            equiposContainer.appendChild(teamCard);
        });
    } catch (error) {
        console.error(error);
        equiposContainer.innerHTML = "<p>Error al cargar los equipos.</p>";
    }
}

let currentTeamId = null; 

// Función para abrir el modal de creación de jugadores
function openCreatePlayerModal() {
    document.getElementById("createPlayerModal").style.display = "flex";
}

// Función para cerrar el modal de creación de jugadores
function closeCreatePlayerModal() {
    document.getElementById("createPlayerModal").style.display = "none";
}

// Función para abrir el modal de un jugador (mejorado)
function openPlayerModal(player) {
    const playerModal = document.getElementById("playerModal");
    document.getElementById("playerModalName").textContent = player.name;
    
    document.getElementById("playerModalSkill").textContent = `Habilidad: ${player.skill}`;
    document.getElementById("playerModalPosition").textContent = `Posición: ${traducirPosicion(player.position)}`;
    document.getElementById("playerModalHasPlayed").textContent = `¿Es titular?: ${player.hasPlayed ? "Sí" : "No"}`;

    document.getElementById("editPlayerButton").onclick = () => openEditPlayerModal(player);
    document.getElementById("deletePlayerButton").onclick = () => deletePlayer(player.id);

    playerModal.style.display = "flex";
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
        const response = await fetch(`/users/${userId}/teams/${currentTeamId}/players/${playerId}`, {
            method: "DELETE",
            credentials: 'include'
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
    closePlayerModal(); 
    openCreatePlayerModal(); 

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
            const response = await fetch(`/users/${userId}/teams/${currentTeamId}/players/${player.id}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",                
                },
                body: JSON.stringify({
                    name: updatedName,
                    skill: updatedSkill,
                    position: updatedPosition,
                    hasPlayed: updatedHasPlayed,
                }),
                credentials: 'include'
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

    document.getElementById("editPlayerName").value = player.name;
    document.getElementById("editPlayerSkill").value = player.skill;

    // Seleccionar la posición actual del jugador
    const positionSelect = document.getElementById("editPlayerPosition");
    positionSelect.value = player.position; 

    document.getElementById("editPlayerHasPlayed").checked = player.hasPlayed;

    // Guardar el ID del jugador en un atributo del formulario
    document.getElementById("editPlayerForm").dataset.playerId = player.id; 

    editPlayerModal.style.display = "flex"; 
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
    const jugadoresTable = document.querySelector(".jugadores-table");
    const jugadoresTitle = document.getElementById("jugadoresTitle");
    const btnCrearJugador = document.getElementById("btnCrearJugador");
    const jugadoresTableBody = jugadoresTable.querySelector("tbody");

    jugadoresTableBody.innerHTML = "<tr><td colspan='4'>Cargando jugadores...</td></tr>";
    jugadoresTable.style.display = "table";
    jugadoresTitle.style.display = "block";
    btnCrearJugador.style.display = "block";
    jugadoresTitle.textContent = `Jugadores del Equipo: ${teamName}`;
    currentTeamId = teamId;
    try {
        const response = await fetch(`/users/${userId}/teams/${teamId}/players`, {
            credentials: 'include'
        });
        if (!response.ok) throw new Error("Error al cargar jugadores");
        let jugadores = await response.json();

        if (typeof ordenarJugadores === "function") {
            jugadores = ordenarJugadores(jugadores);
        }

        if (jugadores.length === 0) {
            jugadoresTableBody.innerHTML = "<tr><td colspan='4'>No hay jugadores en este equipo.</td></tr>";
            return;
        }

        jugadoresTableBody.innerHTML = "";
        jugadores.forEach((jugador) => {
            const tr = document.createElement("tr");
            tr.style.cursor = "pointer";
            tr.onclick = () => openPlayerModal(jugador); 

            tr.innerHTML = `
                <td>${jugador.name}</td>
                <td>${jugador.skill}</td>
                <td>${traducirPosicion(jugador.position)}</td>
                <td>${jugador.hasPlayed ? "Sí" : "No"}</td>
            `;
            jugadoresTableBody.appendChild(tr);
        });
    } catch (error) {
        jugadoresTableBody.innerHTML = "<tr><td colspan='4'>Error al cargar los jugadores.</td></tr>";
        console.error(error);
    }
}

// Lógica para manejar la creación de jugadores
document.getElementById("createPlayerForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const playerName = document.getElementById("playerName").value.trim();
    const playerSkill = document.getElementById("playerSkill").value.trim();
    const playerPosition = document.getElementById("playerPosition").value.trim();
    const playerHasPlayed = document.getElementById("playerHasPlayed").checked;

    if (!playerName || !playerSkill || !playerPosition) {
        alert("Por favor, completa todos los campos.");
        return;
    }

    try {
        const response = await fetch(`/users/${userId}/teams/${currentTeamId}/players`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                name: playerName,
                skill: parseInt(playerSkill, 10),
                position: playerPosition.toUpperCase(),
                hasPlayed: playerHasPlayed, 
            }),
            credentials: 'include'
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

    const playerId = event.target.dataset.playerId;
    if (!playerId) {
        alert("Error interno: No se encontró el ID del jugador.");
        return;
    }

    const updatedName = document.getElementById("editPlayerName").value.trim();
    const updatedSkill = parseInt(document.getElementById("editPlayerSkill").value.trim(), 10);
    const updatedPosition = document.getElementById("editPlayerPosition").value; 
    const updatedHasPlayed = document.getElementById("editPlayerHasPlayed").checked;

    if (!updatedName || isNaN(updatedSkill) || !updatedPosition) {
        alert("Por favor, completa todos los campos.");
        return;
    }

    try {
        const response = await fetch(`/users/${userId}/teams/${currentTeamId}/players/${playerId}`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                name: updatedName,
                skill: updatedSkill,
                position: updatedPosition, 
                hasPlayed: updatedHasPlayed,
            }),
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error("Error al editar el jugador.");
        }

        alert("Jugador editado con éxito.");
        closeEditPlayerModal();
        closePlayerModal(); 
        loadPlayers(currentTeamId, document.getElementById("jugadoresTitle").textContent.split(": ")[1]); 
    } catch (error) {
        console.error("Error al editar el jugador:", error);
        alert("Hubo un error al editar el jugador.");
    }
});

// Función para traducir la posición del jugador al español
function traducirPosicion(pos) {
    switch (pos) {
        case "GOALKEEPER": return "Portero";
        case "DEFENDER": return "Defensa";
        case "MIDFIELDER": return "Mediocentro";
        case "FORWARD": return "Delantero";
        default: return pos;
    }
}

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
            jugadoresTable.style.display = "none";
            jugadoresTitle.style.display = "none";
            btnCrearJugador.style.display = "none";
            loadTeams(name);
        });
    }

    loadTeams();

    jugadoresTitle.style.display = "none";
    btnCrearJugador.style.display = "none";
});