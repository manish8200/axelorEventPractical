package com.axelor.event.db.service;

import com.axelor.event.db.Discount;
import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class EventServiceimpl implements EventService {

  @Override
  public BigDecimal setAmount(Event event, EventRegistration eventReg) {
    // TODO Auto-generated method stub
    if (event.getDiscounts() != null) {
      LocalDateTime evenRegDate = eventReg.getRegistrationDate();
      LocalDateTime eventCloseDate = event.getRegistrationClose();
      BigDecimal discountAmount = BigDecimal.ZERO;
      BigDecimal EventAmount = event.getEventFees();
      List<Discount> discount = event.getDiscounts();

      for (Discount discount2 : discount) {
        int differenceOfDays = eventCloseDate.getDayOfMonth() - evenRegDate.getDayOfMonth();
        System.err.println(differenceOfDays);
        int discountbeforedays = discount2.getBeforeDays();
        if (differenceOfDays >= discountbeforedays) {
          discountAmount = EventAmount.subtract(discount2.getDiscountAmount());
          break;
        } else {
          discountAmount = event.getEventFees();
        }
      }
      return discountAmount;
    } else {
      return BigDecimal.ZERO;
    }
  }

  @Override
  public BigDecimal setTotalAmount(Event event) {
    // TODO Auto-generated method stub
    BigDecimal TotalAmount = BigDecimal.ZERO;
    List<EventRegistration> evntReg = event.getEventRegistration();
    for (EventRegistration eventRegistration : evntReg) {
      TotalAmount = TotalAmount.add(eventRegistration.getAmount());
    }

    return TotalAmount;
  }
}
