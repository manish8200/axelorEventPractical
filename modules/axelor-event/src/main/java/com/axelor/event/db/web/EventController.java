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
import java.util.List;

public class EventController extends JpaSupport {

 
  
  public void setEventRegList(ActionRequest request, ActionResponse response) {
	  EventRegistration event = request.getContext().asType(EventRegistration.class);
	  
	  if(request.getContext().getParent() != null) {
	  response.setValue("event", request.getContext().getParent().asType(Event.class));
	  response.setAttr("event", "hidden", true);
  }
  }

  @Inject EventService service;

  public void setDiscountAmount(ActionRequest request, ActionResponse response) {
    Discount discount = request.getContext().asType(Discount.class);
    Event event = request.getContext().getParent().asType(Event.class);
    if (event.getEventFees() != null) {
      BigDecimal discountAmount =
          (discount.getDiscountPercentage().multiply(event.getEventFees()))
              .divide(new BigDecimal(100));
      System.out.println(discountAmount);
      response.setValue("discountAmount", discountAmount);
    } else {
      response.setValue("discountAmount", BigDecimal.ZERO);
    }
  }

  public void setDisCountList(ActionRequest request, ActionResponse response) {
    Event event = request.getContext().asType(Event.class);
    List<Discount> discountList = service.setDiscountAmount(event);
    if(discountList != null) {
    response.setValue("discounts", discountList);
    }else {
    	response.setValue("discounts", null);
    }
  }

  public void setTotalAmount(ActionRequest request, ActionResponse response) {
    Event event = request.getContext().asType(Event.class);
    BigDecimal Amount = service.setTotalAmount(event);
    response.setValue("amountCollected", Amount);
  }

  public void setAmount(ActionRequest request, ActionResponse response) {
    EventRegistration eventReg = request.getContext().asType(EventRegistration.class);
    
    if(request.getContext().getParent() != null) {
    Event event = request.getContext().getParent().asType(Event.class);
    BigDecimal amount = service.setAmount(event, eventReg);

    response.setValue("amount", amount);
  }
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
  
  public void setEventRegistrationAmount(ActionRequest request, ActionResponse response) {
	  EventRegistration eventReg = request.getContext().asType(EventRegistration.class);
	  if(request.getContext().getParent() == null) {
	  BigDecimal amount = service.setEventAmount(eventReg);
	  response.setValue("amount", amount);
  }
  }
}
