package com.axelor.event.service;

import com.axelor.event.db.Discount;
import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EventServiceimpl implements EventService {

  @Override
  public BigDecimal setTotalAmount(Event event) {
    // TODO Auto-generated method stub
    BigDecimal TotalAmount = BigDecimal.ZERO;
    List<EventRegistration> evntRegList = event.getEventRegistrationList();
    for (EventRegistration eventRegistration : evntRegList) {
      TotalAmount = TotalAmount.add(eventRegistration.getAmount());
    }

    return TotalAmount;
  }

  @Override
  public List<Discount> setDiscountAmount(Event event) {
    // TODO Auto-generated method stub
    BigDecimal discountPercentage = BigDecimal.ZERO;
    BigDecimal DiscountAmount = BigDecimal.ZERO;
    List<Discount> NewdiscountList = new ArrayList<Discount>();
    System.err.println(event.getDiscountList());
    if (event.getDiscountList() != null) {
      List<Discount> discountList = event.getDiscountList();
      for (Discount discount : discountList) {

        discountPercentage = discount.getDiscountPercentage();
        DiscountAmount = event.getEventFees().multiply(discountPercentage);

        discount.setDiscountAmount(DiscountAmount);
        NewdiscountList.add(discount);
      }
      return NewdiscountList;
    } else {
      return NewdiscountList;
    }
  }
}
