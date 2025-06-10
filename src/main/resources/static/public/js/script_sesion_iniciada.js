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
        // Comprobar que el usuario existe en la base de datos
        try {
            const res = await fetch(`http://localhost:8080/users?name=${encodeURIComponent(nombreUsuario)}`);
            const users = await res.json();
            if (!Array.isArray(users) || users.length === 0 || users[0].id != userId) {
                window.location.href = 'Iniciar_sesion.html';
            }
        } catch {
            window.location.href = 'Iniciar_sesion.html';
        }
    }

    // Evento para el botón "Modificar nombre de usuario"
    document.getElementById('Modificar_Nombre').addEventListener('click', async function () {
        const nuevoNombre = prompt("Introduce el nuevo nombre de usuario:");
        if (nuevoNombre) {
            const nombreAntiguo = localStorage.getItem('nombre_usuario'); 

            alert(`Nombre de usuario actualizado de: ${nombreAntiguo} a: ${nuevoNombre}`);
            try {
                const response = await fetch(`http://localhost:8080/users/changeName`, {
                    method: "PUT",
                    headers: { 
                        'Content-Type': "application/json"
                    },
                    body: JSON.stringify({
                        oldUsername: nombreAntiguo, 
                        newUsername: nuevoNombre  
                    })
                });

                if (!response.ok) {
                    throw new Error('No se pudo modificar el nombre de usuario');
                }
                localStorage.setItem('nombre_usuario', nuevoNombre);

            } catch (error) {
                console.error("Error:", error.message);
                alert("Ese nombre ya existe o es el mismo que ya tenias");
            }
        }
    });

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
                const response = await fetch(`http://localhost:8080/users/${userId}`, {
                    method: "DELETE",
                    headers: {
                        'Content-Type': "application/json"
                    }
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
            await fetch('http://localhost:8080/users/logout', {
                method: 'POST',
                credentials: 'include' // Importante para enviar cookies
            });
        } catch (e) {
            // Ignorar errores, continuar con el cierre de sesión local
        }
        localStorage.removeItem('nombre_usuario');
        localStorage.removeItem('user_id');
        window.location.href = 'Iniciar_sesion.html';
    });

    document.getElementById('btnPartidos').addEventListener('click', () => {
        window.location.href = 'partidos.html'; // Redirige a la página de Partidos
    });

    document.getElementById('btnJugadores').addEventListener('click', () => {
        window.location.href = 'jugadores.html'; // Redirige a la página de Jugadores
    });

    document.getElementById('btnEquipos').addEventListener('click', () => {
        window.location.href = 'equipos.html'; // Redirige a la página de Equipos
    });

 
    if (nombreUsuario) {
        fetch(`http://localhost:8080/users?name=${encodeURIComponent(nombreUsuario)}`)
            .then(res => res.json())
            .then(users => {
                if (users.length > 0 && users[0].profilePictureUrl) {
                    document.getElementById('userPhoto').src = '/users/images/' + users[0].profilePictureUrl.split('/').pop() + '?t=' + Date.now();
                }
            })
            .catch(() => {
                // Si hay error, deja la imagen por defecto
            });
    }

    // Llama a la función antes de cualquier otra lógica
    validarUsuario();
});



