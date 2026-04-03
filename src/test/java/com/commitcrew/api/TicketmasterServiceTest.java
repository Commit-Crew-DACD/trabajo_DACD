package com.commitcrew.api;

import com.commitcrew.model.Event;
import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketmasterServiceTest {

    private static final String FAKE_JSON = """
        {
          "_embedded": {
            "events": [
              {
                "id": "1",
                "name": "Concierto Test",
                "url": "https://test.com",
                "dates": { "start": { "localDate": "2026-05-01" } },
                "_embedded": {
                  "venues": [
                    { "name": "Palacio Deportes", "city": { "name": "Madrid" } }
                  ]
                }
              }
            ]
          }
        }
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
    void testGetEventsMadrid() throws Exception {
        TicketmasterService service = new TicketmasterService(buildMockClient(), "FAKE_KEY");
        List<Event> events = service.getEvents("Madrid");

        assertEquals(1, events.size());
        assertEquals("Concierto Test", events.get(0).getName());
        assertEquals("Madrid", events.get(0).getCity());
    }

    @Test
    void testGetEventsBarcelona() throws Exception {
        TicketmasterService service = new TicketmasterService(buildMockClient(), "FAKE_KEY");
        List<Event> events = service.getEvents("Barcelona");

        assertFalse(events.isEmpty());
        assertEquals("Palacio Deportes", events.get(0).getVenue());
    }
}