document.addEventListener("DOMContentLoaded", () => {
    const configuracionButton = document.getElementById("configuracion");
    const configuracionApartado = document.getElementById("configuracion-apartado");

    configuracionButton.addEventListener("click", () => {
        configuracionApartado.classList.toggle("abierto"); // Alternar la clase 'abierto'
    });


    // Evento para el botón "Modificar nombre de usuario"
    document.getElementById('Modificar_Nombre').addEventListener('click', async function () {
        const nuevoNombre = prompt("Introduce el nuevo nombre de usuario:");
        if (nuevoNombre) {
            const token = localStorage.getItem('token');
            const nombreAntiguo = localStorage.getItem('nombre_usuario'); // Obtener el nombre antiguo del localStorage

            alert(`Nombre de usuario actualizado de: ${nombreAntiguo} a: ${nuevoNombre}`);
            try {
                const response = await fetch(`http://localhost:8080/users/changeName`, {
                    method: "PUT",
                    headers: {
                        'Authorization': token,
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
            const userId = localStorage.getItem('user_id'); // <--- Asegúrate de guardar esto al iniciar sesión
            const token = localStorage.getItem('token');

            if (!userId) {
                alert("No se encontró el ID del usuario en localStorage.");
                return;
            }

            try {
                const response = await fetch(`http://localhost:8080/users/${userId}`, {
                    method: "DELETE",
                    headers: {
                        'Authorization': token,
                        'Content-Type': "application/json"
                    }
                });

                if (!response.ok) {
                    throw new Error('No se pudo eliminar la cuenta');
                }

                // Limpiar localStorage y redirigir
                localStorage.removeItem('nombre_usuario');
                localStorage.removeItem('token');
                localStorage.removeItem('user_id'); // <-- Limpiar el ID
                window.location.href = 'Iniciar_sesion.html';

            } catch (error) {
                console.error("Error:", error.message);
                alert("Ocurrió un error al intentar eliminar la cuenta.");
            }
        }
    });

    // Cerrar sesión
    document.getElementById('Volver_Inicio').addEventListener('click', function () {
        localStorage.removeItem('nombre_usuario');
        localStorage.removeItem('token');
        localStorage.removeItem('user_id');
        window.location.href = 'Iniciar_sesion.html';
    });
});

