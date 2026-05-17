# Documentación de Arquitectura y Diseño Técnico

Este documento describe la especificación técnica de la arquitectura de software implementada, centrada en el módulo de procesamiento `business-unit`. El sistema adopta un enfoque orientado a eventos (EDA) acoplado a través de un broker de mensajería asíncrona, un Datamart relacional centralizado y un servidor web integrado para la exposición de servicios.

---

## 1. Arquitectura de Control y Flujo del Sistema (`business-unit`)

El siguiente esquema representa la estructura de hilos y la lógica de control del componente principal. Ilustra la interacción simultánea entre la API REST (`RestApi`), el módulo de lectura histórica de archivos (`EventStoreLoader`) y el receptor de eventos de mensajería (`JmsEventConsumer`), interactuando de forma unificada sobre la fachada del repositorio.

```mermaid
classDiagram
    class Main {
        +main(String[] args)$ void
    }

    class JmsEventConsumer {
        -String brokerUrl
        -String[] topics
        -Connection connection
        -Session session
        +start() void
        -processMessage(String topicName, String json) void
    }

    class RestApi {
        -int port
        +start() void
    }

    class EventStoreLoader {
        -Path eventStorePath
        +load() void
        -loadFile(Path file) void
        -loadLine(String json) void
    }

    class EventMessageParser {
        +isEventMessage(String json) boolean
        +isFlightMessage(String json) boolean
        +parseEvent(String json) Event
        +parseFlight(String json) Flight
    }

    class RecommendationService {
        +rebuildRecommendations() void
        -arrivesBeforeEventWithMargin() boolean
        -departsAfterEventWithMargin() boolean
    }

    class DatamartRepository {
        +saveEvent(Event event) void
        +saveFlight(Flight flight) void
        +saveRecommendation(Recommendation r) void
        +findAllEvents() List~Event~
        +findAllFlights() List~Flight~
        +findAllRecommendations() List~Recommendation~
    }

    %% Relaciones de control
    Main --> DatamartRepository : inicializa
    Main --> EventMessageParser : inicializa
    Main --> EventStoreLoader : orquesta
    Main --> RecommendationService : orquesta
    Main --> JmsEventConsumer : arranca
    Main --> RestApi : expone

    JmsEventConsumer --> EventMessageParser : traduce JSON
    JmsEventConsumer --> DatamartRepository : persiste
    JmsEventConsumer --> RecommendationService : dispara recalculación

    EventStoreLoader --> EventMessageParser : lee histórico
    EventStoreLoader --> DatamartRepository : carga lote

    RecommendationService --> DatamartRepository : consulta e inyecta

    RestApi --> DatamartRepository : consulta datos
    RestApi --> RecommendationService : fuerza recalculación
```

### Especificaciones de Diseño:
* **Desacoplamiento de Red:** Los productores externos (`flight-provider` y `ticketmaster-provider`) interactúan únicamente con el Broker de ActiveMQ, garantizando que el origen de datos y el motor de procesamiento sean independientes.
* **Procesamiento de Payloads:** La lógica de análisis e interpretación de cadenas JSON se centraliza en `EventMessageParser`, encapsulando los esquemas externos fuera de las clases de almacenamiento.

---

## 2. Capa de Persistencia (Patrón Repository y Fachada Datamart)

Este diagrama detalla el diseño estructural del almacenamiento local sobre SQLite. Se organiza mediante la segregación de interfaces por tabla (Patrón Repository) y se consolida bajo un único punto de acceso unificado (Patrón Facade).

```mermaid
classDiagram
    class DatabaseManager {
        -String URL
        +getConnection() Connection
        -initDatabase() void
    }

    class DatamartRepository {
        -EventRepository eventRepository
        -FlightRepository flightRepository
        -RecommendationRepository recommendationRepository
        -ConfigRepository configRepository
    }

    class EventRepository {
        +save(Event event) void
        +findAll() List~Event~
        +clear() void
        +count() int
    }

    class FlightRepository {
        +save(Flight flight) void
        +findAll() List~Flight~
        +clear() void
        +count() int
    }

    class RecommendationRepository {
        +save(Recommendation r) void
        +findAll() List~Recommendation~
        +clear() void
        +count() int
    }

    class ConfigRepository {
        +getConfig() RecommendationConfig
        +saveConfig(RecommendationConfig config) void
    }

    %% Relaciones estructurales de persistencia
    DatamartRepository *--> DatabaseManager : comparte conexión
    DatamartRepository *--> EventRepository : delega eventos
    DatamartRepository *--> FlightRepository : delega vuelos
    DatamartRepository *--> RecommendationRepository : delega recomendaciones
    DatamartRepository *--> ConfigRepository : delega configuración

    EventRepository --> DatabaseManager : solicita Connection
    FlightRepository --> DatabaseManager : solicita Connection
    RecommendationRepository --> DatabaseManager : solicita Connection
    ConfigRepository --> DatabaseManager : solicita Connection
```

### Especificaciones de Persistencia:
* **Abstracción del Motor:** El uso de `DatamartRepository` como fachada oculta la implementación interna de JDBC y las sentencias SQL de las capas superiores de lógica de negocio (`RestApi` y `RecommendationService`).
* **Ciclo de Vida de Conexiones:** `DatabaseManager` asume de forma única la apertura de flujos y la verificación/inicialización de las tablas de datos (`DDL`) durante el ciclo de arranque.

---

## 3. Capa de Modelo (Entidades Inmutables)

Estructura de las clases POJO (`Plain Old Java Objects`) utilizadas para el transporte horizontal de información entre módulos. El diseño prioriza restricciones de inmutabilidad en las propiedades de los datos mapeados.

```mermaid
classDiagram
    class Event {
        -String id
        -String name
        -String city
        -String venue
        -String date
        -String startTime
        -String url
        -String capturedAt
        +getId() String
        +getName() String
        +getCity() String
    }

    class Flight {
        -String flightNumber
        -String origin
        -String destination
        -String destinationCity
        -String date
        -String scheduledTime
        -String estimatedTime
        -String status
        -String airline
        +getFlightNumber() String
        +getOrigin() String
        +getDestination() String
    }

    class Recommendation {
        -String eventId
        -String eventName
        -String eventCity
        -String outboundFlightNumber
        -String returnFlightNumber
        -String capturedAt
        +getEventId() String
        +getOutboundFlightNumber() String
    }

    class RecommendationConfig {
        -String originAirport
        -int outboundMarginHours
        -int returnMarginHours
        -int defaultEventDurationHours
        +getOriginAirport() String
        +getOutboundMarginHours() int
    }

    %% Relaciones conceptuales de negocio
    Recommendation "1" *--> "1" Event : se genera para
    Recommendation "1" *--> "1" Flight : incluye ida
    Recommendation "1" *--> "1" Flight : incluye vuelta
```

### Especificaciones de Modelo:
* **Estructura Segura (Thread-Safety):** Al prescindir de métodos modificadores (`setters`), los objetos de datos son intrínsecamente estables ante llamadas concurrentes asíncronas generadas por los hilos concurrentes del servidor web o los consumidores de la cola.
* **Estructura Desacoplada:** El modelo evita enlaces directos por puntero entre instancias de memoria vivas dentro del Datamart; las entidades se vinculan mediante indexación de identificadores descriptivos.