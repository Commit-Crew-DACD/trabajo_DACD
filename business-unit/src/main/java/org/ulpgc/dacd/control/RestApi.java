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
                        --surface: #ffffff;
                        --surface-soft: #f8fbfc;
                        --border: #d8e3e6;
                        --ink: #162126;
                        --muted: #5f7078;
                        --accent: #0f766e;
                        --accent-strong: #115e59;
                        --accent-soft: #dff3f1;
                        --success: #166534;
                        --success-soft: #dcfce7;
                        --warning: #9a3412;
                        --warning-soft: #ffedd5;
                        --danger: #991b1b;
                        --danger-soft: #fff7f7;
                        --shadow: 0 24px 60px rgba(15, 118, 110, 0.08);
                        --font-body: "Plus Jakarta Sans", "Segoe UI", sans-serif;
                        --font-heading: "Space Grotesk", "Segoe UI", sans-serif;
                    }

                    * { box-sizing: border-box; }

                    body {
                        margin: 0;
                        min-height: 100vh;
                        font-family: var(--font-body);
                        color: var(--ink);
                        background:
                            radial-gradient(circle at top left, #edf9f7 0, transparent 32%),
                            linear-gradient(180deg, #f7fbfb 0%, #eef3f4 100%);
                    }

                    button, input { font: inherit; }

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
                        border-radius: 28px;
                        background: linear-gradient(135deg, #0f1720 0%, #143339 62%, #0f766e 100%);
                        color: #f8fafc;
                        box-shadow: 0 30px 70px rgba(15, 23, 32, 0.2);
                    }

                    .hero-copy {
                        display: flex;
                        flex-direction: column;
                        justify-content: space-between;
                        gap: 18px;
                    }

                    .hero-eyebrow, .search-label, .filter-label, .eyebrow {
                        margin: 0;
                        font-size: 12px;
                        font-weight: 700;
                        letter-spacing: 0.12em;
                        text-transform: uppercase;
                    }

                    .hero-eyebrow, .search-label, .filter-label { color: #c8f7f0; }
                    .eyebrow { color: var(--accent); margin-bottom: 8px; }

                    .hero h1 {
                        margin: 0;
                        font-family: var(--font-heading);
                        font-size: clamp(32px, 5vw, 48px);
                        line-height: 1.05;
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
                        padding: 10px 14px;
                        border: 1px solid rgba(184, 241, 234, 0.18);
                        border-radius: 999px;
                        background: rgba(255, 255, 255, 0.08);
                        color: #d7fbf5;
                        font-size: 13px;
                    }

                    .hero-panel {
                        display: grid;
                        gap: 16px;
                        padding: 24px;
                        border: 1px solid rgba(226, 232, 240, 0.12);
                        border-radius: 22px;
                        background: rgba(255, 255, 255, 0.1);
                        backdrop-filter: blur(10px);
                    }

                    .hero-panel h2 {
                        margin: 0 0 8px;
                        font-family: var(--font-heading);
                        font-size: 20px;
                    }

                    .search-field, .availability-filter { display: grid; gap: 8px; }

                    .search-field input {
                        width: 100%;
                        border: 1px solid rgba(226, 232, 240, 0.22);
                        border-radius: 14px;
                        padding: 14px 16px;
                        background: rgba(248, 250, 252, 0.14);
                        color: #f8fafc;
                        outline: none;
                    }

                    .search-field input::placeholder { color: rgba(226, 232, 240, 0.65); }

                    .filter-options {
                        display: grid;
                        grid-template-columns: repeat(3, minmax(0, 1fr));
                        gap: 8px;
                    }

                    .filter-option {
                        border: 1px solid rgba(226, 232, 240, 0.22);
                        border-radius: 12px;
                        padding: 10px 12px;
                        background: rgba(248, 250, 252, 0.12);
                        color: rgba(248, 250, 252, 0.82);
                        font-size: 13px;
                        font-weight: 700;
                        cursor: pointer;
                    }

                    .filter-option.active {
                        border-color: rgba(184, 241, 234, 0.7);
                        background: #f8fafc;
                        color: #0f1720;
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
                    }

                    .results-label, .status-message {
                        font-size: 13px;
                        color: rgba(241, 245, 249, 0.78);
                    }

                    .status-message.error { color: #fee2e2; }

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

                    .metric-card, .events-panel {
                        border: 1px solid var(--border);
                        background: rgba(255, 255, 255, 0.94);
                        box-shadow: var(--shadow);
                    }

                    .metric-card {
                        padding: 20px;
                        border-radius: 18px;
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
                    }

                    .metric-card small {
                        display: block;
                        margin-top: 10px;
                        color: var(--muted);
                        line-height: 1.5;
                    }

                    .events-panel {
                        padding: 28px;
                        border-radius: 28px;
                    }

                    .section-header {
                        display: flex;
                        flex-wrap: wrap;
                        gap: 16px;
                        align-items: end;
                        justify-content: space-between;
                        margin-bottom: 24px;
                    }

                    .section-header h2 {
                        margin: 0;
                        font-family: var(--font-heading);
                        font-size: clamp(26px, 4vw, 34px);
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
                        border-radius: 22px;
                        background: var(--surface);
                        overflow: hidden;
                    }

                    .event-card[open] {
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

                    .event-summary::-webkit-details-marker { display: none; }

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
                        background: #edf5f6;
                        color: var(--accent-strong);
                        text-align: center;
                    }

                    .date-badge strong {
                        font-family: var(--font-heading);
                        font-size: 24px;
                        line-height: 1;
                    }

                    .date-badge span {
                        margin-top: 4px;
                        font-size: 12px;
                        font-weight: 700;
                        text-transform: uppercase;
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
                        justify-content: flex-end;
                    }

                    .availability-chip, .summary-action, .option-rank {
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        border-radius: 999px;
                        font-size: 13px;
                        font-weight: 700;
                    }

                    .availability-chip, .summary-action { padding: 10px 14px; }
                    .availability-chip.available { background: var(--success-soft); color: var(--success); }
                    .availability-chip.unavailable { background: var(--warning-soft); color: var(--warning); }
                    .summary-action { border: 1px solid var(--border); background: #f8fafc; color: var(--ink); }

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

                    .meta-card, .flight-card, .option-card {
                        border: 1px solid var(--border);
                        background: var(--surface-soft);
                    }

                    .meta-card {
                        padding: 14px 16px;
                        border-radius: 16px;
                    }

                    .meta-card span, .flight-card span {
                        display: block;
                        margin-bottom: 6px;
                        font-size: 11px;
                        font-weight: 700;
                        letter-spacing: 0.1em;
                        text-transform: uppercase;
                        color: var(--muted);
                    }

                    .meta-card strong, .meta-card a {
                        font-size: 14px;
                        color: var(--ink);
                        text-decoration: none;
                    }

                    .meta-card a:hover { color: var(--accent-strong); }

                    .recommendations-grid {
                        display: grid;
                        gap: 14px;
                    }

                    .option-card {
                        padding: 18px;
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
                        padding: 8px 12px;
                        background: var(--accent-soft);
                        color: var(--accent-strong);
                        font-size: 12px;
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
                        border-radius: 16px;
                        background: #ffffff;
                    }

                    .flight-card h4 {
                        margin: 0 0 12px;
                        font-family: var(--font-heading);
                        font-size: 22px;
                    }

                    .flight-card h4 a {
                        color: var(--accent-strong);
                        text-decoration: none;
                    }

                    .flight-card h4 a:hover {
                        text-decoration: underline;
                    }

                    .detail-grid {
                        display: grid;
                        gap: 10px;
                    }

                    .detail-row {
                        display: flex;
                        justify-content: space-between;
                        gap: 12px;
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
                    }

                    .detail-row span {
                        font-size: 14px;
                        font-weight: 600;
                    }

                    .empty-state {
                        padding: 28px;
                        border: 1px dashed #c3d3d7;
                        border-radius: 22px;
                        background: #fbfdfe;
                        color: var(--muted);
                        line-height: 1.6;
                    }

                    .empty-state.error-state {
                        border-color: #fecaca;
                        background: var(--danger-soft);
                        color: var(--danger);
                    }

                    @media (max-width: 1080px) {
                        .overview-grid, .event-meta-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
                    }

                    @media (max-width: 920px) {
                        .hero { grid-template-columns: 1fr; }
                    }

                    @media (max-width: 760px) {
                        .page-shell { width: min(100% - 20px, 1240px); padding-top: 12px; }
                        .hero, .events-panel { padding: 22px; }
                        .overview-grid, .event-meta-grid, .flight-columns, .filter-options { grid-template-columns: 1fr; }
                        .event-summary, .event-summary-main { align-items: flex-start; }
                        .event-summary { flex-direction: column; }
                        .event-summary-side { justify-content: flex-start; }
                        .detail-row { flex-direction: column; }
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
                                La interfaz trabaja sobre el datamart completo de eventos y despliega los vuelos
                                cuando abres el evento que te interesa.
                            </p>
                            <div class="hero-highlights">
                                <span class="highlight-pill">Eventos completos</span>
                                <span class="highlight-pill">Opciones por evento</span>
                                <span class="highlight-pill">Consulta rapida</span>
                            </div>
                        </div>

                        <aside class="hero-panel">
                            <div>
                                <h2>Busqueda y estado</h2>
                                <p>Filtra por evento, ciudad, recinto, aeropuerto, aerolinea o numero de vuelo.</p>
                            </div>

                            <label class="search-field" for="searchInput">
                                <span class="search-label">Buscar</span>
                                <input id="searchInput" type="search"
                                       placeholder="Ej. Madrid, Bad Bunny, LPA, Iberia">
                            </label>

                            <div class="availability-filter">
                                <span class="filter-label">Mostrar</span>
                                <div class="filter-options">
                                    <button class="filter-option active" type="button" data-filter="all">Todos</button>
                                    <button class="filter-option" type="button" data-filter="with">Con vuelos</button>
                                    <button class="filter-option" type="button" data-filter="without">Sin vuelos</button>
                                </div>
                            </div>

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
                                <small>Eventos con al menos una combinacion de vuelos.</small>
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
                                    Pulsa sobre cualquier evento para desplegar sus opciones de vuelo. Los eventos sin
                                    combinaciones siguen visibles para reflejar toda la agenda cargada.
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
                        displayEvents: [],
                        availabilityFilter: 'all'
                    };

                    async function loadData() {
                        const stamp = Date.now();
                        document.getElementById('eventsContainer').innerHTML =
                            '<div class="empty-state">Actualizando datos del datamart...</div>';
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
                        const filteredEvents = state.displayEvents
                            .filter(matchesAvailabilityFilter)
                            .filter(event => matchesEvent(event, event.options, query));

                        document.getElementById('resultsLabel').textContent =
                            `${filteredEvents.length} eventos visibles de ${state.displayEvents.length}`;

                        const eventsContainer = document.getElementById('eventsContainer');

                        if (filteredEvents.length === 0) {
                            eventsContainer.innerHTML = `
                                <div class="empty-state">
                                    No hay eventos que coincidan con los filtros actuales.
                                </div>
                            `;
                            return;
                        }

                        eventsContainer.innerHTML = filteredEvents
                            .map(event => renderEventCard(event, event.options))
                            .join('');
                    }

                    function matchesAvailabilityFilter(event) {
                        if (state.availabilityFilter === 'with') {
                            return event.options.length > 0;
                        }

                        if (state.availabilityFilter === 'without') {
                            return event.options.length === 0;
                        }

                        return true;
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
                                    Este evento esta en la agenda, pero aun no tiene combinaciones de vuelo compatibles.
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
                                        <h4>${renderFlightLink(
                                            `${option.outboundOrigin} -> ${option.outboundDestination}`,
                                            option.outboundFlightNumber,
                                            option.outboundDepartureTime,
                                            option.outboundOrigin,
                                            option.outboundDestination
                                        )}</h4>
            
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
                                        <h4>${renderFlightLink(
                                            `${option.returnOrigin} -> ${option.returnDestination}`,
                                            option.returnFlightNumber,
                                            option.returnDepartureTime,
                                            option.returnOrigin,
                                            option.returnDestination
                                        )}</h4>
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
                        if (!url || !isValidUrl(url)) {
                            return '<strong>No disponible</strong>';
                        }

                        return `<a href="${escapeAttribute(url)}" target="_blank" rel="noreferrer">Abrir evento</a>`;
                    }

                    function renderFlightLink(label, flightNumber, departureTime, origin, destination) {
                         const url = buildGoogleFlightsUrl(origin, destination, departureTime);
            
                         if (!url) {
                             return escapeHtml(label);
                         }
            
                         return `<a href="${escapeAttribute(url)}" target="_blank" rel="noreferrer">${escapeHtml(label)}</a>`;
                     }
            
                     function buildGoogleFlightsUrl(origin, destination, departureTime) {
                         const date = toDate(departureTime);
            
                         if (!origin || !destination || !date) {
                             return '';
                         }
            
                         const formattedDate = date.toISOString().slice(0, 10);
                         const query = encodeURIComponent(`${origin} to ${destination} ${formattedDate}`);
            
                         return `https://www.google.com/travel/flights?q=${query}`;
                     }

                    function isValidUrl(url) {
                        try {
                            const parsedUrl = new URL(url);
                            return parsedUrl.protocol === 'http:' || parsedUrl.protocol === 'https:';
                        } catch (error) {
                            return false;
                        }
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

                            if (displayKey && groupedEvents.has(displayKey)) {
                                groupedEvents.get(displayKey).recommendations.push(recommendation);
                            }
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

                    function sortEvents(events) {
                        return [...events].sort((left, right) =>
                            compareOptionalStrings(left.date, right.date)
                            || compareOptionalStrings(normalizeEventTime(left.startTime), normalizeEventTime(right.startTime))
                            || (left.name || '').localeCompare(right.name || '', 'es', { sensitivity: 'base' })
                        );
                    }

                    function sortRecommendations(recommendations) {
                        return [...recommendations].sort((left, right) => {
                            const eventComparison =
                                compareOptionalStrings(left.eventDate, right.eventDate)
                                || compareOptionalStrings(normalizeEventTime(left.eventStartTime), normalizeEventTime(right.eventStartTime))
                                || (left.eventName || '').localeCompare(right.eventName || '', 'es', { sensitivity: 'base' });

                            return eventComparison
                                || compareDateTimes(right.outboundArrivalTime, left.outboundArrivalTime)
                                || compareDateTimes(left.returnDepartureTime, right.returnDepartureTime);
                        });
                    }

                    function compareDisplayEvents(left, right) {
                        return compareOptionalStrings(left.date, right.date)
                            || compareOptionalStrings(normalizeEventTime(left.startTime), normalizeEventTime(right.startTime))
                            || (left.name || '').localeCompare(right.name || '', 'es', { sensitivity: 'base' });
                    }

                    function compareOptionalStrings(left, right) {
                        if (left && right) return left.localeCompare(right);
                        if (left) return -1;
                        if (right) return 1;
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
                        return [...events].sort(compareRepresentativeEvents)[0];
                    }

                    function compareRepresentativeEvents(left, right) {
                        return representativeScore(right) - representativeScore(left)
                            || compareOptionalStrings(normalizeEventTime(left.startTime), normalizeEventTime(right.startTime))
                            || compareOptionalStrings(left.name, right.name);
                    }

                    function representativeScore(event) {
                        let score = 0;
                        const name = normalizeText(event.name);

                        if (!name.includes('vip packages')) score += 4;
                        if (!name.includes('parking')) score += 4;
                        if (name === normalizeText(extractArtistName(event.name))) score += 4;
                        if (event.venue) score += 2;
                        if (event.url) score += 1;

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
                        return events
                            .map(event => normalizeEventTime(event.startTime))
                            .filter(Boolean)
                            .sort((left, right) => left.localeCompare(right))[0] || '';
                    }

                    function chooseLatestCapturedAt(events) {
                        const latestDate = events
                            .map(event => toDate(event.capturedAt))
                            .filter(Boolean)
                            .sort((left, right) => right - left)[0];

                        return latestDate ? latestDate.toISOString() : '';
                    }

                    function choosePreferredUrl(events) {
                        const candidates = events
                            .filter(event => event.url)
                            .filter(event => !isParkingEvent(event))
                            .sort((left, right) => urlScore(right) - urlScore(left));

                        return candidates.length > 0 ? candidates[0].url : '';
                    }

                    function urlScore(event) {
                        const name = normalizeText(event.name);
                        const url = normalizeText(event.url);
                        let score = 0;

                        if (url.includes('ticketmaster.es')) score += 8;
                        if (url.includes('ticketmaster.com')) score += 3;
                        if (!name.includes('vip packages')) score += 5;
                        if (!name.includes('parking')) score += 4;
                        if (normalizeText(event.name) === normalizeText(extractArtistName(event.name))) score += 2;

                        return score;
                    }

                    function choosePreferredValue(values) {
                        return values.find(Boolean) || '';
                    }

                    function matchesEvent(event, options, query) {
                        if (!query) return true;

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
                        return [formattedDate, [start, end].filter(Boolean).join(' - ')].filter(Boolean).join(' | ');
                    }

                    function formatEventDate(date, time) {
                        return [formatLongDate(date), formatClock(time)].filter(Boolean).join(' | ') || 'Fecha no disponible';
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
                        return date ? String(date.getDate()).padStart(2, '0') : '--';
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
                        if (!value) return null;
                        if (value instanceof Date && !Number.isNaN(value.getTime())) return value;

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

                    document.querySelectorAll('.filter-option').forEach(button => {
                        button.addEventListener('click', () => {
                            state.availabilityFilter = button.dataset.filter;

                            document.querySelectorAll('.filter-option').forEach(option => {
                                option.classList.toggle('active', option === button);
                            });

                            renderEvents();
                        });
                    });

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