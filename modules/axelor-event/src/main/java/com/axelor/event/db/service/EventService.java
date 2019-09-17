package com.axelor.event.db.service;

import com.axelor.event.db.Discount;
import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import java.math.BigDecimal;
import java.util.List;

public interface EventService {

  BigDecimal setAmount(Event event, EventRegistration eventReg);

  BigDecimal setTotalAmount(Event event);

  List<Discount> setDiscountAmount(Event event);
  
 /* List<EventRegistration> setEventReg(Event event);
*/}
