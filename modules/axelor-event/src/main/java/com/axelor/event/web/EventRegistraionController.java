package com.axelor.event.web;

import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import com.axelor.event.service.EventRegistrationService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class EventRegistraionController {

	@Inject
	EventRegistrationService service;

	public void setEventRegList(ActionRequest request, ActionResponse response) {
		if (request.getContext().getParent() != null) {
			response.setValue("event", request.getContext().getParent().asType(Event.class));
			response.setAttr("event", "hidden", true);
		}
	}

	public void setEventRegistrationAmount(ActionRequest request, ActionResponse response) {
		EventRegistration eventReg = request.getContext().asType(EventRegistration.class);
		if (request.getContext().getParent() == null) {
			EventRegistration eventreg = service.calculation(eventReg);
			response.setValue("amount", eventreg.getAmount());
		}
		else {
			EventRegistration eventRegistartion = service.calculation(eventReg);

			response.setValue("amount", eventRegistartion.getAmount());
		}
	}
}
