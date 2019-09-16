package com.axelor.event.db.web;

import com.axelor.db.JpaSupport;
import com.axelor.event.db.Discount;
import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import com.axelor.event.db.service.EventService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class EventController extends JpaSupport {

  @Inject EventService service;

  public void setDiscountAmount(ActionRequest request, ActionResponse response) {
    Discount discount = request.getContext().asType(Discount.class);
    Event event = request.getContext().getParent().asType(Event.class);

    BigDecimal discountAmount =
        (discount.getDiscountPercentage().multiply(event.getEventFees()))
            .divide(new BigDecimal(100));
    System.out.println(discountAmount);
    response.setValue("discountAmount", discountAmount);
  }

  public void setTotalAmount(ActionRequest request, ActionResponse response) {
    Event event = request.getContext().asType(Event.class);
    BigDecimal Amount = service.setTotalAmount(event);
    response.setValue("amountCollected", Amount);
  }

  public void setAmount(ActionRequest request, ActionResponse response) {
    EventRegistration eventReg = request.getContext().asType(EventRegistration.class);
    Event event = request.getContext().getParent().asType(Event.class);

    BigDecimal amount = service.setAmount(event, eventReg);

    response.setValue("amount", amount);
  }

  public void setTotalDiscountAmount(ActionRequest request, ActionResponse response) {
    Event event = request.getContext().asType(Event.class);
    BigDecimal TotalAmount = service.setTotalAmount(event);

    int eventListSize = event.getEventRegistration().size();
    System.err.println(eventListSize);
    BigDecimal totalAmountwithoutdiscount =
        event.getEventFees().multiply(new BigDecimal(eventListSize));
    BigDecimal totalDiscount = totalAmountwithoutdiscount.subtract(TotalAmount);

    response.setValue("totalDiscount", totalDiscount);
  }
}
