package org.ulpgc.dacd.control;

import org.ulpgc.dacd.model.Event;
import java.util.List;

public interface EventProvider {
    List<Event> fetchEvents(String city) throws Exception;
}