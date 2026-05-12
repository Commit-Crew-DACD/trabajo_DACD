package org.ulpgc.dacd.control;

import io.javalin.Javalin;
import org.ulpgc.dacd.model.RecommendationConfig;
import org.ulpgc.dacd.storage.DatamartRepository;

public class RestApi {
    private static final String INDEX_HTML = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Agenda vuelo + evento</title>
                <style>
                    :root {
                        color-scheme: light;
                        --bg: #f4f7f8;
                        --bg-soft: #edf5f6;
                        --surface: rgba(255, 255, 255, 0.94);
                        --surface-strong: #ffffff;
                        --border: #d8e3e6;
                        --border-strong: #c3d3d7;
                        --ink: #162126;
                        --muted: #5f7078;
                        --accent: #0f766e;
                        --accent-strong: #115e59;
                        --accent-soft: #dff3f1;
                        --success: #166534;
                        --success-soft: #dcfce7;
                        --warning: #9a3412;
                        --warning-soft: #ffedd5;
                        --shadow: 0 24px 60px rgba(15, 118, 110, 0.08);
                        --radius-xl: 28px;
                        --radius-lg: 22px;
                        --radius-md: 18px;
                        --radius-sm: 12px;
                        --font-body: "Plus Jakarta Sans", "Segoe UI", sans-serif;
                        --font-heading: "Space Grotesk", "Segoe UI", sans-serif;
                    }

                    * {
                        box-sizing: border-box;
                    }

