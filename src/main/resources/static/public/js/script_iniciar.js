document.addEventListener('DOMContentLoaded', () => {
    const errorMessage = document.getElementById('error-message');
    errorMessage.style.display = 'none';


    document.getElementById('comparePassword').addEventListener('click', async function() {
        const nombreUsuario = document.getElementById('nombre_usuario').value;
        console.log(nombreUsuario)
        const password = document.getElementById('password').value;
        console.log(password)
        let jsonbody = JSON.stringify({
            name: nombreUsuario,
            password: password
        });

        console.log("El json body es"+jsonbody)
    
        try {
            const response = await fetch('http://localhost:8080/users/login',{
                method:"POST",
                body:jsonbody,
                headers:{
                    "Content-Type":"application/json"
                }
            });
            
            if (!response.ok) {
                console.log('Contraseña incorrecta');
                const errorMessage = document.getElementById('error-message');
                errorMessage.style.display = 'block';
                errorMessage.style.color = 'red'

            }
            else{
                const usuarioData = await response.json();
                console.log(usuarioData)
                window.location.href = 'Sesion_iniciada.html' 
                localStorage.setItem('nombre_usuario', nombreUsuario);
                localStorage.setItem('user_id', usuarioData.id);
                }
            } 
     catch (error) {
            console.error("Error:", error.message);
        }
    });
    
    document.getElementById('limpiar').addEventListener('click', function(event) {
        event.preventDefault();
        const errorMessage = document.getElementById('error-message');
        errorMessage.style.display = 'none'
        document.getElementById('registrationForm').reset();
        console.log("Formulario limpiado");
    });
});


