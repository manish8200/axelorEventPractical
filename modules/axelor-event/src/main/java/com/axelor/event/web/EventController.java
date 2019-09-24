package com.axelor.event.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
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
		if(eventRecalValue.getEventRegistrationList() == null) {
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
		List<EventRegistration> emailContentList = new ArrayList<EventRegistration>();
		if (eventRegstrationsList != null) {
			for (EventRegistration eventRegistration : eventRegstrationsList) {
				if (eventRegistration.getEmail() != null && !eventRegistration.getIsEmailSent()) {
					EmailAddress emailAddress = new EmailAddress();
					EventRegistration eventReg = new EventRegistration();
					emailAddress.setAddress(eventRegistration.getEmail());
					emailAddressSet.add(emailAddress);
					eventReg.setEmail(eventRegistration.getEmail());
					if(eventReg.getEmail() != null) {
						eventReg.setName(eventRegistration.getName());
						eventReg.setAddress(eventRegistration.getAddress());
						eventReg.setEmail(eventRegistration.getEmail());
						eventReg.setAmount(eventRegistration.getAmount());
					}
					emailContentList.add(eventReg);
					eventRegistration.setIsEmailSent(true);
				}
			}
			if (!emailAddressSet.isEmpty()) {
				System.out.println(emailAddressSet);
				Message message = new Message();
				Template template = new Template();
			
				for (EventRegistration eventRegistration : emailContentList) {
					template.setContent("HI " + " "+ eventRegistration.getName() +  " YourEmailAddress : " + " \t" + eventRegistration.getEmail() + "\t " + 
										"Your Address is :  " + eventRegistration.getAddress().getFullName() + "\t" + " Your Registration is SuceesfullyDone"	 );	
					message.setMailAccount(Beans.get(EmailAccountRepository.class).all().fetchOne());
					HashSet<EmailAddress> emailid = new HashSet<EmailAddress>();
					EmailAddress email = new EmailAddress();
					email.setAddress(eventRegistration.getEmail());
					emailid.add(email);
					message.setToEmailAddressSet(emailid);
					
					message.setSubject("Event Registration");
					message.setTemplate(template);
					message.setContent("Event regisTered successFully....." );
					messageService.sendByEmail(message);
				}
				response.setFlash("Email Sent successfully");
			} else {
				response.setFlash("Email is Not Sent");
			}
		}
	}
}