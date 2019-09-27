package com.axelor.event.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;

import com.axelor.event.db.Discount;
import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import com.axelor.event.db.repo.EventRepository;
import com.axelor.inject.Beans;

public class EventRegServiceImple implements EventRegistrationService {

  @Override
  public EventRegistration registrationAmountCalculation(EventRegistration eventRegistration) {

    BigDecimal eventamount = BigDecimal.ZERO;
    BigDecimal discountAmount = BigDecimal.ZERO;
    List<Event> eventList =
        Beans.get(EventRepository.class)
            .all()
            .filter("self.id in (?1) ", eventRegistration.getEvent())
            .fetch();
    for (Event event : eventList) {
      if (event.getEventFees() != null && event.getRegistrationClose() != null) {
    	  LocalDate regdate = eventRegistration.getRegistrationDate().toLocalDate();
    	  LocalDate closeDate =event.getRegistrationClose();
        Integer duration = Period.between(regdate, closeDate).getDays();
        List<Discount> discountList = event.getDiscountList();
        discountList.sort(Comparator.comparing(Discount::getBeforeDays));
        for (Discount discount : discountList) {
			if(duration >= discount.getBeforeDays()) {
				eventRegistration.setAmount(event.getEventFees().subtract(discount.getDiscountAmount()));
			}
			else {
				eventRegistration.setAmount(eventRegistration.getAmount());
			}
        }
      } else {
    	  eventamount = BigDecimal.ZERO;
    	  eventRegistration.setAmount(eventamount);
      }
  
    }
    return eventRegistration;
  }
}




