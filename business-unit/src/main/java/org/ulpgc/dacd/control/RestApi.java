package org.ulpgc.dacd.control;

import io.javalin.Javalin;
import org.ulpgc.dacd.model.RecommendationConfig;
import org.ulpgc.dacd.storage.DatamartRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RestApi {
    private static final String INDEX_HTML_RESOURCE = "public/index.html";
    private static final String INDEX_HTML = loadIndexHtml();

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

    private static String loadIndexHtml() {
        try (InputStream inputStream = RestApi.class.getClassLoader().getResourceAsStream(INDEX_HTML_RESOURCE)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing frontend resource: " + INDEX_HTML_RESOURCE);
            }

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load frontend resource: " + INDEX_HTML_RESOURCE, e);
        }
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
