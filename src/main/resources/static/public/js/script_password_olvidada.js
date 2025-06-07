document.addEventListener('DOMContentLoaded', () => {
    const errorMessage = document.getElementById('error-message');
    const questionGroup = document.getElementById('questionGroup');
    const newPasswordGroup = document.getElementById('newPasswordGroup');
    const changePasswordBtn = document.getElementById('changePassword');
    let currentUsername = "";

    errorMessage.style.display = 'none';

    document.getElementById('getQuestion').addEventListener('click', async function () {
        const nombreUsuario = document.getElementById('nombre_usuario').value.trim();
        if (!nombreUsuario) {
            errorMessage.style.display = 'block';
            errorMessage.innerText = 'Introduce el nombre de usuario';
            return;
        }
        try {
            const response = await fetch(`http://localhost:8080/users/security-question?username=${encodeURIComponent(nombreUsuario)}`);
            if (!response.ok) {
                errorMessage.style.display = 'block';
                errorMessage.innerText = 'Usuario no encontrado';
                return;
            }
            const question = await response.text();
            document.getElementById('securityQuestion').innerText = question;
            questionGroup.style.display = 'block';
            newPasswordGroup.style.display = 'block';
            changePasswordBtn.style.display = 'inline-block';
            errorMessage.style.display = 'none';
            currentUsername = nombreUsuario;
        } catch (error) {
            errorMessage.style.display = 'block';
            errorMessage.innerText = 'Error al conectar con el servidor';
        }
    });

    changePasswordBtn.addEventListener('click', async function () {
        const securityAnswer = document.getElementById('securityAnswer').value.trim();
        const newPassword = document.getElementById('new_password').value;

        if (!securityAnswer || !newPassword) {
            errorMessage.style.display = 'block';
            errorMessage.innerText = 'Rellena todos los campos';
            return;
        }

        const data = {
            username: currentUsername,
            securityAnswer: securityAnswer,
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
                errorMessage.innerText = json.message || 'Respuesta incorrecta o error al cambiar la contraseña';
                return;
            }

            window.location.href = 'Iniciar_sesion.html';
        } catch (error) {
            errorMessage.style.display = 'block';
            errorMessage.innerText = 'Error al conectar con el servidor';
        }
    });

    document.getElementById('limpiar').addEventListener('click', function (event) {
        event.preventDefault();
        document.getElementById('passwordRecoveryForm').reset();
        errorMessage.style.display = 'none';
        questionGroup.style.display = 'none';
        newPasswordGroup.style.display = 'none';
        changePasswordBtn.style.display = 'none';
    });
});
