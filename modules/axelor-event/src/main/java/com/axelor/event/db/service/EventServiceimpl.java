package com.axelor.event.db.service;

import com.axelor.event.db.Discount;
import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventServiceimpl implements EventService {

  @Override
  public BigDecimal setAmount(Event event, EventRegistration eventReg) {
    // TODO Auto-generated method stub
    if (event.getDiscounts() != null && event.getRegistrationClose() != null) {
      LocalDateTime evenRegDate = eventReg.getRegistrationDate();
      LocalDateTime eventCloseDate = event.getRegistrationClose();
      BigDecimal discountAmount = BigDecimal.ZERO;
      BigDecimal EventAmount = event.getEventFees();
      List<Discount> discountList = event.getDiscounts();

      for (Discount discount : discountList) {
        int differenceOfDays = eventCloseDate.getDayOfMonth() - evenRegDate.getDayOfMonth();
        System.err.println(differenceOfDays);
        int discountbeforedays = discount.getBeforeDays();
        if (differenceOfDays >= discountbeforedays) {
          discountAmount = EventAmount.subtract(discount.getDiscountAmount());
          break;
        } else {
          discountAmount = event.getEventFees();
        }
      }
      return discountAmount;
    } else {
      return event.getEventFees();
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

  @Override
  public List<Discount> setDiscountAmount(Event event) {
    // TODO Auto-generated method stub
    BigDecimal discountPercentage = BigDecimal.ZERO;
    BigDecimal DiscountAmount = BigDecimal.ZERO;
    List<Discount> discount2 = new ArrayList<Discount>();
    System.err.println(event.getDiscounts());
    if(event.getDiscounts() != null) {
    List<Discount> discountList = event.getDiscounts();
    for (Discount discount : discountList) {
     
      discountPercentage = discount.getDiscountPercentage();
      DiscountAmount = event.getEventFees().multiply(discountPercentage);

      discount.setDiscountAmount(DiscountAmount);
      discount2.add(discount);
    }
    
    return discount2;
    }else {
    	return discount2;
    }
  }

@Override
public BigDecimal setEventAmount(EventRegistration eventReg) {
	// TODO Auto-generated method stub
		Event event = eventReg.getEvent();
		 LocalDateTime evenRegDate = eventReg.getRegistrationDate();
	      LocalDateTime eventCloseDate = event.getRegistrationClose();
	      BigDecimal discountAmount = BigDecimal.ZERO;
	      BigDecimal EventAmount = event.getEventFees();
	      List<Discount> discountList = event.getDiscounts();

	      for (Discount discount : discountList) {
	        int differenceOfDays = eventCloseDate.getDayOfMonth() - evenRegDate.getDayOfMonth();
	        System.err.println(differenceOfDays);
	        int discountbeforedays = discount.getBeforeDays();
	        if (differenceOfDays >= discountbeforedays) {
	          discountAmount = EventAmount.subtract(discount.getDiscountAmount());
	          break;
	        } else {
	          discountAmount = event.getEventFees();
	        }
	      }
	      return discountAmount;
	    } 
	    

}


