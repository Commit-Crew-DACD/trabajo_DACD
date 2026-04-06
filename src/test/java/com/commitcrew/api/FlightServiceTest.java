package com.commitcrew.api;

import com.commitcrew.model.Flight;
import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlightServiceTest {

    private static final String FAKE_JSON = """
            [
              {
                "numVuelo": "1234",
                "iataAena": "LPA",
                "fecha": "04/04/2026",
                "horaProgramada": "10:00:00",
                "fechaEstimada": "04/04/2026",
                "horaEstimada": "10:15:00",
                "iataOtro": "MAD",
                "estado": "SAL",
                "tipoVuelo": "S",
                "terminal": "1",
                "ciudadIataOtro": "MADRID",
                "nombreCompania": "Iberia"
              }
            ]
            """;

    private Call.Factory buildMockClient() throws Exception {
        Call.Factory mockFactory = mock(Call.Factory.class);
        Call mockCall = mock(Call.class);

        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("https://fake.url").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(FAKE_JSON, MediaType.get("application/json")))
                .build();

        when(mockFactory.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        return mockFactory;
    }

    @Test
    void testGetSalidas() throws Exception {
        FlightService service = new FlightService(buildMockClient());
        List<Flight> flights = service.getFlights("LPA", "S");

        assertEquals(1, flights.size());
        assertEquals("1234", flights.get(0).getFlightNumber());
        assertEquals("LPA", flights.get(0).getOrigin());
        assertEquals("MAD", flights.get(0).getDestination());
        assertEquals("Iberia", flights.get(0).getAirline());
        assertEquals("S", flights.get(0).getFlightType());
    }

    @Test
    void testGetLlegadas() throws Exception {
        FlightService service = new FlightService(buildMockClient());
        List<Flight> flights = service.getFlights("LPA", "L");

        assertFalse(flights.isEmpty());
        assertEquals("MADRID", flights.get(0).getDestinationCity());
        assertEquals("10:00:00", flights.get(0).getScheduledTime());
        assertEquals("10:15:00", flights.get(0).getEstimatedTime());
    }
}