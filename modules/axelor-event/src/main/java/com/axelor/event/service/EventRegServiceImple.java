package com.axelor.event.service;

import com.axelor.event.db.Discount;
import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import com.axelor.event.db.repo.EventRepository;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class EventRegServiceImple implements EventRegService {

  @Override
  public BigDecimal setAmount(Event event, EventRegistration eventReg) {
    if (event.getDiscountList() != null && event.getRegistrationClose() != null) {
      LocalDateTime evenRegDate = eventReg.getRegistrationDate();
      LocalDate eventCloseDate = event.getRegistrationClose();
      BigDecimal discountAmount = BigDecimal.ZERO;
      BigDecimal EventAmount = event.getEventFees();
      List<Discount> discountList = event.getDiscountList();

      for (Discount discount : discountList) {
        int differenceOfDays = eventCloseDate.getDayOfMonth() - evenRegDate.getDayOfMonth();
        System.err.println(differenceOfDays);
        int discountbeforedays = discount.getBeforeDays();
        if (discountbeforedays >= differenceOfDays) {
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
  public BigDecimal setEventAmount(EventRegistration eventReg) {
    // TODO Auto-generated method stub
    Event event = eventReg.getEvent();
    LocalDate eventRegDate = (eventReg.getRegistrationDate()).toLocalDate();
    LocalDate eventCloseDate = event.getRegistrationClose();
    BigDecimal discountAmount = BigDecimal.ZERO;
    BigDecimal EventAmount = event.getEventFees();

    List<Discount> discountList = event.getDiscountList();
    Integer period = (Period.between(eventRegDate, eventCloseDate)).getDays();
    for (Discount discount : discountList) {
      if (period.compareTo(discount.getBeforeDays()) == -1) {
        discountAmount = EventAmount.subtract(discount.getDiscountAmount());
        break;
      } else {
        discountAmount = EventAmount;
      }
    }
    return discountAmount;
  }

  @Override
  public EventRegistration calculation(EventRegistration eventRegistration) {

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

      eventRegistration.setAmount(amounts);
    }
    return eventRegistration;
  }
}
