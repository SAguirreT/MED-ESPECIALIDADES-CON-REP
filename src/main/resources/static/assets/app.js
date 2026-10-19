"use strict";
const $ = (q, root = document) => root.querySelector(q);
const $$ = (q, root = document) => [...root.querySelectorAll(q)];
const esc = (value) =>
  String(value ?? "").replace(
    /[&<>"']/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        c
      ],
  );
const fullName = (p) => (p ? `${p.nombre} ${p.apellido}` : "—");
const initials = (s) =>
  String(s || "AU")
    .split(/\s+/)
    .slice(0, 2)
    .map((x) => x[0])
    .join("")
    .toUpperCase();
const today = () =>
  new Intl.DateTimeFormat("en-CA", {
    timeZone: "America/Lima",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(new Date());
const plusDays = (date, n) => {
  const d = new Date(date + "T12:00:00");
  d.setDate(d.getDate() + n);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
};
const time = (s) => s?.slice(0, 5) || "—";
const dateLabel = (s) =>
  s
    ? new Date(s + "T12:00:00").toLocaleDateString("es-PE", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      })
    : "—";
const tag = (state) => {
  const label =
    typeof state === "boolean" ? (state ? "ACTIVO" : "INACTIVO") : state;
  return `<span class="tag ${esc(String(label).toLowerCase())}">${esc(String(label).replaceAll("_", " "))}</span>`;
};
const person = (name, detail = "") =>
  `<div class="person"><span class="avatar">${esc(initials(name))}</span><div><strong>${esc(name)}</strong>${detail ? `<small>${esc(detail)}</small>` : ""}</div></div>`;
const roleLabel = (u) =>
  u.roles.some((r) => r.name === "ADMIN")
    ? "Administración"
    : u.roles.some((r) => r.name === "MODERATOR")
      ? "Recepción"
      : "Consulta";
let token = sessionStorage.getItem("aurora-token"),
  user = null,
  current = "inicio",
  rows = [],
  tablePage = 0,
  search = "",
  auditPage = 0,
  renderId = 0,
  signup = false;
let catalogs = {},
  editSubmit = null,
  selectedSlot = null,
  slotsVersion = 0,
  toastTimer;
const role = (r) => !!user?.roles.some((x) => x.name === r);
const canWrite = (type) =>
  role("ADMIN") || (role("MODERATOR") && ["pacientes", "citas"].includes(type));
async function api(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };
  const response = await fetch("/api" + path, {
    ...options,
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });
  const result = await response
    .json()
    .catch(() => ({ message: "Respuesta inesperada del servidor" }));
  if (!response.ok) {
    if (response.status === 401 && user) {
      logoutLocal();
      toast("La sesión terminó. Vuelve a ingresar.", true);
    }
    const fields = result.fields
      ? Object.entries(result.fields)
          .map(([k, v]) => `${k}: ${v}`)
          .join("\n")
      : "";
    throw new Error(
      [result.message || `Error ${response.status}`, fields]
        .filter(Boolean)
        .join("\n"),
    );
  }
  return result;
}
function toast(message, error = false) {
  clearTimeout(toastTimer);
  $("#toast").textContent = message;
  $("#toast").className = "toast" + (error ? " error" : "");
  $("#toast").hidden = false;
  toastTimer = setTimeout(() => ($("#toast").hidden = true), 5000);
}
function logoutLocal() {
  token = null;
  user = null;
  sessionStorage.removeItem("aurora-token");
  $("#editor").close();
  $("#app-view").hidden = true;
  $("#login-view").hidden = false;
  $("#login-form").reset();
}
async function loadCatalogs() {
  const keys = [
    "medicos",
    "especialidades",
    "consultorios",
    "pacientes",
    "horarios",
  ];
  const values = await Promise.all(keys.map((k) => api("/" + k)));
  catalogs = Object.fromEntries(keys.map((k, i) => [k, values[i]]));
}
const nav = [
  ["inicio", "Resumen", "◫"],
  ["citas", "Agenda y citas", "▦"],
  ["pacientes", "Pacientes", "♧"],
  ["medicos", "Equipo médico", "♙"],
  ["especialidades", "Especialidades", "✳"],
  ["consultorios", "Consultorios", "▤"],
  ["horarios", "Horarios", "◷"],
  ["ausencias", "Bloqueos y permisos", "⊘"],
  ["reportes", "Reportes", "▥"],
  ["usuarios", "Usuarios y roles", "♧"],
  ["auditoria", "Auditoría", "≡"],
  ["seguridad", "Laboratorio JWT", "◇"],
];
async function enter() {
  user = await api("/auth/me");
  $("#login-view").hidden = true;
  $("#app-view").hidden = false;
  $("#user-name").textContent = user.username;
  $("#user-role").textContent = roleLabel(user);
  $("#user-initials").textContent = initials(user.username);
  $("#today-label").textContent = new Intl.DateTimeFormat("es-PE", {
    timeZone: "America/Lima",
    day: "numeric",
    month: "long",
    year: "numeric",
  }).format(new Date());
  $("#navigation").innerHTML = nav
    .filter(([k]) => !["usuarios", "auditoria"].includes(k) || role("ADMIN"))
    .map(
      ([k, n, i], index) =>
        `${index === 9 ? '<div class="nav-divider"></div>' : ""}<a href="#${k}" data-nav="${k}"><span class="nav-icon">${i}</span>${n}</a>`,
    )
    .join("");
  await loadCatalogs();
  await render();
}
function heading(title, subtitle, action = "") {
  return `<div class="page-heading"><div><span class="eyebrow">TU CLÍNICA, EN ORDEN</span><h1>${title}</h1><p>${subtitle}</p></div>${action}</div>`;
}
function addButton(type, label = "Nuevo registro") {
  return canWrite(type)
    ? `<button class="primary" data-action="new" data-type="${type}">＋ ${label}</button>`
    : "";
}
const empty = (text) =>
  `<div class="empty"><strong>Todavía no hay registros</strong>${esc(text)}</div>`;
