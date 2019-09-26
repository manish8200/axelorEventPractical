package com.axelor.event.service;

import com.axelor.event.db.Discount;
import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import com.axelor.event.db.repo.EventRepository;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class EventRegServiceImple implements EventRegistrationService {

  @Override
  public EventRegistration registrationAmountCalculation(EventRegistration eventRegistration) {

    BigDecimal fees = BigDecimal.ZERO;
    BigDecimal discountPer = BigDecimal.ZERO;
    BigDecimal feesAmount = BigDecimal.ZERO;
    BigDecimal amounts = BigDecimal.ZERO;
    Integer days = 0;

    List<Event> eventList =
        Beans.get(EventRepository.class)
            .all()
            .filter("self.id in (?1) ", eventRegistration.getEvent())
            .fetch();
    for (Event event : eventList) {
      if (event.getEventFees() != null && event.getRegistrationClose() != null) {
        fees = event.getEventFees();
        Integer durations = 0;
        LocalDate dateTo = event.getRegistrationClose();
        Date date =
            Date.from(
                eventRegistration.getRegistrationDate().atZone(ZoneId.systemDefault()).toInstant());
        LocalDate regdate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Period registrationDate = Period.between(regdate, dateTo);
        durations = registrationDate.getDays();

        List<Discount> discountList = event.getDiscountList();
        discountList.sort(Comparator.comparing(Discount::getBeforeDays));
        if (event.getEventFees() == null) {
          amounts = fees;
        } else if (discountList.isEmpty()) {
          amounts = fees;
        } else {

          for (Discount discount : discountList) {
            days = discount.getBeforeDays();
            if (days >= durations) {

              discountPer = discount.getDiscountPercentage();
              feesAmount = feesAmount.add(discountPer.multiply(fees)).divide(new BigDecimal(100));
              amounts = fees.subtract(feesAmount);
              break;

            } else {
              amounts = fees;
            }
          }
        }
      } else {
        amounts = BigDecimal.ZERO;
      }

      eventRegistration.setAmount(amounts);
    }
    return eventRegistration;
  }
}
