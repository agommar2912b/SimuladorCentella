// Obtener userId y token del localStorage
const userId = localStorage.getItem("user_id");
const token = localStorage.getItem("token");

// Función para cargar equipos en los selects
async function loadTeamsForSelects() {
    const selectLocal = document.getElementById("equipoLocal");
    const selectVisitante = document.getElementById("equipoVisitante");

    if (!userId || !token) {
        selectLocal.innerHTML = "<option>Error de usuario/token</option>";
        selectVisitante.innerHTML = "<option>Error de usuario/token</option>";
        return;
    }

    try {
        const url = `http://localhost:8080/users/${userId}/teams`;
        const response = await fetch(url, {
            headers: { Authorization: token },
        });

        if (!response.ok) throw new Error("Error al cargar los equipos");

        const teams = await response.json();

        if (teams.length === 0) {
            selectLocal.innerHTML = "<option>No hay equipos</option>";
            selectVisitante.innerHTML = "<option>No hay equipos</option>";
            return;
        }

        // Primera opción vacía
        selectLocal.innerHTML = `<option value="">Selecciona equipo local</option>` +
            teams.map(team =>
                `<option value="${team.id}">${team.name}</option>`
            ).join("");
        selectVisitante.innerHTML = `<option value="">Selecciona equipo visitante</option>` +
            teams.map(team =>
                `<option value="${team.id}">${team.name}</option>`
            ).join("");

        // Evitar seleccionar el mismo equipo en ambos selects
        selectLocal.addEventListener("change", () => {
            for (const opt of selectVisitante.options) {
                opt.disabled = opt.value && opt.value === selectLocal.value;
            }
        });
        selectVisitante.addEventListener("change", () => {
            for (const opt of selectLocal.options) {
                opt.disabled = opt.value && opt.value === selectVisitante.value;
            }
        });

        // Inicializar el bloqueo por defecto
        for (const opt of selectVisitante.options) {
            opt.disabled = opt.value && opt.value === selectLocal.value;
        }
    } catch (error) {
        selectLocal.innerHTML = "<option>Error al cargar</option>";
        selectVisitante.innerHTML = "<option>Error al cargar</option>";
    }
}

// Función para obtener los datos de un equipo y sus jugadores
async function getTeamWithPlayers(teamId) {
    const jugadoresUrl = `http://localhost:8080/users/${userId}/teams/${teamId}/players`;

    // Obtener jugadores
    const jugadoresResp = await fetch(jugadoresUrl, { headers: { Authorization: token } });
    const jugadores = await jugadoresResp.json();

    return { jugadores };
}

