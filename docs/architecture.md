# Documentación de Arquitectura y Diagramas de Clases

Este documento detalla la arquitectura de software del sistema, enfocándose especialmente en el módulo central `business-unit`. La aplicación sigue un enfoque arquitectónico guiado por eventos (EDA - Event-Driven Architecture) totalmente desacoplado mediante un broker de mensajería (ActiveMQ), resistencia centralizada en un Datamart relacional (SQLite) y exposición dinámica de datos a través de una interfaz web integrada por una API REST (Javalin).

---

## 1. Arquitectura General y Flujo de Control (`business-unit`)

Este diagrama representa el núcleo de control y la orquestación de la Unidad de Negocio. Muestra cómo coordinan sus acciones la interfaz de la API web (`RestApi`), el cargador por lotes del histórico (`EventStoreLoader`) y el consumidor asíncrono en tiempo real (`JmsEventConsumer`), delegando el almacenamiento y la consulta de la información en la Fachada única del repositorio central.

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

### Argumentos clave para la defensa ante el tribunal:
* **Desacoplamiento asíncrono:** El componente `JmsEventConsumer` reacciona de manera pura a los tópicos del broker sin conocer el origen de los datos. Los capturadores externos (`flight-provider` y `ticketmaster-provider`) son completamente inmutables e independientes del destino.
* **Separación de Conceptos:** La clase `EventMessageParser` aísla de forma exclusiva la lógica de deserialización e interpretación de los payloads JSON, evitando que las clases de negocio o persistencia tengan que lidiar con dependencias directas de librerías de conversión.

---

## 2. Capa de Persistencia (Patrón Repository y Fachada Datamart)

Este diagrama detalla la arquitectura del almacenamiento relacional de SQLite dentro de la Unidad de Negocio. Implementa el patrón estructural **Repository** para subdividir las tareas SQL por tabla, agrupándolas formalmente bajo una estructura de **Fachada** (`DatamartRepository`) para simplificar su consumo externo.

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

### Argumentos clave para la defensa ante el tribunal:
* **Patrón Fachada (Facade):** Ningún elemento de control de la aplicación (`RestApi` o `RecommendationService`) tiene visibilidad directa de las sentencias SQL ni de los repositorios atómicos de las tablas. Esto permite sustituir el motor de base de datos en el futuro modificando únicamente este paquete cerrado.
* **Ciclo de vida centralizado:** La clase `DatabaseManager` asume de forma única la responsabilidad del mapeo inicial DDL (`CREATE TABLE IF NOT EXISTS`) y de proveer conexiones limpias a través de JDBC mediante bloques robustos de control de recursos.

---

## 3. Capa de Modelo (Entidades Inmutables / POJOs)

Representa los Objetos de Transferencia de Datos (`Plain Old Java Objects`) que fluyen verticalmente a través de todas las capas del sistema. Se ha priorizado el principio de **Inmutabilidad** declarando sus atributos como de lectura exclusiva (`private final`).

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

### Argumentos clave para la defensa ante el tribunal:
* **Thread-Safety implícito:** Al carecer de métodos modificadores de estado (`setters`), estos objetos son inherentemente seguros frente a accesos concurrentes de hilos paralelos. Esto es crítico dado que el servidor HTTP (`Javalin`) atiende peticiones de clientes simultáneamente mientras el consumidor ActiveMQ procesa flujos de entrada.
* **Alineación Conceptual:** La recomendación no almacena referencias de memoria vivas a los objetos para evitar acoplamientos rígidos en el almacenamiento persistente; en su lugar, mapea claves descriptivas e identificadores formando una estructura óptima para su rápido renderizado en la interfaz web.