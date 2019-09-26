package com.axelor.event.web;

import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.EmailAccountRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.event.db.Discount;
import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import com.axelor.event.service.EventRegistrationService;
import com.axelor.event.service.EventService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import javax.mail.MessagingException;

public class EventController {

  @Inject EventService service;

  @Inject EventRegistrationService eventRegservice;

  @Inject private MessageService messageService;

  public void setDiscountAmount(ActionRequest request, ActionResponse response) {
    Discount discount = request.getContext().asType(Discount.class);
    Event event = request.getContext().getParent().asType(Event.class);
    if (event.getEventFees() != null) {
      BigDecimal discountAmount =
          (discount.getDiscountPercentage().multiply(event.getEventFees()))
              .divide(new BigDecimal(100));
      response.setValue("discountAmount", discountAmount);
    } else {
      response.setValue("discountAmount", BigDecimal.ZERO);
    }
  }

  public void recalculateventcalculation(ActionRequest request, ActionResponse response) {
    Event event = request.getContext().asType(Event.class);
    Event eventRecalValue = service.calculateEventCalculation(event);
    if (eventRecalValue.getEventRegistrationList() != null) {
      response.setValue("eventRegistrationList", eventRecalValue.getEventRegistrationList());
      response.setValue("amountCollected", event.getAmountCollected());
      response.setValue("totalDiscount", eventRecalValue.getTotalDiscount());
    }
  }

  public void setDisCountList(ActionRequest request, ActionResponse response) {
    Event event = request.getContext().asType(Event.class);
    List<Discount> discountList = service.setDiscountAmount(event);
    response.setValue("discountList", discountList);
  }

  public void setTotalAmount(ActionRequest request, ActionResponse response) {
    Event event = request.getContext().asType(Event.class);
    BigDecimal Amount = service.setTotalAmount(event);
    response.setValue("amountCollected", Amount);
  }

  public void setTotalDiscountAmount(ActionRequest request, ActionResponse response) {
    Event event = request.getContext().asType(Event.class);
    BigDecimal TotalAmount = service.setTotalAmount(event);

    int eventListSize = event.getEventRegistrationList().size();
    BigDecimal totalAmountwithoutdiscount =
        event.getEventFees().multiply(new BigDecimal(eventListSize));
    BigDecimal totalDiscount = totalAmountwithoutdiscount.subtract(TotalAmount);

    response.setValue("totalDiscount", totalDiscount);
  }

  public void EventRegistrationListImport(ActionRequest request, ActionResponse response)
      throws FileNotFoundException, IOException {
    LinkedHashMap<String, Object> map =
        (LinkedHashMap<String, Object>) request.getContext().get("metaFile");
    MetaFile dataFile =
        Beans.get(MetaFileRepository.class).find(((Integer) map.get("id")).longValue());
    if (!dataFile.getFileType().equals("text/csv")) {
      response.setError("Please Upload a csv file");
    } else {
      Integer id = (Integer) request.getContext().get("_id");
      String message = service.importCsv(dataFile, id);
      response.setFlash(message);
    }
  }

  public void SendEmail(ActionRequest request, ActionResponse response)
      throws MessagingException, IOException, AxelorException {
    Event event = request.getContext().asType(Event.class);
    List<EventRegistration> eventRegistrationList = event.getEventRegistrationList();
    Message message = new Message();
    Template template = new Template();
    if (eventRegistrationList != null) {
      for (EventRegistration eventRegistration : eventRegistrationList) {
        if (eventRegistration.getEmail() != null) {
          template.setContent(
              " Hi"
                  + " "
                  + eventRegistration.getName()
                  + " YourEmailAddress : "
                  + " "
                  + eventRegistration.getEmail()
                  + " "
                  + "For Event :"
                  + eventRegistration.getEvent().getReference()
                  + " Paid Amount is  "
                  + "   "
                  + eventRegistration.getAmount()
                  + "  "
                  + "Your Address is : "
                  + eventRegistration.getAddress().getFullName()
                  + "  "
                  + " Thank You For SuccessFul Transaction");
          message.setMailAccount(Beans.get(EmailAccountRepository.class).all().fetchOne());
          HashSet<EmailAddress> emailid = new HashSet<EmailAddress>();
          EmailAddress email = new EmailAddress();
          email.setAddress(eventRegistration.getEmail());
          emailid.add(email);
          message.setToEmailAddressSet(emailid);
          message.setSubject("Event Registration");
          message.setTemplate(template);
          System.out.println(template.getContent());
          message.setContent("Event regisTered successFully.....");
          eventRegistration.setIsEmailSent(true);
          messageService.sendByEmail(message);
          response.setFlash("Email Sent successfully");
        } else {
          response.setFlash("Please Enter EventRegistration's EmailId");
        }
      }
    } else {
      response.setFlash("Email is not Sent");
    }
  }
}