// Mostrar equipos y jugadores al registrar partido
document.getElementById("partidoForm").addEventListener("submit", async function(e) {
    e.preventDefault();

    const selectLocal = document.getElementById("equipoLocal");
    const selectVisitante = document.getElementById("equipoVisitante");
    const localId = selectLocal.value;
    const visitanteId = selectVisitante.value;
    const localName = selectLocal.options[selectLocal.selectedIndex].text;
    const visitanteName = selectVisitante.options[selectVisitante.selectedIndex].text;
    const resumenDiv = document.getElementById("partidoResumen");

    if (!localId || !visitanteId || localId === visitanteId) {
        resumenDiv.innerHTML = "<p style='color:red'>Selecciona dos equipos diferentes.</p>";
        return;
    }

    // Función para ordenar jugadores: titulares primero, luego por posición
    function ordenarJugadores(jugadores) {
        const posiciones = ["GOALKEEPER", "DEFENDER", "MIDFIELDER", "FORWARD"];
        return jugadores.slice().sort((a, b) => {
            if (a.hasPlayed !== b.hasPlayed) return b.hasPlayed - a.hasPlayed;
            return posiciones.indexOf(a.position) - posiciones.indexOf(b.position);
        });
    }

    // Cargar datos de ambos equipos y sus jugadores
    resumenDiv.innerHTML = "<p>Cargando equipos...</p>";
    try {
        const [local, visitante] = await Promise.all([
            getTeamWithPlayers(localId),
            getTeamWithPlayers(visitanteId)
        ]);

        const localJugadores = ordenarJugadores(local.jugadores);
        const visitanteJugadores = ordenarJugadores(visitante.jugadores);

        function lanzadoresCheckboxes(jugadores, equipo, tipo, label, color) {
            // Excluye porteros
            const jugadoresSinPortero = jugadores.filter(j => j.position !== "GOALKEEPER");
            return `
                <div class="lanzadores-box" style="flex:1;min-width:220px;">
                    <div style="font-weight:bold;color:${color};margin-bottom:0.5rem;">
                        ${label} <span style="font-size:0.9em;color:#888;">(marca uno o varios)</span>
                    </div>
                    <div style="display:flex;flex-direction:column;gap:0.4rem;">
                        ${jugadoresSinPortero.map(j => `
                            <label style="display:flex;align-items:center;gap:0.5em;cursor:pointer;">
                                <input type="checkbox" name="${equipo}_${tipo}" value="${j.id}" style="accent-color:${color};">
                                <span>${j.name} <span style="color:#888;font-size:0.95em;">(${j.position})</span></span>
                            </label>
                        `).join("")}
                    </div>
                </div>
            `;
        }

        resumenDiv.innerHTML = `
            <div class="plantilla-box" style="background:#f8fbff;border-radius:12px;padding:2rem 1rem;margin-bottom:2rem;box-shadow:0 2px 10px #005bb51a;">
                <h3 style="color:#005bb5;text-align:center;margin-bottom:1.2rem;">${localName}</h3>
                <table class="jugadores-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nombre</th>
                            <th>Habilidad</th>
                            <th>Posición</th>
                            <th>¿Titular?</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${
                            localJugadores.length === 0
                            ? `<tr><td colspan="5">No hay jugadores</td></tr>`
                            : localJugadores.map(j => `
                                <tr>
                                    <td>${j.id ?? '-'}</td>
                                    <td>${j.name ?? '-'}</td>
                                    <td>${j.skill ?? '-'}</td>
                                    <td>${j.position ?? '-'}</td>
                                    <td>${j.hasPlayed ? 'Sí' : 'No'}</td>
                                </tr>
                            `).join("")
                        }
                    </tbody>
                </table>
                <div style="display:flex;gap:2rem;justify-content:center;margin-top:1.5rem;flex-wrap:wrap;">
                    ${lanzadoresCheckboxes(localJugadores, "local", "faltas", "Lanzadores de faltas", "#1e88e5")}
                    ${lanzadoresCheckboxes(localJugadores, "local", "corners", "Lanzadores de córners", "#43a047")}
                    ${lanzadoresCheckboxes(localJugadores, "local", "penalty", "Lanzadores de penaltis", "#e53935")}
                </div>
            </div>
            <div class="plantilla-box" style="background:#f8fbff;border-radius:12px;padding:2rem 1rem;margin-bottom:2rem;box-shadow:0 2px 10px #005bb51a;">
                <h3 style="color:#005bb5;text-align:center;margin-bottom:1.2rem;">${visitanteName}</h3>
                <table class="jugadores-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nombre</th>
                            <th>Habilidad</th>
                            <th>Posición</th>
                            <th>¿Titular?</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${
                            visitanteJugadores.length === 0
                            ? `<tr><td colspan="5">No hay jugadores</td></tr>`
                            : visitanteJugadores.map(j => `
                                <tr>
                                    <td>${j.id ?? '-'}</td>
                                    <td>${j.name ?? '-'}</td>
                                    <td>${j.skill ?? '-'}</td>
                                    <td>${j.position ?? '-'}</td>
                                    <td>${j.hasPlayed ? 'Sí' : 'No'}</td>
                                </tr>
                            `).join("")
                        }
                    </tbody>
                </table>
                <div style="display:flex;gap:2rem;justify-content:center;margin-top:1.5rem;flex-wrap:wrap;">
                    ${lanzadoresCheckboxes(visitanteJugadores, "visitante", "faltas", "Lanzadores de faltas", "#1e88e5")}
                    ${lanzadoresCheckboxes(visitanteJugadores, "visitante", "corners", "Lanzadores de córners", "#43a047")}
                    ${lanzadoresCheckboxes(visitanteJugadores, "visitante", "penalty", "Lanzadores de penaltis", "#e53935")}
                </div>
            </div>
            <div style="text-align:center; margin-top:2rem;">
                <button id="simularBtn" style="background:#005bb5;color:#fff;padding:0.7rem 2rem;border:none;border-radius:20px;font-size:1.1rem;cursor:pointer;">Simular Partido</button>
                <div id="resultadoSimulacion" style="margin-top:1.5rem;"></div>
            </div>
        `;

        // Añadir evento al botón de simular
        document.getElementById("simularBtn").onclick = async function() {
            const resultadoDiv = document.getElementById("resultadoSimulacion");
            resultadoDiv.innerHTML = "<p>Simulando partido...</p>";
            try {
                const url = `http://localhost:8080/users/${userId}/teams/simulate?teamAName=${encodeURIComponent(localName)}&teamBName=${encodeURIComponent(visitanteName)}`;
                const response = await fetch(url, {
                    method: "POST",
                    headers: { Authorization: token }
                });
                if (!response.ok) {
                    const errorText = await response.text();
                    resultadoDiv.innerHTML = `<p style="color:red">Error: ${errorText}</p>`;
                    return;
                }
                const resultado = await response.text();

                // Separar eventos y resultado final
                const eventos = resultado.split("<br>");
                const resultadoFinal = eventos.pop(); // Última línea es el resultado final

                resultadoDiv.innerHTML = `
                    <div class="resultado-partido-box">
                        <div class="equipos-marcador">
                            <div class="equipo-nombre">
                                <img src="/public/img/equipos/${localName.replace(/\s+/g, '_').toLowerCase()}.png" 
                                     alt="${localName}" 
                                     onerror="this.onerror=null;this.src='/public/img/equipos/default.png';">
                                <span>${localName}</span>
                            </div>
                            <div class="marcador-central">
                                ${resultadoFinal.replace(/Resultado final: /, '').replace(localName, `<span class='marcador-local'>${localName}</span>`).replace(visitanteName, `<span class='marcador-visitante'>${visitanteName}</span>`)}
                            </div>
                            <div class="equipo-nombre">
                                <img src="/public/img/equipos/${visitanteName.replace(/\s+/g, '_').toLowerCase()}.png" 
                                     alt="${visitanteName}" 
                                     onerror="this.onerror=null;this.src='/public/img/equipos/default.png';">
                                <span>${visitanteName}</span>
                            </div>
                        </div>
                        <div class="eventos-timeline">
                            ${eventos.map(ev => `<div class="evento-linea">${ev}</div>`).join("")}
                        </div>
                    </div>
                `;
            } catch (err) {
                resultadoDiv.innerHTML = "<p style='color:red'>Error al simular el partido.</p>";
            }
        };

    } catch (err) {
        resumenDiv.innerHTML = "<p style='color:red'>Error al cargar los equipos o jugadores.</p>";
    }
});

function setTeamImage(selectId, imgDivId) {
    const select = document.getElementById(selectId);
    const imgDiv = document.getElementById(imgDivId);
    select.addEventListener("change", () => {
        const teamName = select.options[select.selectedIndex]?.text || "";
        // Si está en la opción por defecto, no mostramos imagen
        if (
            teamName.toLowerCase().includes("selecciona equipo") ||
            teamName.trim() === ""
        ) {
            imgDiv.innerHTML = "";
            return;
        }
        const imgSrc = `/public/img/equipos/${teamName.replace(/\s+/g, '_').toLowerCase()}.png`;
        imgDiv.innerHTML = `<img src="${imgSrc}" alt="${teamName}" onerror="this.onerror=null;this.src='/public/img/equipos/default.png';">`;
    });
}

// Ejecutar cuando el DOM esté listo
document.addEventListener("DOMContentLoaded", () => {
    loadTeamsForSelects();
    setTeamImage("equipoLocal", "imgLocal");
    setTeamImage("equipoVisitante", "imgVisitante");
});