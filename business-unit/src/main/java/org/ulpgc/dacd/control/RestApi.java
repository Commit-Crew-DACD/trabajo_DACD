package org.ulpgc.dacd.control;

import io.javalin.Javalin;
import org.ulpgc.dacd.model.RecommendationConfig;
import org.ulpgc.dacd.storage.DatamartRepository;

public class RestApi {
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
            config.routes.get("/", context -> context.result("""
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Recomendador vuelo-evento</title>
                        <style>
                            * {
                                box-sizing: border-box;
                            }

                            body {
                                margin: 0;
                                font-family: Arial, sans-serif;
                                background: #f4f6f8;
                                color: #1f2933;
                            }

                            header {
                                background: #1f2933;
                                color: white;
                                padding: 24px 32px;
                            }

                            header h1 {
                                margin: 0 0 8px;
                                font-size: 28px;
                            }

                            header p {
                                margin: 0;
                                color: #cbd5e1;
                            }

                            main {
                                padding: 24px 32px;
                                max-width: 1200px;
                                margin: 0 auto;
                            }

                            .stats {
                                display: grid;
                                grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
                                gap: 16px;
                                margin-bottom: 24px;
                            }

                            .stat, .config-panel {
                                background: white;
                                border: 1px solid #d9e2ec;
                                border-radius: 8px;
                                padding: 16px;
                            }

                            .stat span {
                                display: block;
                                color: #64748b;
                                font-size: 13px;
                                margin-bottom: 6px;
                            }

                            .stat strong {
                                font-size: 28px;
                            }

                            .config-panel {
                                margin-bottom: 24px;
                            }

                            .config-panel h2 {
                                margin: 0 0 16px;
                                font-size: 20px;
                            }

                            .config-grid {
                                display: grid;
                                grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
                                gap: 12px;
                                align-items: end;
                            }

                            label {
                                display: block;
                                font-size: 13px;
                                color: #475569;
                                margin-bottom: 6px;
                            }

                            input {
                                width: 100%;
                                border: 1px solid #cbd5e1;
                                border-radius: 6px;
                                padding: 10px 12px;
                                font-size: 14px;
                            }

                            .toolbar {
                                display: flex;
                                flex-wrap: wrap;
                                gap: 12px;
                                align-items: center;
                                margin-bottom: 16px;
                            }

                            .toolbar input {
                                max-width: 360px;
                            }

                            button {
                                border: 0;
                                border-radius: 6px;
                                background: #2563eb;
                                color: white;
                                padding: 10px 14px;
                                font-size: 14px;
                                cursor: pointer;
                            }

                            button:hover {
                                background: #1d4ed8;
                            }

                            .table-wrapper {
                                background: white;
                                border: 1px solid #d9e2ec;
                                border-radius: 8px;
                                overflow-x: auto;
                            }

                            table {
                                width: 100%;
                                border-collapse: collapse;
                                min-width: 960px;
                            }

                            th, td {
                                padding: 12px;
                                border-bottom: 1px solid #e5e7eb;
                                text-align: left;
                                vertical-align: top;
                                font-size: 14px;
                            }

                            th {
                                background: #f8fafc;
                                color: #475569;
                                font-size: 12px;
                                text-transform: uppercase;
                                letter-spacing: 0.04em;
                            }

                            tr:hover {
                                background: #f9fafb;
                            }

                            .muted {
                                color: #64748b;
                                font-size: 13px;
                            }

                            .empty {
                                padding: 24px;
                                color: #64748b;
                            }

                            .message {
                                margin-top: 12px;
                                font-size: 14px;
                                color: #166534;
                            }
                        </style>
                    </head>
                    <body>
                        <header>
                            <h1>Recomendador vuelo-evento</h1>
                            <p>Datamart de combinaciones compatibles entre vuelos y eventos.</p>
                        </header>

                        <main>
                            <section class="stats">
                                <div class="stat">
                                    <span>Eventos</span>
                                    <strong id="eventsCount">-</strong>
                                </div>
                                <div class="stat">
                                    <span>Vuelos</span>
                                    <strong id="flightsCount">-</strong>
                                </div>
                                <div class="stat">
                                    <span>Recomendaciones</span>
                                    <strong id="recommendationsCount">-</strong>
                                </div>
                                <div class="stat">
                                    <span>Configuración actual</span>
                                    <div id="configInfo" class="muted">Cargando...</div>
                                </div>
                            </section>

                            <section class="config-panel">
                                <h2>Configurar márgenes</h2>
                                <div class="config-grid">
                                    <div>
                                        <label for="originAirport">Aeropuerto origen</label>
                                        <input id="originAirport" type="text" value="LPA">
                                    </div>
                                    <div>
                                        <label for="outboundMarginHours">Margen ida (horas)</label>
                                        <input id="outboundMarginHours" type="number" min="0" value="3">
                                    </div>
                                    <div>
                                        <label for="returnMarginHours">Margen vuelta (horas)</label>
                                        <input id="returnMarginHours" type="number" min="0" value="2">
                                    </div>
                                    <div>
                                        <label for="defaultEventDurationHours">Duración evento (horas)</label>
                                        <input id="defaultEventDurationHours" type="number" min="1" value="3">
                                    </div>
                                    <div>
                                        <button onclick="saveConfig()">Guardar y recalcular</button>
                                    </div>
                                </div>
                                <div id="configMessage" class="message"></div>
                            </section>

                            <section class="toolbar">
                                <input id="searchInput" type="search" placeholder="Filtrar por evento, ciudad, aeropuerto o aerolínea">
                                <button onclick="loadData()">Actualizar</button>
                            </section>

                            <section class="table-wrapper">
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Evento</th>
                                            <th>Fecha</th>
                                            <th>Ida</th>
                                            <th>Vuelta</th>
                                            <th>Capturado</th>
                                        </tr>
                                    </thead>
                                    <tbody id="recommendationsBody">
                                        <tr>
                                            <td colspan="5" class="empty">Cargando recomendaciones...</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </section>
                        </main>

                        <script>
                            let recommendations = [];

                            async function loadData() {
                                const stamp = Date.now();
                    
                                const [statsResponse, configResponse, recommendationsResponse] = await Promise.all([
                                    fetch(`/api/stats?t=${stamp}`, { cache: 'no-store' }),
                                    fetch(`/api/config?t=${stamp}`, { cache: 'no-store' }),
                                    fetch(`/api/recommendations?t=${stamp}`, { cache: 'no-store' })
                                ]);
                    
                                const stats = await statsResponse.json();
                                const config = await configResponse.json();
                                recommendations = await recommendationsResponse.json();
                    
                                document.getElementById('eventsCount').textContent = stats.events;
                                document.getElementById('flightsCount').textContent = stats.flights;
                                document.getElementById('recommendationsCount').textContent = stats.recommendations;
                    
                                setConfigValues(config);
                                renderRecommendations();
                            }

                            function setConfigValues(config) {
                                document.getElementById('configInfo').textContent =
                                    `Origen ${config.originAirport} · Ida ${config.outboundMarginHours}h · Vuelta ${config.returnMarginHours}h · Duración ${config.defaultEventDurationHours}h`;

                                document.getElementById('originAirport').value = config.originAirport;
                                document.getElementById('outboundMarginHours').value = config.outboundMarginHours;
                                document.getElementById('returnMarginHours').value = config.returnMarginHours;
                                document.getElementById('defaultEventDurationHours').value = config.defaultEventDurationHours;
                            }

                            async function saveConfig() {
                                 const message = document.getElementById('configMessage');
                                 message.textContent = 'Recalculando recomendaciones...';
                    
                                 const payload = {
                                     originAirport: document.getElementById('originAirport').value.trim().toUpperCase(),
                                     outboundMarginHours: Number(document.getElementById('outboundMarginHours').value),
                                     returnMarginHours: Number(document.getElementById('returnMarginHours').value),
                                     defaultEventDurationHours: Number(document.getElementById('defaultEventDurationHours').value)
                                 };
                    
                                 const response = await fetch('/api/config', {
                                     method: 'POST',
                                     headers: {
                                         'Content-Type': 'application/json'
                                     },
                                     cache: 'no-store',
                                     body: JSON.stringify(payload)
                                 });
                    
                                 if (!response.ok) {
                                     message.textContent = 'No se pudo guardar la configuración.';
                                     return;
                                 }
                    
                                 await loadData();
                                 message.textContent = 'Configuración guardada. Recomendaciones recalculadas.';
                             }

                            function renderRecommendations() {
                                const query = document.getElementById('searchInput').value.toLowerCase();
                                const body = document.getElementById('recommendationsBody');

                                const filtered = recommendations.filter(item => {
                                       return [
                                           item.eventName,
                                           item.eventCity,
                                           item.eventDate,
                                           item.outboundAirline,
                                           item.returnAirline,
                                           item.outboundFlightNumber,
                                           item.returnFlightNumber,
                                           item.outboundOrigin,
                                           item.outboundDestination,
                                           item.returnOrigin,
                                           item.returnDestination
                                       ].join(' ').toLowerCase().includes(query);
                                   });
                    
                                if (filtered.length === 0) {
                                    body.innerHTML = '<tr><td colspan="5" class="empty">No hay recomendaciones para ese filtro.</td></tr>';
                                    return;
                                }

                                body.innerHTML = filtered.map(item => `
                                    <tr>
                                        <td>
                                            <strong>${escapeHtml(item.eventName)}</strong>
                                            <div class="muted">${escapeHtml(item.eventCity)}</div>
                                        </td>
                                        <td>
                                            ${escapeHtml(item.eventDate)}
                                            <div class="muted">${escapeHtml(item.eventStartTime)} - ${escapeHtml(item.eventEndTime)}</div>
                                        </td>
                                        <td>
                                            <strong>${escapeHtml(item.outboundOrigin)} → ${escapeHtml(item.outboundDestination)}</strong>
                                            <div>${escapeHtml(item.outboundAirline)} ${escapeHtml(item.outboundFlightNumber)}</div>
                                            <div class="muted">Salida ${escapeHtml(item.outboundDepartureTime)}</div>
                                            <div class="muted">Llegada ${escapeHtml(item.outboundArrivalTime)}</div>
                                        </td>
                                        <td>
                                            <strong>${escapeHtml(item.returnOrigin)} → ${escapeHtml(item.returnDestination)}</strong>
                                            <div>${escapeHtml(item.returnAirline)} ${escapeHtml(item.returnFlightNumber)}</div>
                                            <div class="muted">Salida ${escapeHtml(item.returnDepartureTime)}</div>
                                        </td>
                                        <td class="muted">${escapeHtml(item.capturedAt)}</td>
                                    </tr>
                                `).join('');
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

                            document.getElementById('searchInput').addEventListener('input', renderRecommendations);
                            loadData();
                        </script>
                    </body>
                    </html>
                    """).contentType("text/html"));

            config.routes.get("/api/recommendations",
                    context -> context.json(repository.findAllRecommendations()));

            config.routes.get("/api/config",
                    context -> context.json(repository.getConfig()));

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
