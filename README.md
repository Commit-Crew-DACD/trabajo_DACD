# Sistema de Recomendación de Viajes para Eventos Musicales

Este proyecto implementa una infraestructura distribuida y modular orientada a la ingesta, procesamiento, persistencia y exposición de recomendaciones de viajes optimizadas, combinando la oferta de conciertos musicales con la disponibilidad logística de vuelos comerciales.

Desarrollado como proyecto final para la asignatura **Desarrollo de Aplicaciones para Ciencia de Datos (DACD)** de la Universidad de Las Palmas de Gran Canaria.

---

## 1. Propuesta de Valor

La plataforma resuelve la complejidad de coordinar manualmente los itinerarios de transporte en torno a eventos culturales masivos. Su propósito es automatizar el cruce de datos en tiempo real e histórico de conciertos internacionales con los flujos de vuelos comerciales con origen o destino en el Aeropuerto de Gran Canaria (LPA).

El sistema calcula y genera de forma autónoma **tuplas optimizadas de viaje (Evento + Vuelo de Ida + Vuelo de Vuelta)** bajo ventanas temporales parametrizables. Esto permite transformar flujos masivos de datos dispersos en opciones logísticas inmediatas y seguras para el usuario final.

---

## 2. Estructura del Proyecto y Arquitectura

El ecosistema de la aplicación se divide en tres módulos independientes completamente desacoplados:

* **`flight-provider`:** Componente encargado de la captura de datos aéreos y de la emisión de eventos de vuelos hacia el broker.
* **`ticketmaster-provider`:** Componente encargado de consumir la API de Ticketmaster para extraer e ingestar la programación de conciertos.
* **`business-unit`:** Núcleo central del sistema. Integra el consumidor asíncrono JMS, el cargador de datos históricos (*event-store*), el motor analítico de recomendaciones y la API web de exposición.

### 📊 Especificación de Diagramas de Clases
Toda la documentación técnica del flujo de control, la arquitectura modular de hilos y el modelado UML de persistencia y entidades se encuentra centralizada en:
👉 **[Documentación de Arquitectura y Diagramas de Clases](./docs/architecture.md)**

---

## 3. Justificación Tecnológica

### Orígenes de Datos (APIs)
* **Ticketmaster API:** Proporciona un esquema JSON normalizado y unificado a nivel global (identificadores únicos, salas, geolocalización y marcas de tiempo precisas), garantizando la estabilidad de la ingesta frente a plataformas locales de venta de entradas.
* **Flujos de Vuelos:** Proveen información fidedigna de la disponibilidad y conectividad aérea de la isla de Gran Canaria en las ventanas temporales analizadas.

### Datamart Relacional (SQLite)
La persistencia de la Unidad de Negocio se realiza mediante un motor **SQLite** relacional embebido por los siguientes motivos:
1. **Integridad Referencial:** Garantiza mediante restricciones de clave foránea (*Foreign Keys*) que no existan recomendaciones huérfanas si un vuelo o evento base es modificado o cancelado.
2. **Consultas Combinatorias:** Permite ejecutar operaciones `JOIN` complejas y filtrado analítico por rangos de fecha y hora sobre índices en memoria local de manera eficiente.
3. **Portabilidad:** Centraliza el Datamart en un único archivo local (`business-unit.db`), eliminando la latencia de red y la sobrecarga de despliegue de un servidor de bases de datos externo.

---

## 4. Patrones y Principios de Diseño Aplicados

* **Arquitectura Dirigida por Eventos (EDA):** Comunicación totalmente asíncrona mediante un broker **JMS (Apache ActiveMQ)**. Los proveedores y la unidad de procesamiento no se conocen entre sí, lo que garantiza el aislamiento ante caídas o picos de carga.
* **Patrón Repository:** Segregación de la lógica SQL por tablas en clases dedicadas (`EventRepository`, `FlightRepository`, `RecommendationRepository`), aislando las consultas JDBC del resto de la aplicación.
* **Patrón Fachada (Facade):** La clase `DatamartRepository` unifica el acceso a todos los sub-repositorios, exponiendo una interfaz limpia hacia los servicios de negocio y la API web.
* **Inmutabilidad (Thread-Safety):** Las entidades del modelo se definen como objetos inmutables (`POJOs`) con propiedades de lectura exclusiva (`private final`) y sin métodos modificadores (`setters`). Esto asegura la estabilidad del sistema ante accesos concurrentes de los hilos del servidor HTTP y la inyección de datos del consumidor JMS.

---

## 5. Requisitos y Guía de Ejecución

### Prerrequisitos
* **Java Development Kit (JDK):** Versión 21 o superior.
* **Apache Maven:** Configurado en el entorno.
* **Apache ActiveMQ:** Versión 5.x o superior en ejecución (Puerto `61616`).

### Compilación
Desde la raíz del proyecto (donde reside el `pom.xml` padre), ejecute:
```bash
mvn clean compile
```

### Orden de Ejecución
1. **ActiveMQ:** Asegúrese de tener el broker de mensajería iniciado (`activemq start` o servicio de Windows activo).
2. **Unidad de Negocio:** Ejecute la clase principal `org.ulpgc.dacd.Main` del módulo `business-unit`. Esto levantará el Datamart SQLite, procesará el histórico de datos, activará el servidor web y abrirá la escucha JMS.
3. **Proveedores:** Inicie las clases principales de los módulos `flight-provider` y `ticketmaster-provider` para activar la ingesta en tiempo real.

---

## 🌐 6. Interfaz REST y Ejemplos de Uso

La aplicación expone sus servicios a través de un servidor **Javalin** integrado en el puerto `8080`.

### 1. Obtener todas las recomendaciones
* **Ruta:** `GET http://localhost:8080/recommendations`
* **Formato de Respuesta (JSON):**
```json
[
  {
    "eventId": "Z7rGza1AdOk3v",
    "eventName": "Bad Bunny - DeBÍ TiRAR MáS FOToS World Tour",
    "eventCity": "Madrid",
    "eventDate": "2026-05-30",
    "eventStartTime": "21:00",
    "outboundFlightNumber": "UX9059",
    "outboundAirline": "Air Europa",
    "outboundOrigin": "LPA",
    "outboundDestination": "MAD",
    "outboundDepartureTime": "2026-05-30 07:00",
    "returnFlightNumber": "UX9160R",
    "returnAirline": "Air Europa",
    "returnOrigin": "MAD",
    "returnDestination": "LPA",
    "returnDepartureTime": "2026-05-31 12:30",
    "capturedAt": "2026-05-17T15:20:11.450Z"
  }
]
```

### 2. Filtrar recomendaciones por ciudad
* **Ruta:** `GET http://localhost:8080/recommendations?city=Madrid`

### 3. Consultar la configuración del algoritmo
* **Ruta:** `GET http://localhost:8080/config`