const fields = {
  pacientes: [
    ["nombre", "Nombres", "text", true, 100],
    ["apellido", "Apellidos", "text", true, 100],
    ["dni", "DNI", "text", true, 8],
    ["fechaNacimiento", "Fecha de nacimiento", "date", true],
    ["telefono", "Teléfono", "tel", false, 20],
    ["email", "Correo electrónico", "email", false, 100],
  ],
  medicos: [
    ["nombre", "Nombres", "text", true, 100],
    ["apellido", "Apellidos", "text", true, 100],
    ["cmp", "CMP", "text", true, 20],
    ["telefono", "Teléfono", "tel", false, 20],
    ["email", "Correo electrónico", "email", false, 100],
    ["especialidadIds", "Especialidades", "multi", true],
  ],
  especialidades: [
    ["nombre", "Nombre", "text", true, 100],
    ["descripcion", "Descripción", "textarea", false, 255],
  ],
  consultorios: [
    ["codigo", "Código", "text", true, 20],
    ["nombre", "Nombre", "text", true, 100],
    ["piso", "Piso", "text", false, 20],
    ["area", "Área", "text", false, 100],
  ],
  horarios: [
    ["medicoId", "Profesional", "select", true],
    ["consultorioId", "Consultorio", "select", true],
    ["diaSemana", "Día de atención", "select", true],
    ["duracionCitaMinutos", "Duración de cada cita (min)", "number", true],
    ["horaInicio", "Inicio del turno", "time", true],
    ["horaFin", "Fin del turno", "time", true],
  ],
  bloqueos: [
    ["medicoId", "Profesional", "select", true],
    ["fecha", "Fecha", "date", true],
    ["horaInicio", "Desde", "time", true],
    ["horaFin", "Hasta", "time", true],
    ["motivo", "Motivo", "textarea", true, 255],
  ],
  vacaciones: [
    ["medicoId", "Profesional", "select", true],
    ["fechaInicio", "Inicio de ausencia", "date", true],
    ["fechaFin", "Fin de ausencia", "date", true],
    ["motivo", "Motivo", "textarea", true, 255],
  ],
  usuarios: [
    ["username", "Nombre de usuario", "text", true, 60],
    ["email", "Correo electrónico", "email", true, 100],
    ["password", "Contraseña inicial", "password", true, 64],
    ["roles", "Permiso de acceso", "select", true],
  ],
};
const configs = {
  pacientes: {
    title: "Pacientes",
    subtitle: "Un registro claro para una atención cercana.",
    singular: "paciente",
    cols: ["Paciente", "DNI", "Nacimiento", "Contacto", "Estado"],
    row: (p) => [
      person(fullName(p)),
      esc(p.dni),
      dateLabel(p.fechaNacimiento),
      `${esc(p.telefono || "—")}<small>${esc(p.email)}</small>`,
      tag(p.estado),
    ],
  },
  medicos: {
    title: "Equipo médico",
    subtitle: "Profesionales y especialidades que cuidan a tus pacientes.",
    singular: "médico",
    cols: ["Profesional", "CMP", "Especialidades", "Contacto", "Estado"],
    row: (m) => [
      person(fullName(m)),
      esc(m.cmp),
      m.especialidades.map((e) => esc(e.nombre)).join(", ") || "Sin asignar",
      `${esc(m.telefono || "—")}<small>${esc(m.email)}</small>`,
      tag(m.estado),
    ],
  },
  especialidades: {
    title: "Especialidades",
    subtitle: "Organiza los servicios disponibles en tu clínica.",
    singular: "especialidad",
    cols: ["Especialidad", "Descripción", "Estado"],
    row: (e) => [
      `<strong>${esc(e.nombre)}</strong>`,
      esc(e.descripcion || "—"),
      tag(e.estado),
    ],
  },
  consultorios: {
    title: "Consultorios",
    subtitle: "Espacios de atención y asignación de recursos.",
    singular: "consultorio",
    cols: ["Código", "Consultorio", "Piso", "Área", "Estado"],
    row: (c) => [
      `<strong>${esc(c.codigo)}</strong>`,
      esc(c.nombre),
      esc(c.piso || "—"),
      esc(c.area || "—"),
      tag(c.estado),
    ],
  },
  horarios: {
    title: "Horarios de atención",
    subtitle: "Turnos semanales sin cruces entre médicos ni consultorios.",
    singular: "horario",
    cols: ["Profesional", "Día", "Turno", "Duración", "Consultorio", "Estado"],
    row: (h) => [
      person(fullName(h.medico)),
      esc(h.diaSemana),
      `${time(h.horaInicio)} – ${time(h.horaFin)}`,
      `${h.duracionCitaMinutos} min`,
      esc(h.consultorio?.codigo || "Sin asignar"),
      tag(h.estado),
    ],
  },
  usuarios: {
    title: "Usuarios y roles",
    subtitle: "Accesos personales, permisos claros y sesiones revocables.",
    singular: "usuario",
    cols: ["Usuario", "Correo", "Rol", "Estado"],
    row: (u) => [
      person(u.username),
      esc(u.email),
      u.roles.map((r) => esc(r.name)).join(", "),
      tag(u.activo),
    ],
  },
};
function actions(type, r) {
  if (!canWrite(type)) return "";
  if (type === "usuarios")
    return `<button data-action="access" data-id="${r.id}">Permisos</button>`;
  return `<button data-action="edit" data-type="${type}" data-id="${r.id}">Editar</button><button data-action="state" data-type="${type}" data-id="${r.id}">${r.estado === false || r.estado === "INACTIVO" ? "Activar" : "Desactivar"}</button>`;
}
function tableHtml(cols, body) {
  return `<div class="table-wrap"><table><thead><tr>${cols.map((c) => `<th>${c}</th>`).join("")}</tr></thead><tbody>${body}</tbody></table></div>`;
}
function paintTable(type) {
  const cfg = configs[type];
  const filtered = rows.filter((r) =>
    JSON.stringify(r)
      .toLocaleLowerCase("es")
      .includes(search.toLocaleLowerCase("es")),
  );
  const maxPage = Math.max(0, Math.ceil(filtered.length / 8) - 1);
  tablePage = Math.min(tablePage, maxPage);
  const page = filtered.slice(tablePage * 8, tablePage * 8 + 8);
  $("#table-body").innerHTML = page.length
    ? tableHtml(
        [...cfg.cols, ...(canWrite(type) ? ["Acciones"] : [])],
        page
          .map(
            (r) =>
              `<tr>${cfg
                .row(r)
                .map((c) => `<td>${c}</td>`)
                .join(
                  "",
                )}${canWrite(type) ? `<td><div class="row-actions">${actions(type, r)}</div></td>` : ""}</tr>`,
          )
          .join(""),
      )
    : empty(
        search
          ? "Prueba con otro término de búsqueda."
          : "Crea el primer registro para comenzar.",
      );
  $("#result-count").textContent =
    `${filtered.length} registros · Página ${tablePage + 1} de ${maxPage + 1}`;
  $("#prev-page").disabled = tablePage === 0;
  $("#next-page").disabled = tablePage === maxPage;
}
async function directory(type, id) {
  rows = await api("/" + type);
  if (id !== renderId) return;
  const cfg = configs[type];
  $("#content").innerHTML =
    heading(
      cfg.title,
      cfg.subtitle,
      addButton(type, "Añadir " + cfg.singular),
    ) +
    `<section class="card"><div class="toolbar"><div class="searchbox"><span>⌕</span><input id="table-search" type="search" aria-label="Buscar registros" placeholder="Buscar en ${cfg.title.toLowerCase()}…" value="${esc(search)}"></div><span class="subtitle-small">${rows.length} registros en tu clínica</span></div><div id="table-body"></div><div class="results-footer"><span id="result-count"></span><div class="pagination"><button id="prev-page" aria-label="Página anterior">←</button><button id="next-page" aria-label="Página siguiente">→</button></div></div></section>`;
  paintTable(type);
  $("#table-search").oninput = (e) => {
    search = e.target.value;
    tablePage = 0;
    paintTable(type);
  };
  $("#prev-page").onclick = () => {
    tablePage--;
    paintTable(type);
  };
  $("#next-page").onclick = () => {
    tablePage++;
    paintTable(type);
  };
}
function appointmentTable(data, withActions = true) {
  return data.length
    ? tableHtml(
        [
          "Hora",
          "Paciente",
          "Profesional",
          "Especialidad",
          "Estado",
          ...(withActions && canWrite("citas") ? ["Acciones"] : []),
        ],
        data
          .map(
            (c) =>
              `<tr><td><strong>${time(c.horaInicio)}</strong><small>${dateLabel(c.fecha)}</small></td><td>${person(fullName(c.paciente), "DNI " + c.paciente.dni)}</td><td>${esc(fullName(c.medico))}<small>${esc(c.consultorio.codigo)}</small></td><td>${esc(c.especialidad.nombre)}</td><td>${tag(c.estado)}</td>${withActions && canWrite("citas") ? `<td><div class="row-actions">${c.estado === "CONFIRMADA" ? `<button data-action="reschedule" data-id="${c.id}">Reprogramar</button><button data-action="complete" data-id="${c.id}">Atender</button><button class="danger" data-action="cancel-cita" data-id="${c.id}">Cancelar</button>` : "—"}</div></td>` : ""}</tr>`,
          )
          .join(""),
      )
    : empty(
        "No hay citas en este período. Puedes consultar otra fecha o agendar una nueva atención.",
      );
}
async function dashboard(id) {
  const d = await api("/dashboard");
  if (id !== renderId) return;
  rows = d.agenda;
  const name = user.username.split(" ")[0];
  $("#content").innerHTML =
    heading(
      `Todo listo para cuidar, ${esc(name)}.`,
      "Una mirada a la actividad de tu clínica.",
      addButton("citas", "Agendar cita"),
    ) +
    `<section class="hero"><div><h2>Cada cita es una oportunidad<br>para cuidar mejor.</h2><p>Organiza tu día y acompaña a tu equipo en cada atención.</p></div><div class="hero-badge"><span class="round-icon">◷</span><div><strong>${d.pendientesHoy}</strong><small>citas pendientes hoy</small></div></div><span class="hero-flower">✳</span></section><section class="metrics">${[
      [d.pacientes, "Pacientes registrados", "♧", "Tu comunidad de pacientes"],
      [d.medicosActivos, "Médicos activos", "♙", "Profesionales disponibles"],
      [d.citasHoy, "Citas para hoy", "▦", "Agenda del día"],
      [d.pendientesHoy, "Atenciones pendientes", "◷", "Por completar hoy"],
    ]
      .map(
        ([n, label, icon, note]) =>
          `<article class="metric"><div class="metric-top">${label}<span class="metric-icon">${icon}</span></div><strong>${String(n).padStart(2, "0")}</strong><small>${note}</small></article>`,
      )
      .join(
        "",
      )}</section><div class="dashboard-grid"><section class="card"><div class="card-header"><div><h2>Agenda de hoy</h2><p>${dateLabel(today())} · Atención ambulatoria</p></div><a href="#citas">Ver agenda →</a></div>${appointmentTable(d.agenda.slice(0, 5), false)}<div class="results-footer"><span>${d.agenda.length} citas programadas hoy</span><span>Horarios de Lima</span></div></section><section class="card"><div class="card-header"><div><h2>Nuestras especialidades</h2><p>Distribución del equipo médico</p></div><span>✳</span></div><div class="specialty-list">${
      d.especialidades
        .slice(0, 5)
        .map(
          (e) =>
            `<div class="specialty-item"><div class="specialty-row"><span>${esc(e.especialidad)}</span><strong>${e.cantidadMedicos}</strong></div><div class="bar"><span data-bar="${Math.round((100 * e.cantidadMedicos) / Math.max(1, d.medicosActivos))}"></span></div></div>`,
        )
        .join("") || '<p class="muted">Aún no hay especialidades.</p>'
    }</div><div class="quick-links"><a href="#medicos">Conocer al equipo <span>→</span></a><a href="#reportes">Consultar reportes <span>→</span></a></div></section></div>`;
  $$("[data-bar]").forEach((el) => (el.style.width = el.dataset.bar + "%"));
}
let agendaFrom = today(),
  agendaTo = today();
