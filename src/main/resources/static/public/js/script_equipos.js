// Declarar userId y token globalmente
const userId = localStorage.getItem("user_id"); // Obtener el ID del usuario desde localStorage
const token = localStorage.getItem("token"); // Obtener el token desde localStorage

// Funciones para abrir y cerrar el modal
function openCreateTeamModal() {
    document.getElementById("createTeamModal").style.display = "flex";
}

function closeCreateTeamModal() {
    document.getElementById("createTeamModal").style.display = "none";
}

// Función para abrir el modal con los botones "Editar" y "Eliminar"
function openEditTeamModal(team) {
    const modal = document.getElementById("editTeamModal");
    const teamInfo = document.getElementById("teamInfo");
    const editButton = document.getElementById("editTeamButton");
    const deleteButton = document.getElementById("deleteTeamButton");

    // Mostrar información del equipo
    teamInfo.textContent = `Equipo: ${team.name} (ID: ${team.id})`;

    // Configurar acciones de los botones
    editButton.onclick = () => openEditFormModal(team); // Abrir el modal de edición
    deleteButton.onclick = () => deleteTeam(team.id); // Eliminar el equipo

    modal.style.display = "flex"; // Mostrar el modal
}

// Función para abrir el modal del formulario de edición
function openEditFormModal(team) {
    const editFormModal = document.getElementById("editFormModal");

    // Rellenar los campos del formulario con los datos del equipo
    document.getElementById("editTeamName").value = team.name;
    document.getElementById("editTeamImage").value = team.profilePictureUrl;

    // Mostrar el modal del formulario de edición
    editFormModal.style.display = "flex";

    // Configurar el evento de envío del formulario
    const editForm = document.getElementById("editTeamForm");
    editForm.onsubmit = async (event) => {
        event.preventDefault();

        const updatedName = document.getElementById("editTeamName").value.trim();
        const updatedImage = document.getElementById("editTeamImage").value.trim();

        if (!updatedName || !updatedImage) {
            alert("Por favor, completa todos los campos.");
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/users/${userId}/teams/${team.id}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: token,
                },
                body: JSON.stringify({
                    name: updatedName,
                    profilePictureUrl: updatedImage,
                }),
            });

            if (!response.ok) {
                throw new Error("Error al editar el equipo.");
            }

            alert("Equipo editado con éxito.");
            editFormModal.style.display = "none"; // Cerrar el modal de edición
            closeEditTeamModal(); // Cerrar el modal principal
            loadTeams(); // Recargar la lista de equipos
        } catch (error) {
            console.error(error);
            alert("Hubo un error al editar el equipo (Puede ser que el nombre este repetido).");
        }
    };
}

// Función para cerrar el modal principal
function closeEditTeamModal() {
    const modal = document.getElementById("editTeamModal");
    modal.style.display = "none";
}

// Función para cerrar el modal del formulario de edición
function closeEditFormModal() {
    const editFormModal = document.getElementById("editFormModal");
    editFormModal.style.display = "none";
}

// Función para eliminar un equipo
async function deleteTeam(teamId) {
    if (!confirm("¿Estás seguro de que deseas eliminar este equipo?")) {
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/users/${userId}/teams/${teamId}`, {
            method: "DELETE",
            headers: {
                Authorization: token,
            },
        });

        if (!response.ok) {
            throw new Error("Error al eliminar el equipo.");
        }

        alert("Equipo eliminado con éxito.");
        closeEditTeamModal();
        loadTeams(); // Recargar la lista de equipos
    } catch (error) {
        console.error(error);
        alert("Hubo un error al eliminar el equipo.");
    }
}

// Función para cargar equipos
async function loadTeams(name = "") {
    const equiposContainer = document.getElementById("equiposContainer");

    if (!userId || !token) {
        equiposContainer.innerHTML = "<p>Error: No se encontró el ID del usuario o el token.</p>";
        return;
    }

    try {
        const url = name
            ? `http://localhost:8080/users/${userId}/teams?name=${name}`
            : `http://localhost:8080/users/${userId}/teams`;

        const response = await fetch(url, {
            headers: {
                Authorization: token,
            },
        });

        if (!response.ok) {
            throw new Error("Error al cargar los equipos");
        }

        const teams = await response.json();
        equiposContainer.innerHTML = ""; // Limpiar el contenedor

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

            // Asignar evento onclick para abrir el modal de edición/eliminación
            teamCard.onclick = () => openEditTeamModal(team);

            equiposContainer.appendChild(teamCard);
        });
    } catch (error) {
        console.error(error);
        equiposContainer.innerHTML = "<p>Error al cargar los equipos.</p>";
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const searchInput = document.getElementById("searchInput");
    const searchButton = document.getElementById("searchButton");

    // Evento para buscar equipos
    searchButton.addEventListener("click", () => {
        const name = searchInput.value.trim();
        loadTeams(name);
    });

    // Cargar todos los equipos al inicio
    loadTeams();

    // Lógica para manejar la creación de equipos
    document.getElementById("createTeamForm").addEventListener("submit", async (event) => {
        event.preventDefault();

        const teamName = document.getElementById("teamName").value.trim();
        const teamImage = document.getElementById("teamImage").value.trim();

        if (!teamName || !teamImage) {
            alert("Por favor, completa todos los campos.");
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/users/${userId}/teams`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: token,
                },
                body: JSON.stringify({
                    name: teamName,
                    profilePictureUrl: teamImage,
                }),
            });

            if (!response.ok) {
                throw new Error("Error al crear el equipo puede ser que ya tengas un equipo con ese nombre.");
            }

            alert("Equipo creado con éxito.");
            closeCreateTeamModal();
            loadTeams(); // Recargar la lista de equipos
        } catch (error) {
            console.error(error);
            alert("Hubo un error al crear el equipo puede ser que ya tengas un equipo con ese nombre.");
        }
    });
});