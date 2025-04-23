
window.addEventListener("DOMContentLoaded", async () => {

    document.getElementById('configuracion').addEventListener('click', function () {
        const configuracionApartado = document.getElementById('configuracion-apartado');
        console.dir(configuracionApartado.style);
        configuracionApartado.style.display = (configuracionApartado.style.display === 'none' || configuracionApartado.style.display === "") ? 'block' : 'none';
    });

    try {
        const username = localStorage.getItem('nombre_usuario');
        if (username) {
            document.getElementById('userPhoto').src = `../imagenes/personas/${username}`;
        }

        // Generar opciones del selector de año
        const fundacionSelect = document.getElementById('fundacion-equipo');
        const currentYear = new Date().getFullYear();
        for (let year = 1800; year <= currentYear; year++) {
            const option = document.createElement('option');
            option.value = year;
            option.textContent = year;
            fundacionSelect.appendChild(option);
        }

        // Manejador de envío del formulario
        document.getElementById('form-registro-equipo').addEventListener('submit', function (e) {
            e.preventDefault();

            // Obtener datos del formulario
            const nombre = document.getElementById('nombre-equipo').value;
            const ciudad = document.getElementById('ciudad-equipo').value;
            const fundacion = document.getElementById('fundacion-equipo').value;
            const imagenInput = document.getElementById('imagen-equipo');
            const imagenArchivo = imagenInput.files[0];

            if (!imagenArchivo) {
                alert('Por favor, selecciona una imagen.');
                return;
            }

            // Crear un URL para la imagen seleccionada
            const imagenURL = URL.createObjectURL(imagenArchivo);

            // Crear tarjeta del equipo
            // Crear tarjeta del equipo
            const equipoCard = document.createElement('div');
            equipoCard.className = 'equipo-card';
            equipoCard.innerHTML = `
    <img src="${imagenURL}" alt="${nombre}">
    <h4>${nombre}</h4>
    <p>Ciudad: ${ciudad}</p>
    <p>Año de fundación: ${fundacion}</p>
`;

            // Añadir tarjeta al contenedor
            const equiposContainer = document.getElementById('equipos-container');
            equiposContainer.appendChild(equipoCard);
            equiposContainer.style.display = 'grid'; // Asegúrate de que el contenedor usa 'grid'


            // Resetear formulario
            this.reset();
        });

        // Cerrar sesión
        document.getElementById('Volver_Inicio').addEventListener('click', function () {
            window.location.href = 'Iniciar_sesion.html';
        });

        // Eliminar cuenta
        document.getElementById('Eliminar_cuenta').addEventListener('click', async function () {
            const confirmDelete = confirm("¿Estás seguro que quieres borrar la cuenta?");
            if (confirmDelete) {
                const nombre_usuario = localStorage.getItem('nombre_usuario');
                const token = localStorage.getItem('token');
                if (!nombre_usuario) {
                    alert("No se encontró el nombre de usuario en localStorage.");
                    return;
                }
                try {
                    const response = await fetch(`http://localhost:5050/api/iniciar/${nombre_usuario}`, {
                        method: "DELETE",
                        headers: {
                            'Authorization': token,
                            'Content-Type': "application/json"
                        }
                    });

                    if (!response.ok) {
                        throw new Error('No se pudo obtener la información del usuario');
                    }

                    window.location.href = 'Iniciar_sesion.html';
                    localStorage.removeItem('nombre_usuario');
                    localStorage.removeItem('token');
                } catch (error) {
                    console.error("Error:", error.message);
                    alert("Ocurrió un error al intentar eliminar la cuenta.");
                }
            }
        });
    } catch (error) {
        console.error("Error al inicializar:", error.message);
    }
});