async function agenda(id) {
  rows = await api(`/citas?desde=${agendaFrom}&hasta=${agendaTo}`);
  if (id !== renderId) return;
  $("#content").innerHTML =
    heading(
      "Agenda y citas",
      "Cada paciente, en el momento y lugar indicados.",
      addButton("citas", "Agendar cita"),
    ) +
    `<section class="card"><div class="toolbar"><div class="filters"><label>Desde<input id="agenda-from" type="date" value="${agendaFrom}"></label><label>Hasta<input id="agenda-to" type="date" value="${agendaTo}"></label><button class="secondary" id="apply-dates">Consultar</button><button class="text-button" id="tomorrow">Mañana</button></div><select id="appointment-state" aria-label="Filtrar por estado"><option value="">Todos los estados</option><option>CONFIRMADA</option><option>ATENDIDA</option><option>CANCELADA</option></select></div><div id="agenda-table">${appointmentTable(rows)}</div><div class="results-footer"><span>${rows.length} citas en el período</span><span>Los cupos se validan al confirmar la reserva</span></div></section>`;
  $("#apply-dates").onclick = () => {
    agendaFrom = $("#agenda-from").value;
    agendaTo = $("#agenda-to").value;
    if (!agendaFrom || !agendaTo || agendaTo < agendaFrom)
      return toast("Selecciona un rango válido", true);
    render();
  };
  $("#tomorrow").onclick = () => {
    agendaFrom = agendaTo = plusDays(today(), 1);
    render();
  };
  $("#appointment-state").onchange = (e) =>
    ($("#agenda-table").innerHTML = appointmentTable(
      rows.filter((c) => !e.target.value || c.estado === e.target.value),
    ));
}
function optionsFor(name, value) {
  let opts = [];
  switch (name) {
    case "medicoId":
      opts = (catalogs.medicos || [])
        .filter((m) => m.estado || m.id === Number(value))
        .map((m) => [m.id, fullName(m)]);
      break;
    case "consultorioId":
      opts = (catalogs.consultorios || [])
        .filter((c) => c.estado === "DISPONIBLE" || c.id === Number(value))
        .map((c) => [c.id, c.codigo + " · " + c.nombre]);
      break;
    case "pacienteId":
      opts = (catalogs.pacientes || [])
        .filter((p) => p.estado)
        .map((p) => [p.id, fullName(p) + " · " + p.dni]);
      break;
    case "especialidadIds":
      opts = (catalogs.especialidades || [])
        .filter((e) => e.estado)
        .map((e) => [e.id, e.nombre]);
      break;
    case "diaSemana":
      opts = [
        "LUNES",
        "MARTES",
        "MIERCOLES",
        "JUEVES",
        "VIERNES",
        "SABADO",
        "DOMINGO",
      ].map((d) => [d, d]);
      break;
    case "roles":
      opts = [
        ["ADMIN", "ADMIN · Administración"],
        ["MODERATOR", "MODERATOR · Recepción"],
        ["USER", "USER · Consulta"],
      ];
      break;
  }
  return opts
    .map(
      ([key, label]) =>
        `<option value="${esc(key)}" ${Array.isArray(value) ? (value.map(String).includes(String(key)) ? "selected" : "") : String(value) === String(key) ? "selected" : ""}>${esc(label)}</option>`,
    )
    .join("");
}
function fieldHtml([name, label, type, required, max], value = "") {
  const attrs = `name="${name}" id="field-${name}" ${required ? "required" : ""} ${max ? `maxlength="${max}"` : ""}`;
  let input;
  if (type === "select" || type === "multi")
    input = `<select ${attrs} ${type === "multi" ? "multiple" : ""}>${type === "select" ? '<option value="">Selecciona una opción</option>' : ""}${optionsFor(name, value)}</select>${type === "multi" ? '<span class="field-help">Usa Ctrl o ⌘ para seleccionar varias especialidades.</span>' : ""}`;
  else if (type === "textarea")
    input = `<textarea ${attrs}>${esc(value)}</textarea>`;
  else
    input = `<input ${attrs} type="${type}" value="${esc(value)}" ${name === "dni" ? 'pattern="[0-9]{8}" inputmode="numeric"' : ""} ${name === "duracionCitaMinutos" ? 'min="5" max="240" step="1"' : ""} ${name === "password" ? 'minlength="10" autocomplete="new-password"' : ""} ${name === "fechaNacimiento" ? `max="${today()}"` : ""}>`;
  return `<label class="${type === "textarea" || type === "multi" ? "full" : ""}">${label}${required ? " *" : ""}${input}</label>`;
}
function openDialog(title, html, submit) {
  $("#editor-title").textContent = title;
  $("#editor-fields").innerHTML = html;
  $("#editor-error").textContent = "";
  $("#save-editor").disabled = false;
  $("#save-editor").textContent = "Guardar cambios";
  editSubmit = submit;
  $("#editor").showModal();
}
async function entityEditor(type, row) {
  await loadCatalogs();
  const data = { ...row };
  if (type === "medicos")
    data.especialidadIds = row?.especialidades.map((e) => e.id) || [];
  if (type === "horarios") {
    data.medicoId = row?.medico.id;
    data.consultorioId = row?.consultorio?.id;
    data.duracionCitaMinutos = row?.duracionCitaMinutos || 30;
  }
  const title =
    (row ? "Editar " : "Añadir ") +
    (configs[type]?.singular ||
      { bloqueos: "bloqueo", vacaciones: "ausencia" }[type]);
  openDialog(
    title,
    fields[type].map((f) => fieldHtml(f, data[f[0]] ?? "")).join(""),
    async (form) => {
      let body = Object.fromEntries(new FormData(form));
      if (type === "medicos")
        body.especialidadIds = $$("#field-especialidadIds option:checked").map(
          (o) => Number(o.value),
        );
      for (const key of ["medicoId", "consultorioId", "duracionCitaMinutos"])
        if (body[key]) body[key] = Number(body[key]);
      if (type === "usuarios") body.roles = [body.roles];
      let path = "/" + type + (row ? "/" + row.id : "");
      if (type === "horarios") {
        path = row
          ? "/horarios/" + row.id
          : "/horarios/medico/" + body.medicoId;
        delete body.medicoId;
      }
      if (type === "bloqueos") {
        path = "/horarios/medico/" + body.medicoId;
        path += "/bloqueos";
        delete body.medicoId;
      }
      await api(path, { method: row ? "PUT" : "POST", body });
    },
  );
  if (type === "horarios" && row) {
    $("#field-medicoId").disabled = true;
  }
}
async function appointmentEditor(row) {
  await loadCatalogs();
  selectedSlot = null;
  openDialog(
    row ? "Reprogramar cita" : "Agendar una cita",
    fieldHtml(["pacienteId", "Paciente", "select", true], row?.paciente.id) +
      fieldHtml(["medicoId", "Profesional", "select", true], row?.medico.id) +
      `<label>Especialidad *<select id="cita-specialty" name="especialidadId" required></select></label>` +
      fieldHtml(
        ["fecha", "Fecha de atención", "date", true],
        row?.fecha || plusDays(today(), 1),
      ) +
      fieldHtml(
        ["motivo", "Motivo de consulta", "textarea", true, 255],
        row?.motivo || "",
      ) +
      `<div class="full"><label>Selecciona una franja disponible *</label><div id="slot-list" class="slots"></div><p class="slot-legend">Las horas tachadas no están disponibles. La reserva se comprueba nuevamente al guardar.</p></div>`,
    async (form) => {
      if (!selectedSlot) throw new Error("Selecciona una hora disponible");
      const body = Object.fromEntries(new FormData(form));
      for (const k of ["pacienteId", "medicoId", "especialidadId"])
        body[k] = Number(body[k]);
      body.horarioId = selectedSlot.horarioId;
      body.horaInicio = selectedSlot.horaInicio;
      await api("/citas" + (row ? "/" + row.id : ""), {
        method: row ? "PUT" : "POST",
        body,
      });
      agendaFrom = agendaTo = body.fecha;
      history.replaceState(null, "", "#citas");
    },
  );
  $("#save-editor").textContent = row ? "Confirmar cambio" : "Confirmar cita";
  $("#field-fecha").min = today();
  let currentSlots = [];
  async function refreshSlots() {
    const version = ++slotsVersion;
    selectedSlot = null;
    const mid = $("#field-medicoId").value;
    const date = $("#field-fecha").value;
    const doctor = catalogs.medicos.find((m) => String(m.id) === mid);
    $("#cita-specialty").innerHTML =
      '<option value="">Selecciona una especialidad</option>' +
      (doctor?.especialidades
        .filter((e) => e.estado)
        .map(
          (e) =>
            `<option value="${e.id}" ${row?.especialidad.id === e.id ? "selected" : ""}>${esc(e.nombre)}</option>`,
        )
        .join("") || "");
    if (doctor?.especialidades.filter((e) => e.estado).length === 1)
      $("#cita-specialty").selectedIndex = 1;
    $("#slot-list").innerHTML =
      '<span class="muted">Selecciona médico y fecha.</span>';
    if (!mid || !date) return;
    try {
      currentSlots = await api(`/disponibilidad/medico/${mid}?fecha=${date}`);
      if (version !== slotsVersion || !$("#editor").open) return;
      $("#slot-list").innerHTML =
        currentSlots
          .map(
            (s, i) =>
              `<button type="button" class="slot" data-slot="${i}" ${!s.disponible ? "disabled" : ""} title="${esc(s.motivo || "Consultorio " + s.consultorio)}">${time(s.horaInicio)}</button>`,
          )
          .join("") ||
        '<span class="muted full">Sin horarios activos para este día. Revisa el médico, consultorio o elige otra fecha.</span>';
      $$("[data-slot]").forEach(
        (button) =>
          (button.onclick = () => {
            selectedSlot = currentSlots[Number(button.dataset.slot)];
            $$("[data-slot]").forEach((b) =>
              b.classList.toggle("selected", b === button),
            );
          }),
      );
    } catch (e) {
      $("#editor-error").textContent = e.message;
    }
  }
  $("#field-medicoId").onchange = refreshSlots;
  $("#field-fecha").onchange = refreshSlots;
  await refreshSlots();
}
async function absences(id) {
  const [bs, vs] = await Promise.all([api("/bloqueos"), api("/vacaciones")]);
  if (id !== renderId) return;
  $("#content").innerHTML =
    heading(
      "Bloqueos y permisos",
      "Administra las excepciones de la agenda y conserva su historial.",
      role("ADMIN")
        ? '<div class="filters"><button class="secondary" data-action="new" data-type="bloqueos">＋ Bloqueo puntual</button><button class="primary" data-action="new" data-type="vacaciones">＋ Ausencia</button></div>'
        : "",
    ) +
    `<p class="notice">Si hay citas confirmadas en un intervalo, primero debes reprogramarlas o cancelarlas. Cancelar un bloqueo o permiso libera la agenda.</p><div class="report-grid"><section class="card"><div class="card-header"><h2>Bloqueos puntuales</h2></div>${bs.length ? tableHtml(["Profesional", "Fecha y horas", "Estado", ...(role("ADMIN") ? ["Acción"] : [])], bs.map((b) => `<tr><td>${esc(fullName(b.medico))}<small>${esc(b.motivo)}</small></td><td>${dateLabel(b.fecha)}<small>${time(b.horaInicio)} – ${time(b.horaFin)}</small></td><td>${tag(b.estado)}</td>${role("ADMIN") ? `<td>${b.estado !== "CANCELADO" ? `<button class="text-button" data-action="cancel-absence" data-type="bloqueos" data-id="${b.id}">Liberar</button>` : "—"}</td>` : ""}</tr>`).join("")) : empty("No hay bloqueos registrados.")}</section><section class="card"><div class="card-header"><h2>Vacaciones y permisos</h2></div>${vs.length ? tableHtml(["Profesional", "Período", "Estado", ...(role("ADMIN") ? ["Acción"] : [])], vs.map((v) => `<tr><td>${esc(fullName(v.medico))}<small>${esc(v.motivo)}</small></td><td>${dateLabel(v.fechaInicio)}<small>al ${dateLabel(v.fechaFin)}</small></td><td>${tag(v.estado)}</td>${role("ADMIN") ? `<td>${v.estado !== "CANCELADO" ? `<button class="text-button" data-action="cancel-absence" data-type="vacaciones" data-id="${v.id}">Cancelar</button>` : "—"}</td>` : ""}</tr>`).join("")) : empty("No hay ausencias registradas.")}</section></div>`;
}
async function auditView(id) {
  const data = await api(`/auditoria?page=${auditPage}&size=15`);
  if (id !== renderId) return;
  $("#content").innerHTML =
    heading(
      "Auditoría",
      "La trazabilidad de cada cambio: quién, qué, cuándo y dónde.",
    ) +
    `<p class="notice">Historial de solo lectura. Las acciones exitosas se guardan junto con la operación; contraseñas y tokens nunca se incluyen.</p><section class="card">${tableHtml(["Fecha y hora", "Usuario", "Acción", "Tabla / registro", "Detalle"], data.content.map((a) => `<tr><td>${esc(new Date(a.fechaHora).toLocaleString("es-PE", { timeZone: "America/Lima" }))}</td><td>${esc(a.usuario)}</td><td>${tag(a.accion)}</td><td>${esc(a.tabla)}<small>Registro #${a.registroId ?? "—"}</small></td><td class="audit-detail">${esc(a.detalle)}</td></tr>`).join(""))}<div class="results-footer"><span>${data.totalElements} eventos · Página ${auditPage + 1} de ${Math.max(1, data.totalPages)}</span><div class="pagination"><button id="audit-prev" ${data.first ? "disabled" : ""}>← Anterior</button><button id="audit-next" ${data.last ? "disabled" : ""}>Siguiente →</button></div></div></section>`;
  $("#audit-prev").onclick = () => {
    auditPage--;
    render();
  };
  $("#audit-next").onclick = () => {
    auditPage++;
    render();
  };
}
let reportFrom = today(),
  reportTo = plusDays(today(), 7),
  reportRows = [];
