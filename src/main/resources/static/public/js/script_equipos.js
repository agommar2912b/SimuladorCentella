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

// Funciones para abrir y cerrar el modal
function openCreateTeamModal() {
    document.getElementById("createTeamModal").style.display = "flex";
}

function closeCreateTeamModal() {
    document.getElementById("createTeamModal").style.display = "none";
}

// Función para abrir el modal con los botones "Editar" y "Eliminar"
function openEditTeamModal(team, imageUrl) {
    const modal = document.getElementById("editTeamModal");
    const teamInfo = document.getElementById("teamInfo");
    const editButton = document.getElementById("editTeamButton");
    const deleteButton = document.getElementById("deleteTeamButton");
    const img = document.getElementById("editTeamModalImg");

    teamInfo.textContent = team.name;
    img.src = imageUrl || "img/default_team.png";
    img.style.display = "block";

    editButton.onclick = () => openEditFormModal(team);
    deleteButton.onclick = () => deleteTeam(team.id);

    modal.style.display = "flex";
}

// Función para abrir el modal del formulario de edición
function openEditFormModal(team) {
    const editFormModal = document.getElementById("editFormModal");
    document.getElementById("editTeamName").value = team.name;
    document.getElementById("editTeamImageFile").value = "";

    const preview = document.getElementById("editTeamImagePreview");
    if (preview && team.profilePictureUrl) {
        preview.src = `/users/${userId}/teams/images/${userId}/${team.profilePictureUrl.split('/').pop()}?t=${Date.now()}`;
        preview.style.display = "block";
    }

    editFormModal.style.display = "flex";

    // Configurar el evento de envío del formulario
    const editForm = document.getElementById("editTeamForm");
    editForm.onsubmit = async (event) => {
        event.preventDefault();

        const updatedName = document.getElementById("editTeamName").value.trim();
        const updatedImageInput = document.getElementById("editTeamImageFile");
        const updatedImage = updatedImageInput.files[0];

        if (!updatedName) {
            alert("Por favor, introduce el nombre del equipo.");
            return;
        }

        const formData = new FormData();
        formData.append("name", updatedName);
        if (updatedImage) {
            formData.append("image", updatedImage);
        }

        try {
            const response = await fetch(`/users/${userId}/teams/${team.id}`, {
                method: "PATCH",
                body: formData,
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error("Error al editar el equipo.");
            }

            alert("Equipo editado con éxito.");
            editFormModal.style.display = "none"; 
            closeEditTeamModal(); 
            loadTeams(); 
        } catch (error) {
            console.error(error);
            alert("Hubo un error al editar el equipo (puede ser que el nombre esté repetido).");
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
        const response = await fetch(`/users/${userId}/teams/${teamId}`, {
            method: "DELETE",
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error("Error al eliminar el equipo.");
        }

        alert("Equipo eliminado con éxito.");
        closeEditTeamModal();
        loadTeams(); 
    } catch (error) {
        console.error(error);
        alert("Hubo un error al eliminar el equipo.");
    }
}

// Función para cargar equipos
async function loadTeams(name = "") {
    const equiposContainer = document.getElementById("equiposContainer");

    try {

        const url = `/users/${userId}/teams`;

        const response = await fetch(url, {
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error("Error al cargar los equipos");
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

            teamCard.querySelector('.equipo-img').onclick = (e) => {
                e.stopPropagation();
                openEditTeamModal(team, imageUrl);
            };

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

    loadTeams();

    // Lógica para manejar la creación de equipos
    document.getElementById("createTeamForm").addEventListener("submit", async (event) => {
        event.preventDefault();

        const teamName = document.getElementById("teamName").value.trim();
        const teamImageInput = document.getElementById("teamImageFile");
        const teamImage = teamImageInput.files[0];

        if (!teamName || !teamImage) {
            alert("Por favor, completa todos los campos.");
            return;
        }

        const formData = new FormData();
        formData.append("name", teamName);
        formData.append("image", teamImage);

        try {
            const response = await fetch(`/users/${userId}/teams`, {
                method: "POST",
                body: formData,
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error("Error al crear el equipo puede ser que ya tengas un equipo con ese nombre.");
            }

            alert("Equipo creado con éxito.");
            closeCreateTeamModal();
            loadTeams();
        } catch (error) {
            console.error(error);
            alert("Hubo un error al crear el equipo puede ser que ya tengas un equipo con ese nombre.");
        }
    });
});