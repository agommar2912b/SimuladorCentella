document.addEventListener('DOMContentLoaded', () => {
    const errorMessage = document.getElementById('error-message');
    errorMessage.style.display = 'none';

    document.getElementById('comparePassword').addEventListener('click', async function () {
        const nombreUsuario = document.getElementById('nombre_usuario').value;
        const oldPassword = document.getElementById('password').value;
        const newPassword = document.getElementById('new_password').value;

        const data = {
            username: nombreUsuario,
            old_password: oldPassword,
            new_password: newPassword
        };

        try {
            const response = await fetch('http://localhost:8080/users/changePassword', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                const json = await response.json();
                errorMessage.style.display = 'block';
                errorMessage.innerText = json.message || 'Error al cambiar la contraseña';
                return;
            }


            window.location.href = 'Iniciar_sesion.html';
        } catch (error) {
            console.error('Error:', error);
            errorMessage.style.display = 'block';
            errorMessage.innerText = 'Error al conectar con el servidor';
        }
    });

    document.getElementById('limpiar').addEventListener('click', function (event) {
        event.preventDefault();
        document.getElementById('registrationForm').reset();
        errorMessage.style.display = 'none';
        console.log("Formulario limpiado");
    });
});
