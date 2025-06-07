document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('createForm').addEventListener('submit', async function (e) {
        e.preventDefault();

        const nombre = document.getElementById('nombre').value;
        const contraseña = document.getElementById('contraseña').value;
        const imagenInput = document.getElementById('imagen');
        const imagen = imagenInput.files[0];
        const pregunta = document.getElementById('pregunta').value;
        const respuesta = document.getElementById('respuesta').value;

        try {
            const formData = new FormData();
            formData.append('name', nombre);
            formData.append('password', contraseña);
            if (imagen) {
                formData.append('image', imagen);
            }
            formData.append('securityQuestion', pregunta);
            formData.append('securityAnswer', respuesta);

            const response = await fetch('http://localhost:8080/users', {
                method: 'POST',
                body: formData
            });

            if (!response.ok) throw new Error('Error al crear usuario');

            const data = await response.json();

            document.getElementById('usuario_creado').style.display = 'block';
            document.getElementById('parrafo_info').textContent = `Usuario ${data.name} creado con éxito con ID ${data.id}`;
            document.getElementById('parrafo_info').style.display = 'block';

            window.location.href = 'Iniciar_sesion.html'; 

        } catch (error) {
            console.error('Error:', error);
            alert('Error al crear usuario');
        }
    });

    document.getElementById('imagen').addEventListener('change', function() {
        const fileName = this.files[0] ? this.files[0].name : 'Ningún archivo seleccionado';
        document.getElementById('fileName').textContent = fileName;
    });
});
