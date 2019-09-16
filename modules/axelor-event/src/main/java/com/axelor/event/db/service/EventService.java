package com.axelor.event.db.service;

import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import java.math.BigDecimal;

public interface EventService {

  BigDecimal setAmount(Event event, EventRegistration eventReg);

  BigDecimal setTotalAmount(Event event);
}