                    body {
                        margin: 0;
                        min-height: 100vh;
                        font-family: var(--font-body);
                        color: var(--ink);
                        background:
                            radial-gradient(circle at top left, #edf9f7 0, transparent 32%),
                            linear-gradient(180deg, #f7fbfb 0%, #eef3f4 100%);
                    }

                    a {
                        color: inherit;
                    }

                    button,
                    input {
                        font: inherit;
                    }

                    .page-shell {
                        width: min(1240px, calc(100% - 32px));
                        margin: 0 auto;
                        padding: 28px 0 56px;
                    }

                    .hero {
                        display: grid;
                        grid-template-columns: minmax(0, 1.55fr) minmax(280px, 0.95fr);
                        gap: 24px;
                        padding: 36px;
                        border-radius: var(--radius-xl);
                        background: linear-gradient(135deg, #0f1720 0%, #143339 62%, #0f766e 100%);
                        color: #f8fafc;
                        box-shadow: 0 30px 70px rgba(15, 23, 32, 0.2);
                    }

                    .hero-copy {
                        display: flex;
                        flex-direction: column;
                        gap: 18px;
                        justify-content: space-between;
                    }

                    .hero-eyebrow {
                        margin: 0;
                        font-size: 12px;
                        font-weight: 700;
                        letter-spacing: 0.18em;
                        text-transform: uppercase;
                        color: #b8f1ea;
                    }

                    .hero h1 {
                        margin: 0;
                        font-family: var(--font-heading);
                        font-size: clamp(32px, 5vw, 48px);
                        line-height: 1.05;
                        letter-spacing: -0.04em;
                    }

                    .hero p {
                        margin: 0;
                        max-width: 64ch;
                        color: rgba(241, 245, 249, 0.82);
                        line-height: 1.6;
                    }

                    .hero-highlights {
                        display: flex;
                        flex-wrap: wrap;
                        gap: 10px;
                    }

                    .highlight-pill {
                        display: inline-flex;
                        align-items: center;
                        padding: 10px 14px;
                        border: 1px solid rgba(184, 241, 234, 0.18);
                        border-radius: 999px;
                        background: rgba(255, 255, 255, 0.08);
                        color: #d7fbf5;
                        font-size: 13px;
                    }

                    .hero-panel {
                        display: flex;
                        flex-direction: column;
                        gap: 16px;
                        padding: 24px;
                        border: 1px solid rgba(226, 232, 240, 0.12);
                        border-radius: var(--radius-lg);
                        background: rgba(255, 255, 255, 0.1);
                        backdrop-filter: blur(10px);
                    }

                    .hero-panel h2 {
                        margin: 0;
                        font-family: var(--font-heading);
                        font-size: 20px;
                        letter-spacing: -0.03em;
                    }

                    .hero-panel p {
                        color: rgba(241, 245, 249, 0.78);
                    }

                    .search-field {
                        display: block;
                    }

                    .search-label {
                        display: block;
                        margin-bottom: 8px;
                        font-size: 12px;
                        font-weight: 700;
                        letter-spacing: 0.1em;
                        text-transform: uppercase;
                        color: #c8f7f0;
                    }

                    .search-field input {
                        width: 100%;
                        border: 1px solid rgba(226, 232, 240, 0.22);
                        border-radius: 14px;
                        padding: 14px 16px;
                        background: rgba(248, 250, 252, 0.14);
                        color: #f8fafc;
                        outline: none;
                    }

                    .search-field input::placeholder {
                        color: rgba(226, 232, 240, 0.65);
                    }

                    .search-field input:focus {
                        border-color: rgba(184, 241, 234, 0.55);
                        background: rgba(248, 250, 252, 0.18);
                    }

                    .panel-actions {
                        display: flex;
                        flex-wrap: wrap;
                        gap: 12px;
                        align-items: center;
                        justify-content: space-between;
                    }

                    .panel-actions button {
                        border: 0;
                        border-radius: 14px;
                        padding: 12px 16px;
                        background: #f8fafc;
                        color: #0f1720;
                        font-weight: 700;
                        cursor: pointer;
                        transition: transform 0.18s ease, box-shadow 0.18s ease;
                    }

                    .panel-actions button:hover {
                        transform: translateY(-1px);
                        box-shadow: 0 12px 24px rgba(15, 23, 32, 0.16);
                    }

                    .results-label,
                    .status-message {
                        font-size: 13px;
                        color: rgba(241, 245, 249, 0.78);
                    }

                    .status-message.error {
                        color: #fee2e2;
                    }

                    .main-content {
                        display: grid;
                        gap: 24px;
                        margin-top: 24px;
                    }

                    .overview-grid {
                        display: grid;
                        grid-template-columns: repeat(4, minmax(0, 1fr));
                        gap: 16px;
                    }

                    .metric-card {
                        padding: 20px;
                        border: 1px solid var(--border);
                        border-radius: var(--radius-md);
                        background: var(--surface);
                        box-shadow: var(--shadow);
                    }

                    .metric-card span {
                        display: block;
                        margin-bottom: 10px;
                        font-size: 12px;
                        font-weight: 700;
                        letter-spacing: 0.1em;
                        text-transform: uppercase;
                        color: var(--muted);
                    }

                    .metric-card strong {
                        display: block;
                        font-family: var(--font-heading);
                        font-size: clamp(26px, 4vw, 34px);
                        letter-spacing: -0.04em;
                    }

                    .metric-card small {
                        display: block;
                        margin-top: 10px;
                        color: var(--muted);
                        line-height: 1.5;
                    }

                    .events-panel {
                        padding: 28px;
                        border: 1px solid var(--border);
                        border-radius: var(--radius-xl);
                        background: var(--surface);
                        box-shadow: var(--shadow);
                    }

                    .section-header {
                        display: flex;
                        flex-wrap: wrap;
                        gap: 16px;
                        align-items: end;
                        justify-content: space-between;
                        margin-bottom: 24px;
                    }

                    .eyebrow {
                        margin: 0 0 8px;
                        font-size: 12px;
                        font-weight: 700;
                        letter-spacing: 0.12em;
                        text-transform: uppercase;
                        color: var(--accent);
                    }

                    .section-header h2 {
                        margin: 0;
                        font-family: var(--font-heading);
                        font-size: clamp(26px, 4vw, 34px);
                        letter-spacing: -0.04em;
                    }

                    .section-copy {
                        max-width: 55ch;
                        margin: 0;
                        color: var(--muted);
                        line-height: 1.6;
                    }

                    .events-list {
                        display: grid;
                        gap: 14px;
                    }

                    .event-card {
                        border: 1px solid var(--border);
                        border-radius: var(--radius-lg);
                        background: var(--surface-strong);
                        overflow: hidden;
                        transition: border-color 0.2s ease, box-shadow 0.2s ease;
                    }

                    .event-card[open] {
                        border-color: var(--border-strong);
                        box-shadow: 0 20px 40px rgba(15, 23, 32, 0.08);
                    }

                    .event-summary {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        gap: 18px;
                        padding: 22px 24px;
                        cursor: pointer;
                        list-style: none;
                    }

                    .event-summary::-webkit-details-marker {
                        display: none;
                    }

                    .event-summary-main {
                        display: flex;
                        align-items: center;
                        gap: 18px;
                        min-width: 0;
                    }

                    .date-badge {
                        display: grid;
                        place-items: center;
                        min-width: 86px;
                        padding: 14px 12px;
                        border-radius: 16px;
                        background: var(--bg-soft);
                        color: var(--accent-strong);
                        text-align: center;
                    }

                    .date-badge strong {
                        display: block;
                        font-family: var(--font-heading);
                        font-size: 24px;
                        line-height: 1;
                    }

                    .date-badge span {
                        margin-top: 4px;
                        font-size: 12px;
                        font-weight: 700;
                        text-transform: uppercase;
                        letter-spacing: 0.08em;
                    }

                    .event-copy {
                        min-width: 0;
                    }

                    .event-copy h3 {
                        margin: 0 0 6px;
                        font-size: 20px;
                        line-height: 1.25;
                    }

                    .event-copy p {
                        margin: 0;
                        color: var(--muted);
                        line-height: 1.5;
                    }

                    .event-summary-side {
                        display: flex;
                        flex-wrap: wrap;
                        gap: 10px;
                        align-items: center;
                        justify-content: end;
                    }

                    .availability-chip,
                    .summary-action {
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        padding: 10px 14px;
                        border-radius: 999px;
                        font-size: 13px;
                        font-weight: 700;
                    }

                    .availability-chip.available {
                        background: var(--success-soft);
                        color: var(--success);
                    }

                    .availability-chip.unavailable {
                        background: var(--warning-soft);
                        color: var(--warning);
                    }

                    .summary-action {
                        border: 1px solid var(--border);
                        color: var(--ink);
                        background: #f8fafc;
                    }

                    .event-card[open] .summary-action {
                        border-color: transparent;
                        background: var(--accent-soft);
                        color: var(--accent-strong);
                    }

                    .event-body {
                        padding: 0 24px 24px;
                        border-top: 1px solid #edf2f4;
                    }

                    .event-meta-grid {
                        display: grid;
                        grid-template-columns: repeat(4, minmax(0, 1fr));
                        gap: 12px;
                        padding-top: 18px;
                        margin-bottom: 18px;
                    }

                    .meta-card {
                        padding: 14px 16px;
                        border: 1px solid var(--border);
                        border-radius: 16px;
                        background: #fbfdfe;
                    }

                    .meta-card span {
                        display: block;
                        margin-bottom: 6px;
                        font-size: 11px;
                        font-weight: 700;
                        letter-spacing: 0.1em;
                        text-transform: uppercase;
                        color: var(--muted);
                    }

                    .meta-card strong,
                    .meta-card a {
                        font-size: 14px;
                        line-height: 1.5;
                        color: var(--ink);
                        text-decoration: none;
                    }

                    .meta-card a:hover {
                        color: var(--accent-strong);
                    }

                    .recommendations-grid {
                        display: grid;
                        gap: 14px;
                    }

                    .option-card {
                        padding: 18px;
                        border: 1px solid var(--border);
                        border-radius: 18px;
                        background: linear-gradient(180deg, #ffffff 0%, #f8fbfc 100%);
                    }

                    .option-header {
                        display: flex;
                        flex-wrap: wrap;
                        gap: 10px;
                        align-items: center;
                        justify-content: space-between;
                        margin-bottom: 14px;
                    }

                    .option-rank {
                        display: inline-flex;
                        align-items: center;
                        padding: 8px 12px;
                        border-radius: 999px;
                        background: var(--accent-soft);
                        color: var(--accent-strong);
                        font-size: 12px;
                        font-weight: 700;
                        letter-spacing: 0.08em;
                        text-transform: uppercase;
                    }

                    .captured-label {
                        color: var(--muted);
                        font-size: 13px;
                    }

                    .flight-columns {
                        display: grid;
                        grid-template-columns: repeat(2, minmax(0, 1fr));
                        gap: 14px;
                    }

                    .flight-card {
                        padding: 16px;
                        border: 1px solid var(--border);
                        border-radius: 16px;
                        background: #ffffff;
                    }

                    .flight-card span {
                        display: block;
                        margin-bottom: 8px;
                        font-size: 11px;
                        font-weight: 700;
                        letter-spacing: 0.1em;
                        text-transform: uppercase;
                        color: var(--muted);
                    }

                    .flight-card h4 {
                        margin: 0 0 12px;
                        font-family: var(--font-heading);
                        font-size: 22px;
                        letter-spacing: -0.04em;
                    }

                    .detail-grid {
                        display: grid;
                        gap: 10px;
                    }

                    .detail-row {
                        display: flex;
                        justify-content: space-between;
                        gap: 12px;
                        align-items: baseline;
                        padding-top: 10px;
                        border-top: 1px solid #edf2f4;
                    }

                    .detail-row:first-child {
                        padding-top: 0;
                        border-top: 0;
                    }

                    .detail-row strong {
                        color: var(--muted);
                        font-size: 13px;
                        font-weight: 600;
                    }

                    .detail-row span {
                        margin: 0;
                        font-size: 14px;
                        font-weight: 600;
                        letter-spacing: 0;
                        text-transform: none;
                        color: var(--ink);
                    }

                    .empty-state {
                        padding: 28px;
                        border: 1px dashed var(--border-strong);
                        border-radius: var(--radius-lg);
                        background: #fbfdfe;
                        color: var(--muted);
                        line-height: 1.6;
                    }

                    .empty-state.error-state {
                        border-color: #fecaca;
                        background: #fff7f7;
                        color: #991b1b;
                    }

                    @media (max-width: 1080px) {
                        .overview-grid,
                        .event-meta-grid {
                            grid-template-columns: repeat(2, minmax(0, 1fr));
                        }
                    }

                    @media (max-width: 920px) {
                        .hero {
                            grid-template-columns: 1fr;
                        }
                    }

                    @media (max-width: 760px) {
                        .page-shell {
                            width: min(100% - 20px, 1240px);
                            padding-top: 12px;
                        }

                        .hero,
                        .events-panel {
                            padding: 22px;
                        }

                        .overview-grid,
                        .event-meta-grid,
                        .flight-columns {
                            grid-template-columns: 1fr;
                        }

                        .event-summary,
                        .event-summary-main {
                            align-items: flex-start;
                        }

                        .event-summary {
                            flex-direction: column;
                        }

                        .event-summary-side {
                            width: 100%;
                            justify-content: flex-start;
                        }

                        .date-badge {
                            min-width: 78px;
                        }

                        .detail-row {
                            flex-direction: column;
                            align-items: flex-start;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="page-shell">
                    <header class="hero">
                        <div class="hero-copy">
                            <div>
                                <p class="hero-eyebrow">Explorador de agenda</p>
                                <h1>Todos los eventos y sus mejores opciones de vuelo en una sola vista.</h1>
                            </div>
                            <p>
                                La interfaz trabaja sobre el datamart completo de eventos y solo despliega los vuelos
                                cuando abres el evento que te interesa. Asi no se pierde informacion aunque el conjunto
                                de datos abarque varias semanas o meses.
                            </p>
                            <div class="hero-highlights">
                                <span class="highlight-pill">Eventos completos</span>
                                <span class="highlight-pill">Opciones de ida y vuelta por evento</span>
                                <span class="highlight-pill">Vista limpia para consulta rapida</span>
                            </div>
                        </div>

                        <aside class="hero-panel">
                            <div>
                                <h2>Busqueda y estado</h2>
                                <p>Filtra por evento, ciudad, recinto, aeropuertos, aerolinea o numero de vuelo.</p>
                            </div>

                            <label class="search-field" for="searchInput">
                                <span class="search-label">Buscar</span>
                                <input id="searchInput" type="search"
                                       placeholder="Ej. Madrid, Bad Bunny, LPA, Iberia, WiZink Center">
                            </label>

                            <div class="panel-actions">
                                <button id="refreshButton" type="button">Actualizar datos</button>
                                <div id="resultsLabel" class="results-label">Cargando eventos...</div>
                            </div>

                            <div id="statusMessage" class="status-message">Conectando con la API...</div>
                        </aside>
                    </header>

                    <main class="main-content">
                        <section class="overview-grid">
                            <article class="metric-card">
                                <span>Eventos</span>
                                <strong id="eventsCount">-</strong>
                                <small>Total de eventos disponibles en el datamart.</small>
                            </article>
                            <article class="metric-card">
                                <span>Con opciones</span>
                                <strong id="coveredEventsCount">-</strong>
                                <small>Eventos que ya tienen al menos una combinacion de vuelos.</small>
                            </article>
                            <article class="metric-card">
                                <span>Combinaciones</span>
                                <strong id="recommendationsCount">-</strong>
                                <small>Alternativas de ida y vuelta listas para revisar.</small>
                            </article>
                            <article class="metric-card">
                                <span>Ultima captura</span>
                                <strong id="lastUpdated">-</strong>
                                <small id="flightsSummary">-</small>
                            </article>
                        </section>

                        <section class="events-panel">
                            <div class="section-header">
                                <div>
                                    <p class="eyebrow">Agenda completa</p>
                                    <h2>Eventos disponibles</h2>
                                </div>
                                <p class="section-copy">
                                    Pulsa sobre cualquier evento para desplegar las opciones de vuelo asociadas.
                                    Si un evento no tiene combinaciones compatibles, tambien se mostrara.
                                </p>
                            </div>

                            <div id="eventsContainer" class="events-list">
                                <div class="empty-state">Cargando eventos...</div>
                            </div>
                        </section>
                    </main>
                </div>

                <script>
                    const state = {
                        events: [],
                        flights: [],
                        recommendations: [],
                        displayEvents: []
                    };

                    async function loadData() {
                        const stamp = Date.now();
                        const eventsContainer = document.getElementById('eventsContainer');
                        eventsContainer.innerHTML = '<div class="empty-state">Actualizando datos del datamart...</div>';
                        setStatusMessage('Actualizando eventos, vuelos y recomendaciones...', false);

                        try {
                            const [eventsResponse, flightsResponse, recommendationsResponse] = await Promise.all([
                                fetch(`/api/events?t=${stamp}`, { cache: 'no-store' }),
                                fetch(`/api/flights?t=${stamp}`, { cache: 'no-store' }),
                                fetch(`/api/recommendations?t=${stamp}`, { cache: 'no-store' })
                            ]);

                            if (!eventsResponse.ok || !flightsResponse.ok || !recommendationsResponse.ok) {
                                throw new Error('La API no devolvio una respuesta valida.');
                            }

                            state.events = sortEvents(await eventsResponse.json());
                            state.flights = await flightsResponse.json();
                            state.recommendations = sortRecommendations(await recommendationsResponse.json());
                            state.displayEvents = buildDisplayEvents(state.events, state.recommendations);

                            renderOverview();
                            renderEvents();
                            setStatusMessage('Datos sincronizados correctamente.', false);
                        } catch (error) {
                            console.error(error);
                            renderLoadError();
                            setStatusMessage('No se pudieron cargar los datos de la API.', true);
                        }
                    }

                    function renderOverview() {
                        const coveredEvents = state.displayEvents.filter(event => event.options.length > 0).length;
                        const visibleRecommendations = state.displayEvents.reduce(
                            (total, event) => total + event.options.length,
                            0
                        );

                        document.getElementById('eventsCount').textContent = state.displayEvents.length;
                        document.getElementById('coveredEventsCount').textContent =
                            `${coveredEvents}/${state.displayEvents.length || 0}`;
                        document.getElementById('recommendationsCount').textContent = visibleRecommendations;
                        document.getElementById('lastUpdated').textContent = getLatestCapturedLabel();
                        document.getElementById('flightsSummary').textContent =
                            `${state.flights.length} vuelos monitorizados en el conjunto actual.`;
                    }

                    function renderEvents() {
                        const query = normalizeText(document.getElementById('searchInput').value);
                        const filteredEvents = state.displayEvents.filter(event => matchesEvent(event, event.options, query));

                        document.getElementById('resultsLabel').textContent =
                            `${filteredEvents.length} eventos visibles de ${state.displayEvents.length}`;

                        const eventsContainer = document.getElementById('eventsContainer');

                        if (filteredEvents.length === 0) {
                            eventsContainer.innerHTML = `
                                <div class="empty-state">
                                    No hay eventos que coincidan con la busqueda actual.
                                </div>
                            `;
                            return;
                        }

                        eventsContainer.innerHTML = filteredEvents
                            .map(event => renderEventCard(event, event.options))
                            .join('');
                    }

                    function renderEventCard(event, options) {
                        const availabilityClass = options.length > 0 ? 'available' : 'unavailable';
                        const availabilityLabel = options.length > 0
                            ? `${options.length} ${options.length === 1 ? 'opcion disponible' : 'opciones disponibles'}`
                            : 'Sin vuelos compatibles';

                        return `
                            <details class="event-card">
                                <summary class="event-summary">
                                    <div class="event-summary-main">
                                        <div class="date-badge">
                                            <strong>${escapeHtml(formatShortDay(event.date))}</strong>
                                            <span>${escapeHtml(formatShortMonth(event.date))}</span>
                                        </div>

                                        <div class="event-copy">
                                            <h3>${escapeHtml(event.name || 'Evento sin nombre')}</h3>
                                            <p>${escapeHtml(buildEventSubtitle(event))}</p>
                                        </div>
                                    </div>

                                    <div class="event-summary-side">
                                        <span class="availability-chip ${availabilityClass}">${escapeHtml(availabilityLabel)}</span>
                                        <span class="summary-action">Ver detalle</span>
                                    </div>
                                </summary>

                                <div class="event-body">
                                    <div class="event-meta-grid">
                                        <div class="meta-card">
                                            <span>Fecha</span>
                                            <strong>${escapeHtml(formatEventDate(event.date, event.startTime))}</strong>
                                        </div>
                                        <div class="meta-card">
                                            <span>Recinto</span>
                                            <strong>${escapeHtml(event.venue || 'No disponible')}</strong>
                                        </div>
                                        <div class="meta-card">
                                            <span>Capturado</span>
                                            <strong>${escapeHtml(formatCapturedAt(event.capturedAt))}</strong>
                                        </div>
                                        <div class="meta-card">
                                            <span>Enlace</span>
                                            ${renderEventLink(event.url)}
                                        </div>
                                    </div>

                                    ${renderRecommendations(options)}
                                </div>
                            </details>
                        `;
                    }

                    function renderRecommendations(options) {
                        if (options.length === 0) {
                            return `
                                <div class="empty-state">
                                    Este evento sigue visible en la agenda, pero no se han encontrado combinaciones
                                    de vuelo compatibles en los datos cargados.
                                </div>
                            `;
                        }

                        return `
                            <div class="recommendations-grid">
                                ${options.map((option, index) => renderOptionCard(option, index)).join('')}
                            </div>
                        `;
                    }

                    function renderOptionCard(option, index) {
                        return `
                            <article class="option-card">
                                <div class="option-header">
                                    <span class="option-rank">Opcion ${index + 1}</span>
                                    <span class="captured-label">Generada ${escapeHtml(formatCapturedAt(option.capturedAt))}</span>
                                </div>

                                <div class="flight-columns">
                                    <section class="flight-card">
                                        <span>Vuelo de ida</span>
                                        <h4>${escapeHtml(`${option.outboundOrigin} -> ${option.outboundDestination}`)}</h4>
                                        <div class="detail-grid">
                                            <div class="detail-row">
                                                <strong>Aerolinea</strong>
                                                <span>${escapeHtml(buildAirlineLabel(option.outboundAirline, option.outboundFlightNumber))}</span>
                                            </div>
                                            <div class="detail-row">
                                                <strong>Salida</strong>
                                                <span>${escapeHtml(formatDateTime(option.outboundDepartureTime))}</span>
                                            </div>
                                            <div class="detail-row">
                                                <strong>Llegada</strong>
                                                <span>${escapeHtml(formatDateTime(option.outboundArrivalTime))}</span>
                                            </div>
                                        </div>
                                    </section>

                                    <section class="flight-card">
                                        <span>Vuelo de vuelta</span>
                                        <h4>${escapeHtml(`${option.returnOrigin} -> ${option.returnDestination}`)}</h4>
                                        <div class="detail-grid">
                                            <div class="detail-row">
                                                <strong>Aerolinea</strong>
                                                <span>${escapeHtml(buildAirlineLabel(option.returnAirline, option.returnFlightNumber))}</span>
                                            </div>
                                            <div class="detail-row">
                                                <strong>Salida</strong>
                                                <span>${escapeHtml(formatDateTime(option.returnDepartureTime))}</span>
                                            </div>
                                            <div class="detail-row">
                                                <strong>Ventana evento</strong>
                                                <span>${escapeHtml(formatEventWindow(option.eventDate, option.eventStartTime, option.eventEndTime))}</span>
                                            </div>
                                        </div>
                                    </section>
                                </div>
                            </article>
                        `;
                    }

                    function renderEventLink(url) {
                        if (!url) {
                            return '<strong>No disponible</strong>';
                        }

                        return `<a href="${escapeAttribute(url)}" target="_blank" rel="noreferrer">Abrir evento</a>`;
                    }

                    function buildDisplayEvents(events, recommendations) {
                        const groupedEvents = new Map();
                        const eventIdsByDisplayKey = new Map();

                        sortEvents(events)
                            .filter(event => !isParkingEvent(event))
                            .forEach(event => {
                                const displayKey = buildDisplayEventKey(event);
                                const existingGroup = groupedEvents.get(displayKey) || {
                                    events: [],
                                    recommendations: []
                                };

                                existingGroup.events.push(event);
                                groupedEvents.set(displayKey, existingGroup);
                                eventIdsByDisplayKey.set(event.id, displayKey);
                            });

                        sortRecommendations(recommendations).forEach(recommendation => {
                            const displayKey = eventIdsByDisplayKey.get(recommendation.eventId);
                            if (!displayKey || !groupedEvents.has(displayKey)) {
                                return;
                            }

                            groupedEvents.get(displayKey).recommendations.push(recommendation);
                        });

                        return [...groupedEvents.values()]
                            .map(group => toDisplayEvent(group.events, group.recommendations))
                            .sort(compareDisplayEvents);
                    }

                    function toDisplayEvent(events, recommendations) {
                        const representative = chooseRepresentativeEvent(events);

                        return {
                            id: buildDisplayEventKey(representative),
                            name: chooseDisplayName(events),
                            city: choosePreferredValue(events.map(event => event.city)),
                            venue: choosePreferredVenue(events),
                            date: representative.date,
                            startTime: choosePreferredStartTime(events),
                            capturedAt: chooseLatestCapturedAt(events),
                            url: choosePreferredUrl(events),
                            options: mergeRecommendations(recommendations)
                        };
                    }

                    function mergeRecommendations(recommendations) {
                        const merged = new Map();

                        sortRecommendations(recommendations).forEach(recommendation => {
                            const key = buildRecommendationKey(recommendation);
                            if (!merged.has(key)) {
                                merged.set(key, recommendation);
                            }
                        });

                        return sortRecommendations([...merged.values()]);
                    }

                    function buildRecommendationKey(recommendation) {
                        return [
                            recommendation.outboundFlightNumber,
                            recommendation.outboundDepartureTime,
                            recommendation.outboundArrivalTime,
                            recommendation.returnFlightNumber,
                            recommendation.returnDepartureTime,
                            recommendation.outboundAirline,
                            recommendation.returnAirline
                        ].join('|');
                    }

                    function groupRecommendationsByEvent(recommendations) {
                        return recommendations.reduce((groups, recommendation) => {
                            const key = recommendation.eventId || `${recommendation.eventName}-${recommendation.eventDate}`;
                            groups[key] = groups[key] || [];
                            groups[key].push(recommendation);
                            return groups;
                        }, {});
                    }

                    function sortEvents(events) {
                        return [...events].sort((left, right) => {
                            const dateComparison = compareOptionalStrings(left.date, right.date);
                            if (dateComparison !== 0) {
                                return dateComparison;
                            }

                            const timeComparison = compareOptionalStrings(normalizeEventTime(left.startTime), normalizeEventTime(right.startTime));
                            if (timeComparison !== 0) {
                                return timeComparison;
                            }

                            return (left.name || '').localeCompare(right.name || '', 'es', { sensitivity: 'base' });
                        });
                    }

                    function sortRecommendations(recommendations) {
                        return [...recommendations].sort((left, right) => {
                            const eventComparison = compareOptionalStrings(left.eventDate, right.eventDate)
                                || compareOptionalStrings(normalizeEventTime(left.eventStartTime), normalizeEventTime(right.eventStartTime))
                                || (left.eventName || '').localeCompare(right.eventName || '', 'es', { sensitivity: 'base' });

                            if (eventComparison !== 0) {
                                return eventComparison;
                            }

                            return compareDateTimes(right.outboundArrivalTime, left.outboundArrivalTime)
                                || compareDateTimes(left.returnDepartureTime, right.returnDepartureTime);
                        });
                    }

                    function compareDisplayEvents(left, right) {
                        return compareOptionalStrings(left.date, right.date)
                            || compareOptionalStrings(normalizeEventTime(left.startTime), normalizeEventTime(right.startTime))
                            || (left.name || '').localeCompare(right.name || '', 'es', { sensitivity: 'base' });
                    }

                    function compareOptionalStrings(left, right) {
                        if (left && right) {
                            return left.localeCompare(right);
                        }

                        if (left) {
                            return -1;
                        }

                        if (right) {
                            return 1;
                        }

                        return 0;
                    }

                    function compareDateTimes(left, right) {
                        const leftDate = toDate(left);
                        const rightDate = toDate(right);

                        if (leftDate && rightDate) {
                            return leftDate - rightDate;
                        }

                        return compareOptionalStrings(left, right);
                    }

                    function buildDisplayEventKey(event) {
                        return [
                            event.date,
                            normalizeText(extractArtistName(event.name)),
                            normalizeText(event.venue || event.city)
                        ].join('|');
                    }

                    function extractArtistName(name) {
                        const cleanedName = stripEventDecorators(name);
                        const separators = [' - ', ': ', ' | '];

                        for (const separator of separators) {
                            const separatorIndex = cleanedName.indexOf(separator);
                            if (separatorIndex > 0) {
                                return cleanedName.slice(0, separatorIndex).trim();
                            }
                        }

                        return cleanedName.trim();
                    }

                    function stripEventDecorators(name) {
                        return String(name || '')
                            .replace(/\\|\\s*vip packages.*$/i, '')
                            .replace(/\\s+/g, ' ')
                            .trim();
                    }

                    function isParkingEvent(event) {
                        return normalizeText(event && event.name).includes('parking');
                    }

                    function chooseRepresentativeEvent(events) {
                        return [...events].sort((left, right) => compareRepresentativeEvents(left, right))[0];
                    }

                    function compareRepresentativeEvents(left, right) {
                        return representativeScore(right) - representativeScore(left)
                            || compareOptionalStrings(normalizeEventTime(left.startTime), normalizeEventTime(right.startTime))
                            || compareOptionalStrings(left.name, right.name);
                    }

                    function representativeScore(event) {
                        let score = 0;

                        if (!normalizeText(event.name).includes('vip packages')) {
                            score += 4;
                        }

                        if (normalizeText(event.name) === normalizeText(extractArtistName(event.name))) {
                            score += 4;
                        }

                        if (event.venue) {
                            score += 2;
                        }

                        if (event.url) {
                            score += 1;
                        }

                        return score;
                    }

                    function chooseDisplayName(events) {
                        const names = events
                            .map(event => extractArtistName(event.name))
                            .filter(Boolean)
                            .sort((left, right) => left.length - right.length || left.localeCompare(right, 'es', { sensitivity: 'base' }));

                        return names[0] || 'Evento sin nombre';
                    }

                    function choosePreferredVenue(events) {
                        const venues = events
                            .map(event => event.venue)
                            .filter(Boolean)
                            .sort((left, right) => right.length - left.length || left.localeCompare(right, 'es', { sensitivity: 'base' }));

                        return venues[0] || 'No disponible';
                    }

                    function choosePreferredStartTime(events) {
                        const startTimes = events
                            .map(event => normalizeEventTime(event.startTime))
                            .filter(Boolean)
                            .sort((left, right) => left.localeCompare(right));

                        return startTimes[0] || '';
                    }

                    function chooseLatestCapturedAt(events) {
                        const latestDate = events
                            .map(event => toDate(event.capturedAt))
                            .filter(Boolean)
                            .sort((left, right) => right - left)[0];

                        return latestDate ? latestDate.toISOString() : '';
                    }

                    function choosePreferredUrl(events) {
                        const preferredEvent = chooseRepresentativeEvent(events);
                        if (preferredEvent.url) {
                            return preferredEvent.url;
                        }

                        const alternativeEvent = events.find(event => event.url);
                        return alternativeEvent ? alternativeEvent.url : '';
                    }

                    function choosePreferredValue(values) {
                        return values.find(Boolean) || '';
                    }

                    function matchesEvent(event, options, query) {
                        if (!query) {
                            return true;
                        }

                        const haystack = [
                            event.name,
                            event.city,
                            event.venue,
                            event.date,
                            event.startTime,
                            ...options.flatMap(option => [
                                option.outboundAirline,
                                option.outboundFlightNumber,
                                option.outboundOrigin,
                                option.outboundDestination,
                                option.returnAirline,
                                option.returnFlightNumber,
                                option.returnOrigin,
                                option.returnDestination
                            ])
                        ].join(' ');

                        return normalizeText(haystack).includes(query);
                    }

                    function buildEventSubtitle(event) {
                        return [event.city, event.venue].filter(Boolean).join(' | ') || 'Ubicacion no disponible';
                    }

                    function buildAirlineLabel(airline, flightNumber) {
                        return [airline, flightNumber].filter(Boolean).join(' ') || 'No disponible';
                    }

                    function formatEventWindow(date, startTime, endTime) {
                        const formattedDate = formatLongDate(date);
                        const start = formatClock(startTime);
                        const end = formatClock(endTime);

                        if (!start && !end) {
                            return formattedDate;
                        }

                        return [formattedDate, [start, end].filter(Boolean).join(' - ')].filter(Boolean).join(' | ');
                    }

                    function formatEventDate(date, time) {
                        const formattedDate = formatLongDate(date);
                        const formattedTime = formatClock(time);
                        return [formattedDate, formattedTime].filter(Boolean).join(' | ') || 'Fecha no disponible';
                    }

                    function formatDateTime(value) {
                        const date = toDate(value);

                        if (!date) {
                            return value || 'No disponible';
                        }

                        return new Intl.DateTimeFormat('es-ES', {
                            day: '2-digit',
                            month: 'short',
                            year: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                        }).format(date);
                    }

                    function formatCapturedAt(value) {
                        const date = toDate(value);

                        if (!date) {
                            return 'No disponible';
                        }

                        return new Intl.DateTimeFormat('es-ES', {
                            day: '2-digit',
                            month: 'short',
                            year: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                        }).format(date);
                    }

                    function formatLongDate(value) {
                        const date = toDate(value);

                        if (!date) {
                            return value || '';
                        }

                        return new Intl.DateTimeFormat('es-ES', {
                            day: '2-digit',
                            month: 'long',
                            year: 'numeric'
                        }).format(date);
                    }

                    function formatShortDay(value) {
                        const date = toDate(value);

                        if (!date) {
                            return '--';
                        }

                        return String(date.getDate()).padStart(2, '0');
                    }

                    function formatShortMonth(value) {
                        const date = toDate(value);

                        if (!date) {
                            return '---';
                        }

                        return new Intl.DateTimeFormat('es-ES', { month: 'short' })
                            .format(date)
                            .replace('.', '')
                            .toUpperCase();
                    }

                    function formatClock(value) {
                        const normalized = normalizeEventTime(value);

                        if (!normalized) {
                            return '';
                        }

                        const pieces = normalized.split(':');
                        return pieces.length >= 2 ? `${pieces[0]}:${pieces[1]}` : normalized;
                    }

                    function normalizeEventTime(value) {
                        if (!value || value === 'N/A') {
                            return '';
                        }

                        return value.length === 5 ? `${value}:00` : value;
                    }

                    function getLatestCapturedLabel() {
                        const latest = [
                            ...state.events.map(item => item.capturedAt),
                            ...state.flights.map(item => item.capturedAt),
                            ...state.recommendations.map(item => item.capturedAt)
                        ]
                            .map(toDate)
                            .filter(Boolean)
                            .sort((left, right) => right - left)[0];

                        return latest ? formatCapturedAt(latest.toISOString()) : 'Sin datos';
                    }

                    function toDate(value) {
                        if (!value) {
                            return null;
                        }

                        if (value instanceof Date && !Number.isNaN(value.getTime())) {
                            return value;
                        }

                        const trimmed = String(value).trim();

                        if (/^\\d{4}-\\d{2}-\\d{2}$/.test(trimmed)) {
                            const parsedDate = new Date(`${trimmed}T00:00:00`);
                            return Number.isNaN(parsedDate.getTime()) ? null : parsedDate;
                        }

                        if (/^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$/.test(trimmed)) {
                            const parsedDate = new Date(trimmed.replace(' ', 'T'));
                            return Number.isNaN(parsedDate.getTime()) ? null : parsedDate;
                        }

                        const parsed = new Date(trimmed);
                        return Number.isNaN(parsed.getTime()) ? null : parsed;
                    }

                    function normalizeText(value) {
                        return String(value || '')
                            .normalize('NFD')
                            .replace(/[\\u0300-\\u036f]/g, '')
                            .toLowerCase()
                            .trim();
                    }

                    function escapeHtml(value) {
                        if (value === null || value === undefined) {
                            return '';
                        }

                        return String(value)
                            .replaceAll('&', '&amp;')
                            .replaceAll('<', '&lt;')
                            .replaceAll('>', '&gt;')
                            .replaceAll('"', '&quot;')
                            .replaceAll("'", '&#039;');
                    }

                    function escapeAttribute(value) {
                        return escapeHtml(value);
                    }

                    function renderLoadError() {
                        document.getElementById('eventsCount').textContent = '-';
                        document.getElementById('coveredEventsCount').textContent = '-';
                        document.getElementById('recommendationsCount').textContent = '-';
                        document.getElementById('lastUpdated').textContent = '-';
                        document.getElementById('flightsSummary').textContent = 'No se pudieron recuperar los vuelos.';
                        document.getElementById('resultsLabel').textContent = 'Sin conexion con la API';
                        document.getElementById('eventsContainer').innerHTML = `
                            <div class="empty-state error-state">
                                No se pudieron cargar los datos desde la API REST. Revisa la consola del servicio
                                y vuelve a intentarlo.
                            </div>
                        `;
                    }

                    function setStatusMessage(message, isError) {
                        const statusMessage = document.getElementById('statusMessage');
                        statusMessage.textContent = message;
                        statusMessage.classList.toggle('error', Boolean(isError));
                    }

                    document.getElementById('searchInput').addEventListener('input', renderEvents);
                    document.getElementById('refreshButton').addEventListener('click', loadData);
                    loadData();
                </script>
            </body>
            </html>
            """;

    private final DatamartRepository repository;
    private final RecommendationService recommendationService;
    private final int port;

    public RestApi(DatamartRepository repository, RecommendationService recommendationService, int port) {
        this.repository = repository;
        this.recommendationService = recommendationService;
        this.port = port;
    }

    public void start() {
        Javalin app = Javalin.create(config -> {
            config.routes.get("/", context -> context.result(INDEX_HTML).contentType("text/html"));

            config.routes.get("/api/recommendations",
                    context -> context.json(repository.findAllRecommendations()));

            config.routes.get("/api/config",
                    context -> context.json(repository.getConfig()));

            config.routes.get("/api/events",
                    context -> context.json(repository.findAllEvents()));

            config.routes.get("/api/flights",
                    context -> context.json(repository.findAllFlights()));

            config.routes.post("/api/config", context -> {
                ConfigRequest request = context.bodyAsClass(ConfigRequest.class);

                RecommendationConfig updatedConfig = new RecommendationConfig(
                        request.originAirport(),
                        request.outboundMarginHours(),
                        request.returnMarginHours(),
                        request.defaultEventDurationHours()
                );

                repository.saveConfig(updatedConfig);
                recommendationService.rebuildRecommendations();

                context.json(repository.getConfig());
            });

            config.routes.get("/api/stats",
                    context -> context.json(new StatsResponse(
                            repository.countEvents(),
                            repository.countFlights(),
                            repository.countRecommendations()
                    )));
        });

        app.start(port);
        System.out.println("REST API started at http://localhost:" + port);
    }

    private record StatsResponse(int events, int flights, int recommendations) {
    }

    private record ConfigRequest(
            String originAirport,
            int outboundMarginHours,
            int returnMarginHours,
            int defaultEventDurationHours
    ) {
    }
}