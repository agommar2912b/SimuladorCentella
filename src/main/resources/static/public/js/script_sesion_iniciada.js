document.addEventListener("DOMContentLoaded", () => {
    const userId = localStorage.getItem('user_id');
    const nombreUsuario = localStorage.getItem('nombre_usuario');
    const configuracionButton = document.getElementById("configuracion");
    const configuracionApartado = document.getElementById("configuracion-apartado");

    configuracionButton.addEventListener("click", () => {
        configuracionApartado.classList.toggle("visible");
    });

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

    // MODAL CAMBIAR NOMBRE
    const modal = document.getElementById('modalCambiarNombre');
    const cerrarModal = document.getElementById('cerrarModalCambiarNombre');
    const guardarBtn = document.getElementById('guardarNuevoNombre');
    const cancelarBtn = document.getElementById('cancelarNuevoNombre');
    const errorNombre = document.getElementById('errorNombre');
    const nuevoNombreInput = document.getElementById('nuevoNombreInput');
    const formCambiarNombre = document.getElementById('formCambiarNombre');

    document.getElementById('Modificar_Nombre').addEventListener('click', function () {
        errorNombre.style.display = 'none';
        nuevoNombreInput.value = '';
        modal.style.display = 'flex';
        setTimeout(() => nuevoNombreInput.focus(), 100);
    });

    cerrarModal.onclick = cancelarBtn.onclick = function() {
        modal.style.display = 'none';
    };

    window.onclick = function(event) {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    };

    formCambiarNombre.onsubmit = async function(e) {
        e.preventDefault();
        const nuevoNombre = nuevoNombreInput.value.trim();
        if (!nuevoNombre) {
            errorNombre.textContent = "Introduce un nombre válido.";
            errorNombre.style.display = 'block';
            nuevoNombreInput.focus();
            return;
        }
        const nombreAntiguo = localStorage.getItem('nombre_usuario');
        try {
            const response = await fetch(`/users/changeName`, {
                method: "PUT",
                headers: { 'Content-Type': "application/json" },
                body: JSON.stringify({
                    oldUsername: nombreAntiguo,
                    newUsername: nuevoNombre
                }),
                credentials: 'include'
            });
            if (!response.ok) {
                throw new Error('No se pudo modificar el nombre de usuario');
            }
            localStorage.setItem('nombre_usuario', nuevoNombre);
            modal.style.display = 'none';
            alert(`Nombre de usuario actualizado de: ${nombreAntiguo} a: ${nuevoNombre}`);
            location.reload();
        } catch (error) {
            errorNombre.textContent = "Ese nombre ya existe o es el mismo que ya tenías.";
            errorNombre.style.display = 'block';
        }
    };

    // Eliminar cuenta
    document.getElementById('Eliminar_cuenta').addEventListener('click', async function () {
        const confirmDelete = confirm("¿Estás seguro que quieres borrar la cuenta?");
        if (confirmDelete) {
            const userId = localStorage.getItem('user_id'); 
            
            if (!userId) {
                alert("No se encontró el ID del usuario en localStorage.");
                return;
            }

            try {
                const response = await fetch(`/users/${userId}`, {
                    method: "DELETE",
                    headers: {
                        'Content-Type': "application/json"
                    },
                    credentials: 'include'
                });

                if (!response.ok) {
                    throw new Error('No se pudo eliminar la cuenta');
                }

              
                localStorage.removeItem('nombre_usuario');
                localStorage.removeItem('user_id');
                window.location.href = 'Iniciar_sesion.html';

            } catch (error) {
                console.error("Error:", error.message);
                alert("Ocurrió un error al intentar eliminar la cuenta.");
            }
        }
    });

    // Cerrar sesión
    document.getElementById('Volver_Inicio').addEventListener('click', async function () {
        try {
            await fetch('/users/logout', {
                method: 'POST',
                credentials: 'include'
            });
        } catch (e) {
            
        }
        localStorage.removeItem('nombre_usuario');
        localStorage.removeItem('user_id');
        window.location.href = 'Iniciar_sesion.html';
    });

    document.getElementById('btnPartidos').addEventListener('click', () => {
        window.location.href = 'Partidos.html'; // Redirige a la página de Partidos
    });

    document.getElementById('btnJugadores').addEventListener('click', () => {
        window.location.href = 'Jugadores.html'; // Redirige a la página de Jugadores
    });

    document.getElementById('btnEquipos').addEventListener('click', () => {
        window.location.href = 'Equipos.html'; // Redirige a la página de Equipos
    });

 
    if (nombreUsuario) {
        fetch(`/users?name=${encodeURIComponent(nombreUsuario)}`, {
            credentials: 'include',
            
        })
            .then(res => res.json())
            .then(users => {
                if (users.length > 0 && users[0].profilePictureUrl) {
                    document.getElementById('userPhoto').src = '/users/images/' + users[0].profilePictureUrl.split('/').pop() + '?t=' + Date.now();
                }
            })
            .catch(() => {
                
            });
    }

    validarUsuario();
});



