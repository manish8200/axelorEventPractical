package com.axelor.event.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import javax.mail.MessagingException;

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

public class EventController {

	@Inject
	EventService service;

	@Inject
	EventRegistrationService eventRegservice;

	@Inject
	private MessageService messageService;

	public void setDiscountAmount(ActionRequest request, ActionResponse response) {
		Discount discount = request.getContext().asType(Discount.class);
		Event event = request.getContext().getParent().asType(Event.class);
		if (event.getEventFees() != null) {
			BigDecimal discountAmount = (discount.getDiscountPercentage().multiply(event.getEventFees()))
					.divide(new BigDecimal(100));
			System.out.println(discountAmount);
			response.setValue("discountAmount", discountAmount);
		} else {
			response.setValue("discountAmount", BigDecimal.ZERO);
		}
	}

	public void recalculateventcalculation(ActionRequest request, ActionResponse response) {
		Event event = request.getContext().asType(Event.class);
		Event eventRecalValue = service.CalculateEventCalculation(event);
	//	System.err.println(event.getEventRegistrationList());
		response.setValue("eventRegistrationList", eventRecalValue.getEventRegistrationList());
		response.setValue("amountCollected", event.getAmountCollected());
		response.setValue("totalDiscount", eventRecalValue.getTotalDiscount());
		/* response.setValues(eventrecalValue); */
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
	//	System.err.println(eventListSize);
		BigDecimal totalAmountwithoutdiscount = event.getEventFees().multiply(new BigDecimal(eventListSize));
		BigDecimal totalDiscount = totalAmountwithoutdiscount.subtract(TotalAmount);

		response.setValue("totalDiscount", totalDiscount);
	}

	public void EventRegistrationListImport(ActionRequest request, ActionResponse response) {
		LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) request.getContext().get("metaFile");
		MetaFile dataFile = Beans.get(MetaFileRepository.class).find(((Integer) map.get("id")).longValue());
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
		List<EventRegistration> eventRegstrationsList = event.getEventRegistrationList();
		HashSet<EmailAddress> emailAddressSet = new HashSet<EmailAddress>();
		String email = new String();
		HashSet<EventRegistration> eventregContent = new HashSet<EventRegistration>();
		System.out.println(email);
		if (eventRegstrationsList != null) {
			for (EventRegistration eventRegistration : eventRegstrationsList) {
				if (eventRegistration.getEmail() != null && !eventRegistration.getIsEmailSent()) {
					EmailAddress emailAddress = new EmailAddress();
					emailAddress.setAddress(eventRegistration.getEmail());
					eventregContent.add(eventRegistration);
					emailAddressSet.add(emailAddress);
					eventRegistration.setIsEmailSent(true);
				}
			}
			System.err.println(email);
			if (!emailAddressSet.isEmpty()) {
				System.out.println(emailAddressSet);
				Message message = new Message();
				Template template = new Template();
				template.setContent("Event Content: " +eventregContent);
				// System.err.println(Beans.get(EmailAccountRepository.class).all().fetchOne());
				message.setMailAccount(Beans.get(EmailAccountRepository.class).all().fetchOne());
				message.setToEmailAddressSet(emailAddressSet);
				message.setSubject("Event Registration");
				message.setTemplate(template);
				System.err.println(email);
				System.err.println(template);
				message.setContent("Event regisTered successFully....." );
				messageService.sendByEmail(message);
				System.err.println(message);
				response.setFlash("Email Sent successfully");
			} else {
				response.setFlash("Email is Not Sent");
			}
		}
	}
}
