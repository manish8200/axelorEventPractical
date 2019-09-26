package com.axelor.csv.script;

import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import com.google.inject.persist.Transactional;
import java.util.Map;

public class ImportEventRegistraion {

  @Transactional
  public Object ImportEvent(Object bean, Map<String, Object> values) {
    assert bean instanceof EventRegistration;
    int successfullImports = 0;
    EventRegistration eventRegistration = (EventRegistration) bean;
    // System.out.println();
    Event event = eventRegistration.getEvent();
    int totalEntry = event.getTotalEntry();
    if (event.getTotalEntry() < event.getCapacity()) {
      successfullImports++;
      totalEntry++;
    }
    // System.err.println(successfullImports);
    event.setTotalEntry(totalEntry);
    System.err.println(event.getTotalEntry());
    if (successfullImports > 0) {
      return eventRegistration;
    }
    return null;
  }
}