function downloadCsv(filename, data) {
  const cell = (value) => {
    let text = String(value ?? "");
    if (/^[=+\-@\t\r]/.test(text)) text = "'" + text;
    return '"' + text.replaceAll('"', '""') + '"';
  };
  const blob = new Blob(
    ["\ufeff" + data.map((row) => row.map(cell).join(";")).join("\r\n")],
    { type: "text/csv;charset=utf-8" },
  );
  const a = document.createElement("a");
  a.href = URL.createObjectURL(blob);
  a.download = filename;
  a.click();
  setTimeout(() => URL.revokeObjectURL(a.href), 1000);
}
async function reports(id) {
  const [specialties, appointments] = await Promise.all([
    api("/reportes/medicos-por-especialidad"),
    api(`/citas?desde=${reportFrom}&hasta=${reportTo}`),
  ]);
  if (id !== renderId) return;
  reportRows = appointments;
  $("#content").innerHTML =
    heading(
      "Reportes de la clínica",
      "Información útil para organizar el servicio y presentar resultados.",
    ) +
    `<section class="card"><div class="toolbar"><div class="filters"><label>Desde<input id="report-from" type="date" value="${reportFrom}"></label><label>Hasta<input id="report-to" type="date" value="${reportTo}"></label><button class="secondary" id="report-apply">Actualizar</button></div><button class="primary" id="export-citas">↓ Exportar citas CSV</button></div></section><section class="metrics">${["TOTAL", "CONFIRMADA", "ATENDIDA", "CANCELADA"].map((s) => `<article class="metric"><div class="metric-top">${s === "TOTAL" ? "Citas en el período" : s}</div><strong>${s === "TOTAL" ? appointments.length : appointments.filter((c) => c.estado === s).length}</strong><small>${dateLabel(reportFrom)} — ${dateLabel(reportTo)}</small></article>`).join("")}</section><div class="report-grid"><section class="card"><div class="card-header"><div><h2>Médicos por especialidad</h2><p>Se cuentan únicamente médicos activos</p></div><button class="text-button" id="export-specialties">↓ CSV</button></div>${tableHtml(["Especialidad", "Médicos activos", "Estado"], specialties.map((e) => `<tr><td>${esc(e.especialidad)}</td><td>${e.cantidadMedicos}</td><td>${tag(e.activa)}</td></tr>`).join(""))}</section><section class="card"><div class="card-header"><div><h2>Actividad por profesional</h2><p>Citas en el período seleccionado</p></div></div>${tableHtml(["Profesional", "Confirmadas", "Atendidas", "Canceladas"], catalogs.medicos.map((m) => `<tr><td>${esc(fullName(m))}</td>${["CONFIRMADA", "ATENDIDA", "CANCELADA"].map((s) => `<td>${appointments.filter((c) => c.medico.id === m.id && c.estado === s).length}</td>`).join("")}</tr>`).join(""))}</section></div>`;
  $("#report-apply").onclick = () => {
    reportFrom = $("#report-from").value;
    reportTo = $("#report-to").value;
    render();
  };
  $("#export-citas").onclick = () =>
    downloadCsv("aurora-citas.csv", [
      [
        "Fecha",
        "Inicio",
        "Fin",
        "Paciente",
        "Médico",
        "Especialidad",
        "Consultorio",
        "Estado",
      ],
      ...reportRows.map((c) => [
        c.fecha,
        c.horaInicio,
        c.horaFin,
        fullName(c.paciente),
        fullName(c.medico),
        c.especialidad.nombre,
        c.consultorio.codigo,
        c.estado,
      ]),
    ]);
  $("#export-specialties").onclick = () =>
    downloadCsv("aurora-especialidades.csv", [
      ["Especialidad", "Médicos activos", "Activa"],
      ...specialties.map((e) => [
        e.especialidad,
        e.cantidadMedicos,
        e.activa ? "Sí" : "No",
      ]),
    ]);
}
function securityView() {
  const cases = [
    [
      "Ruta pública",
      "/api/test/all",
      "Sin autenticación. Debe responder 200.",
      "none",
    ],
    [
      "Acceso sin token",
      "/api/test/user",
      "Una ruta protegida debe responder 401.",
      "none",
    ],
    [
      "Token alterado",
      "/api/test/user",
      "La firma no es válida: debe responder 401.",
      "invalid",
    ],
    [
      "Token vigente",
      "/api/test/user",
      "Con tu sesión actual: debe responder 200.",
      "current",
    ],
    [
      "ADMIN sin permisos",
      "/api/test/admin",
      "Ejecuta este caso con la cuenta Consulta (USER): debe responder 403. Con ADMIN responderá 200.",
      "current",
    ],
    [
      "ADMIN autorizado",
      "/api/test/admin",
      "Ejecuta este caso con Administración (ADMIN): debe responder 200. Con USER responderá 403.",
      "current",
    ],
  ];
  $("#content").innerHTML =
    heading(
      "Laboratorio de seguridad JWT",
      "Pruebas reales contra la API para comprender autenticación y autorización.",
    ) +
    `<p class="notice">Sesión actual: <strong>${esc(roleLabel(user))}</strong>. La contraseña se verifica con BCrypt y cada solicitud protegida valida la firma, vencimiento y vigencia del usuario. Los resultados siguientes provienen del servidor.</p><div class="security-grid">${cases.map(([name, path, note, mode], i) => `<article class="card security-card"><span class="eyebrow">CASO ${i + 1}</span><h3>${name}</h3><code>GET ${path}</code><p>${note}</p><button class="secondary" data-security="${i}">Ejecutar prueba →</button><pre class="security-result" id="security-${i}" aria-live="polite"></pre></article>`).join("")}</div>`;
  $$("[data-security]").forEach(
    (b) =>
      (b.onclick = async () => {
        const i = Number(b.dataset.security),
          [, path, , mode] = cases[i];
        b.disabled = true;
        try {
          const response = await fetch(path, {
            headers:
              mode === "none"
                ? {}
                : {
                    Authorization:
                      "Bearer " +
                      (mode === "invalid"
                        ? "altered.invalid.signature"
                        : token),
                  },
          });
          $("#security-" + i).textContent =
            `HTTP ${response.status}\n${JSON.stringify(await response.json(), null, 2)}`;
        } catch (e) {
          $("#security-" + i).textContent = e.message;
        } finally {
          b.disabled = false;
        }
      }),
  );
}
async function render() {
  if (!user) return;
  const id = ++renderId;
  current = location.hash.slice(1) || "inicio";
  if (
    !nav.some(([key]) => key === current) ||
    (["usuarios", "auditoria"].includes(current) && !role("ADMIN"))
  )
    current = "inicio";
  $$("[data-nav]").forEach((a) =>
    a.classList.toggle("active", a.dataset.nav === current),
  );
  $("#crumb").textContent = nav.find(([k]) => k === current)[1];
  $("#sidebar").classList.remove("open");
  $("#content").innerHTML =
    '<div class="loading">Preparando tu espacio de trabajo…</div>';
  try {
    if (configs[current]) await directory(current, id);
    else if (current === "inicio") await dashboard(id);
    else if (current === "citas") await agenda(id);
    else if (current === "ausencias") await absences(id);
    else if (current === "reportes") await reports(id);
    else if (current === "auditoria") await auditView(id);
    else securityView();
    $("#connection-state").textContent = "Conectado";
  } catch (e) {
    if (id === renderId) {
      $("#content").innerHTML =
        `<div class="empty"><strong>No se pudo cargar la información</strong>${esc(e.message)}<p><button class="secondary" id="retry">Reintentar</button></p></div>`;
      $("#retry").onclick = render;
      $("#connection-state").textContent = "Revisa la conexión";
    }
  }
}
$("#editor-form").onsubmit = async (event) => {
  event.preventDefault();
  const button = $("#save-editor");
  button.disabled = true;
  $("#editor-error").textContent = "";
  try {
    await editSubmit(event.target);
    $("#editor").close();
    toast("Operación guardada correctamente");
    if (user) {
      await loadCatalogs();
      await render();
    }
  } catch (e) {
    $("#editor-error").textContent = e.message;
  } finally {
    button.disabled = false;
  }
};
$("#close-editor").onclick = $("#cancel-editor").onclick = () => {
  $("#editor").close();
  slotsVersion++;
};
$("#content").onclick = async (event) => {
  const button = event.target.closest("[data-action]");
  if (!button) return;
  const { action, type } = button.dataset;
  const id = Number(button.dataset.id);
  const row = rows.find((r) => r.id === id);
  try {
    if (action === "new")
      return type === "citas"
        ? await appointmentEditor()
        : await entityEditor(type);
    if (action === "edit") return await entityEditor(type, row);
    if (action === "reschedule") return await appointmentEditor(row);
    if (action === "state") {
      const enabled = row.estado === false || row.estado === "INACTIVO";
      openDialog(
        enabled ? "Activar registro" : "Desactivar registro",
        `<p class="full muted">${enabled ? "El registro volverá a estar disponible." : "El registro se conservará en el historial. No se permite desactivarlo si tiene citas futuras confirmadas."}</p>`,
        () =>
          api(`/${type}/${id}/estado?estado=${enabled}`, { method: "PATCH" }),
      );
      return;
    }
    if (action === "cancel-cita") {
      openDialog(
        "Cancelar cita",
        fieldHtml(["motivo", "Motivo de cancelación", "textarea", true, 255]),
        (form) =>
          api(`/citas/${id}/cancelar`, {
            method: "PATCH",
            body: Object.fromEntries(new FormData(form)),
          }),
      );
      return;
    }
    if (action === "complete") {
      openDialog(
        "Registrar atención",
        '<p class="full muted">Confirma que la cita fue atendida. Se conservará en el historial y en los reportes.</p>',
        () => api(`/citas/${id}/atender`, { method: "PATCH" }),
      );
      return;
    }
    if (action === "cancel-absence") {
      openDialog(
        "Liberar disponibilidad",
        '<p class="full muted">Se cancelará esta restricción y se conservará su historial.</p>',
        () => api(`/${type}/${id}/cancelar`, { method: "PATCH" }),
      );
      return;
    }
    if (action === "access") {
      openDialog(
        "Permisos de " + row.username,
        fieldHtml(["roles", "Rol", "select", true], row.roles[0]?.name) +
          `<label>Estado<select name="activo"><option value="true" ${row.activo ? "selected" : ""}>Activo</option><option value="false" ${!row.activo ? "selected" : ""}>Inactivo</option></select></label><p class="field-help full">Al guardar se revocan las sesiones existentes de este usuario. Debe permanecer al menos un administrador activo.</p>`,
        (form) => {
          const d = Object.fromEntries(new FormData(form));
          return api(`/usuarios/${id}/acceso`, {
            method: "PATCH",
            body: { activo: d.activo === "true", roles: [d.roles] },
          });
        },
      );
    }
  } catch (e) {
    toast(e.message, true);
  }
};
$("#profile-button").onclick = () => {
  openDialog(
    "Tu cuenta",
    `<p class="full muted">${esc(user.email)} · ${esc(roleLabel(user))}</p><button type="button" id="change-password" class="secondary">Cambiar contraseña</button><button type="button" id="signout" class="secondary danger">Cerrar sesión</button>`,
    async () => {},
  );
  $("#save-editor").hidden = true;
  $("#editor").addEventListener(
    "close",
    () => ($("#save-editor").hidden = false),
    { once: true },
  );
  $("#signout").onclick = async () => {
    try {
      await api("/auth/signout", { method: "POST" });
      logoutLocal();
    } catch (e) {
      toast(e.message, true);
    }
  };
  $("#change-password").onclick = () => {
    $("#editor").close();
    openDialog(
      "Cambiar contraseña",
      fieldHtml(["actual", "Contraseña actual", "password", true]) +
        fieldHtml([
          "nueva",
          "Nueva contraseña (mínimo 10 caracteres)",
          "password",
          true,
          64,
        ]),
      async (form) => {
        const d = Object.fromEntries(new FormData(form));
        if (d.nueva.length < 10)
          throw new Error(
            "La nueva contraseña necesita al menos 10 caracteres",
          );
        await api("/auth/password", { method: "PUT", body: d });
        logoutLocal();
      },
    );
  };
};
$("#menu-toggle").onclick = () => $("#sidebar").classList.toggle("open");
window.addEventListener("hashchange", () => {
  search = "";
  tablePage = 0;
  render();
});
$("#signup-toggle").onclick = () => {
  signup = !signup;
  $("#signup-name-label").hidden = !signup;
  $("#login-form").elements.username.required = signup;
  $("#login-title").textContent = signup
    ? "Crea tu cuenta de consulta"
    : "Te damos la bienvenida";
  $("#login-description").textContent = signup
    ? "Un administrador debe aprobar tu acceso."
    : "Ingresa a tu espacio de trabajo.";
  $("#login-submit").textContent = signup
    ? "Crear cuenta"
    : "Ingresar al portal →";
  $("#signup-toggle").textContent = signup
    ? "Ya tengo una cuenta"
    : "Crear una cuenta de consulta";
  $("#login-form").elements.password.minLength = signup ? 10 : 1;
  $("#login-error").textContent = "";
};
$("#login-form").onsubmit = async (event) => {
  event.preventDefault();
  $("#login-submit").disabled = true;
  $("#login-error").textContent = "";
  try {
    const data = Object.fromEntries(new FormData(event.target));
    if (signup) {
      await api("/auth/signup", { method: "POST", body: data });
      $("#signup-toggle").click();
      toast("Solicitud enviada. Un administrador debe activar tu cuenta.");
    } else {
      const result = await api("/auth/signin", {
        method: "POST",
        body: { email: data.email, password: data.password },
      });
      token = result.accessToken;
      sessionStorage.setItem("aurora-token", token);
      await enter();
    }
  } catch (e) {
    $("#login-error").textContent = e.message;
  } finally {
    $("#login-submit").disabled = false;
  }
};
$$("[data-demo]").forEach(
  (button) =>
    (button.onclick = () => {
      if (signup) $("#signup-toggle").click();
      const name = button.dataset.demo;
      $("#login-form").elements.email.value = name + "@aurora.demo";
      $("#login-form").elements.password.value = {
        admin: "AuroraAdmin2026!",
        recepcion: "AuroraRecepcion2026!",
        consulta: "AuroraConsulta2026!",
      }[name];
      $("#login-form").requestSubmit();
    }),
);
async function boot() {
  try {
    const info = await api("/info");
    $("#demo-access").hidden = !info.demo;
    $("#demo-badge").hidden = !info.demo;
    if (token) await enter();
  } catch (e) {
    logoutLocal();
    $("#login-error").textContent = e.message;
  }
}
boot();
