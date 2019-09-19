package com.axelor.event.service;

import com.axelor.event.db.Discount;
import com.axelor.event.db.Event;
import java.math.BigDecimal;
import java.util.List;

public interface EventService {

  BigDecimal setTotalAmount(Event event);

  List<Discount> setDiscountAmount(Event event);
}
