const userId = localStorage.getItem('user_id');
const nombreUsuario = localStorage.getItem('nombre_usuario');

async function validarUsuario() {
    if (!userId || !nombreUsuario) {
        window.location.href = 'Iniciar_sesion.html';
        return;
    }
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

validarUsuario();

// Función para cargar equipos en los selects
async function loadTeamsForSelects() {
  const selectLocal = document.getElementById("equipoLocal");
  const selectVisitante = document.getElementById("equipoVisitante");

  if (!userId ) {
    selectLocal.innerHTML = "<option>Error de usuario</option>";
    return;
  }

  try {
    const url = `http://localhost:8080/users/${userId}/teams`;
    const response = await fetch(url, {
    });

    if (!response.ok) throw new Error("Error al cargar los equipos");

    const teams = await response.json();

    if (teams.length === 0) {
      selectLocal.innerHTML = "<option>No hay equipos</option>";
      selectVisitante.innerHTML = "<option>No hay equipos</option>";
      return;
    }

    selectLocal.innerHTML =
      `<option value="">Selecciona equipo local</option>` +
      teams
        .map(
          (team) =>
            `<option value="${team.id}" data-img="${team.profilePictureUrl ? `/users/${userId}/teams/images/${userId}/${team.profilePictureUrl.split('/').pop()}` : '/public/img/equipos/default.png'}">${team.name}</option>`
        )
        .join("");
    selectVisitante.innerHTML =
      `<option value="">Selecciona equipo visitante</option>` +
      teams
        .map(
          (team) =>
            `<option value="${team.id}" data-img="${team.profilePictureUrl ? `/users/${userId}/teams/images/${userId}/${team.profilePictureUrl.split('/').pop()}` : '/public/img/equipos/default.png'}">${team.name}</option>`
        )
        .join("");

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

  const jugadoresResp = await fetch(jugadoresUrl, {
  });
  const jugadores = await jugadoresResp.json();

  return { jugadores };
}

// Mostrar equipos y jugadores al registrar partido
document
  .getElementById("partidoForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const selectLocal = document.getElementById("equipoLocal");
    const selectVisitante = document.getElementById("equipoVisitante");
    const localId = selectLocal.value;
    const visitanteId = selectVisitante.value;
    const localName = selectLocal.options[selectLocal.selectedIndex].text;
    const visitanteName =
      selectVisitante.options[selectVisitante.selectedIndex].text;
    const resumenDiv = document.getElementById("partidoResumen");

    if (!localId || !visitanteId || localId === visitanteId) {
      resumenDiv.innerHTML =
        "<p style='color:red'>Selecciona dos equipos diferentes.</p>";
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

    resumenDiv.innerHTML = "<p>Cargando equipos...</p>";
    try {
      const [local, visitante] = await Promise.all([
        getTeamWithPlayers(localId),
        getTeamWithPlayers(visitanteId),
      ]);

      const localJugadores = ordenarJugadores(local.jugadores);
      const visitanteJugadores = ordenarJugadores(visitante.jugadores);

      function lanzadoresCheckboxes(jugadores, equipo, tipo, label, color) {
        const jugadoresSinPortero = jugadores.filter(
          (j) => j.position !== "GOALKEEPER"
        );
        return `
                <div class="lanzadores-box" style="flex:1;min-width:220px;">
                    <div style="font-weight:bold;color:${color};margin-bottom:0.5rem;">
                        ${label} <span style="font-size:0.9em;color:#888;">(marca uno o varios)</span>
                    </div>
                    <div style="display:flex;flex-direction:column;gap:0.4rem;">
                        ${jugadoresSinPortero
                          .map(
                            (j) =>
                              `<label style="margin-right:1em;">
        <input type="checkbox" name="${equipo}_${tipo}" value="${j.id}" data-nombre="${j.name}">
        ${j.name}
    </label>`
                          )
                          .join("")}
                    </div>
                </div>
            `;
      }

      const localOption = selectLocal.options[selectLocal.selectedIndex];
      const visitanteOption = selectVisitante.options[selectVisitante.selectedIndex];
      const localImg = localOption.getAttribute("data-img") || "/public/img/equipos/default.png";
      const visitanteImg = visitanteOption.getAttribute("data-img") || "/public/img/equipos/default.png";

      resumenDiv.innerHTML = `
            <div class="plantilla-box" style="background:#f8fbff;border-radius:12px;padding:2rem 1rem;margin-bottom:2rem;box-shadow:0 2px 10px #005bb51a;">
                <h3 style="color:#005bb5;text-align:center;margin-bottom:1.2rem;display:flex;flex-direction:column;align-items:center;gap:0.5rem;">
                  <img src="${localImg}" alt="${localName}" style="height:120px;width:120px;vertical-align:middle;border-radius:50%;margin:0 auto 0.5rem auto;display:block;" onerror="this.onerror=null;this.src='/public/img/equipos/default.png';">
                  <span>${localName}</span>
                </h3>
                <table class="jugadores-table">
                    <thead>
                        <tr>
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
                            : localJugadores
                                .map(
                                  (j) => `
                                <tr>
                                    <td>${j.name ?? "-"}</td>
                                    <td>${j.skill ?? "-"}</td>
                                    <td>${traducirPosicion(j.position ?? "-")}</td>
                                    <td>${j.hasPlayed ? "Sí" : "No"}</td>
                                </tr>
                            `
                                )
                                .join("")
                        }
                    </tbody>
                </table>
                <div style="display:flex;gap:2rem;justify-content:center;margin-top:1.5rem;flex-wrap:wrap;">
                    ${lanzadoresCheckboxes(
                      localJugadores,
                      "local",
                      "faltas",
                      "Lanzadores de faltas",
                      "#1e88e5"
                    )}
                    ${lanzadoresCheckboxes(
                      localJugadores,
                      "local",
                      "corners",
                      "Lanzadores de córners",
                      "#43a047"
                    )}
                    ${lanzadoresCheckboxes(
                      localJugadores,
                      "local",
                      "penalty",
                      "Lanzadores de penaltis",
                      "#e53935"
                    )}
                </div>
            </div>
            <div class="plantilla-box" style="background:#f8fbff;border-radius:12px;padding:2rem 1rem;margin-bottom:2rem;box-shadow:0 2px 10px #005bb51a;">
                <h3 style="color:#005bb5;text-align:center;margin-bottom:1.2rem;display:flex;flex-direction:column;align-items:center;gap:0.5rem;">
                  <img src="${visitanteImg}" alt="${visitanteName}" style="height:120px;width:120px;vertical-align:middle;border-radius:50%;margin:0 auto 0.5rem auto;display:block;" onerror="this.onerror=null;this.src='/public/img/equipos/default.png';">
                  <span>${visitanteName}</span>
                </h3>
                <table class="jugadores-table">
                    <thead>
                        <tr>
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
                            : visitanteJugadores
                                .map(
                                  (j) => `
                                <tr>
                                    <td>${j.name ?? "-"}</td>
                                    <td>${j.skill ?? "-"}</td>
                                    <td>${traducirPosicion(j.position ?? "-")}</td>
                                    <td>${j.hasPlayed ? "Sí" : "No"}</td>
                                </tr>
                            `
                                )
                                .join("")
                        }
                    </tbody>
                </table>
                <div style="display:flex;gap:2rem;justify-content:center;margin-top:1.5rem;flex-wrap:wrap;">
                    ${lanzadoresCheckboxes(
                      visitanteJugadores,
                      "visitante",
                      "faltas",
                      "Lanzadores de faltas",
                      "#1e88e5"
                    )}
                    ${lanzadoresCheckboxes(
                      visitanteJugadores,
                      "visitante",
                      "corners",
                      "Lanzadores de córners",
                      "#43a047"
                    )}
                    ${lanzadoresCheckboxes(
                      visitanteJugadores,
                      "visitante",
                      "penalty",
                      "Lanzadores de penaltis",
                      "#e53935"
                    )}
                </div>
            </div>
            <div style="text-align:center; margin-top:2rem;">
                <button id="simularBtn" style="background:#005bb5;color:#fff;padding:0.7rem 2rem;border:none;border-radius:20px;font-size:1.1rem;cursor:pointer;">Simular Partido</button>
                <div id="resultadoSimulacion" style="margin-top:1.5rem;"></div>
            </div>
        `;

      // Añadir evento al botón de simular
      document.getElementById("simularBtn").onclick = async function () {
        const resultadoDiv = document.getElementById("resultadoSimulacion");
        resultadoDiv.innerHTML = "<p>Simulando partido...</p>";

        function getCheckedNames(name) {
          return Array.from(
            document.querySelectorAll(`input[name="${name}"]:checked`)
          ).map((cb) => cb.dataset.nombre);
        }
        const lanzadores = {
          local_faltas: getCheckedNames("local_faltas"),
          local_corners: getCheckedNames("local_corners"),
          local_penalty: getCheckedNames("local_penalty"),
          visitante_faltas: getCheckedNames("visitante_faltas"),
          visitante_corners: getCheckedNames("visitante_corners"),
          visitante_penalty: getCheckedNames("visitante_penalty"),
        };

        const body = {
          teamAName: localName,
          teamBName: visitanteName,
          ...lanzadores,
        };

        try {
          const url = `http://localhost:8080/users/${userId}/teams/simulate`;
          const response = await fetch(url, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(body),
          });
          if (!response.ok) {
            const errorText = await response.text();
            resultadoDiv.innerHTML = `<p style="color:red">Error: ${errorText}</p>`;
            return;
          }
          const resultado = await response.text();

          const eventos = resultado.split("<br>");
          const resultadoFinal = eventos.pop(); 

          resultadoDiv.innerHTML = `
                    <div class="resultado-partido-box">
                        <div class="equipos-marcador">
                            <div class="equipo-nombre">
                                <img src="${localImg}" 
                                     alt="${localName}" 
                                     style="height:100px;width:100px;border-radius:50%;"
                                     onerror="this.onerror=null;this.src='/public/img/equipos/default.png';">
                                <span>${localName}</span>
                            </div>
                            <div class="marcador-central">
                                ${resultadoFinal
                                  .replace(/Resultado final: /, "")
                                  .replace(
                                    localName,
                                    `<span class='marcador-local'>${localName}</span>`
                                  )
                                  .replace(
                                    visitanteName,
                                    `<span class='marcador-visitante'>${visitanteName}</span>`
                                  )}
                            </div>
                            <div class="equipo-nombre">
                                <img src="${visitanteImg}" 
                                     alt="${visitanteName}" 
                                     style="height:100px;width:100px;border-radius:50%;"
                                     onerror="this.onerror=null;this.src='/public/img/equipos/default.png';">
                                <span>${visitanteName}</span>
                            </div>
                        </div>
                        <div class="eventos-timeline">
                            ${eventos
                              .map(
                                (ev) => `<div class="evento-linea">${ev}</div>`
                              )
                              .join("")}
                        </div>
                    </div>
                `;
        } catch (err) {
          resultadoDiv.innerHTML =
            "<p style='color:red'>Error al simular el partido.</p>";
        }
      };
    } catch (err) {
      resumenDiv.innerHTML =
        "<p style='color:red'>Error al cargar los equipos o jugadores.</p>";
    }
  });

function setTeamImage(selectId, imgDivId, imgTagId, nombreSpanId) {
  const select = document.getElementById(selectId);
  const imgDiv = document.getElementById(imgDivId);
  const imgTag = document.getElementById(imgTagId);
  const nombreSpan = document.getElementById(nombreSpanId);

  function updateImageAndName() {
    const selectedOption = select.options[select.selectedIndex];
    const teamName = selectedOption?.text || "";
    const imgSrc = selectedOption?.getAttribute("data-img") || "/public/img/equipos/default.png";
    if (
      teamName.toLowerCase().includes("selecciona equipo") ||
      teamName.trim() === ""
    ) {
      imgTag.style.display = "none"; 
      imgDiv.style.background = "transparent"; 
      imgDiv.style.border = "none"; 
      nombreSpan.textContent = "";
      return;
    }
    imgTag.style.display = "block"; 
    imgDiv.style.background = "#e0e7ef"; 
    imgDiv.style.border = "2px solid #005bb5";
    imgTag.src = imgSrc;
    imgTag.alt = teamName;
    imgTag.onerror = function () {
      this.onerror = null;
      this.src = "/public/img/equipos/default.png";
    };
    nombreSpan.textContent = teamName;
  }

  select.addEventListener("change", updateImageAndName);
  updateImageAndName(); 
}

function traducirPosicion(pos) {
    switch (pos) {
        case "GOALKEEPER": return "Portero";
        case "DEFENDER": return "Defensa";
        case "MIDFIELDER": return "Mediocentro";
        case "FORWARD": return "Delantero";
        default: return pos;
    }
}

document.addEventListener("DOMContentLoaded", () => {
  loadTeamsForSelects();
  setTeamImage("equipoLocal", "imgLocal", "imgLocalImg", "nombreLocal");
  setTeamImage("equipoVisitante", "imgVisitante", "imgVisitanteImg", "nombreVisitante");

  // Mostrar/ocultar menú lateral
  const toggleBtn = document.getElementById("toggleMenuBtn");
  const menuLateral = document.getElementById("menuLateral");
  if (toggleBtn && menuLateral) {
    toggleBtn.addEventListener("click", () => {
      menuLateral.classList.toggle("oculto");
    });
  }
});
