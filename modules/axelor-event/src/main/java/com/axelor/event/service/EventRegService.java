package com.axelor.event.service;

import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import java.math.BigDecimal;

public interface EventRegService {

  BigDecimal setAmount(Event event, EventRegistration eventReg);

  BigDecimal setEventAmount(EventRegistration eventReg);

  EventRegistration calculation(EventRegistration eventRegistration);
}
