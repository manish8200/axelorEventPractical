package com.axelor.event.db.web;

import java.math.BigDecimal;
import java.util.List;

import com.axelor.db.JpaSupport;
import com.axelor.event.db.Discount;
import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;


public class EventController extends JpaSupport{
	


	public void setDiscountAmount(ActionRequest request, ActionResponse response) {
		Discount discount = request.getContext().asType(Discount.class);
		Event event = request.getContext().getParent().asType(Event.class);
			
	BigDecimal discountAmount = 	(discount.getDiscountPercentage().multiply(event.getEventFees())).divide(new BigDecimal(100));
				System.out.println(discountAmount);
			response.setValue("discountAmount", discountAmount);
	}
	
	public void setAmountCollected(ActionRequest request,ActionResponse response ) {
		/*EventRegistration eventReg = request.getContext().asType(EventRegistration.class);*/
		Event event = request.getContext().getParent().asType(Event.class);
		BigDecimal Amount = BigDecimal.ZERO;
		List<EventRegistration> eventRegs = event.getEventRegistration();
		for (EventRegistration eventRegistration : eventRegs) {
			Amount = Amount.add(eventRegistration.getAmount());
			
		}
		event.setAmountCollected(Amount);
		response.setValues(event);
		System.err.println(Amount);
		/*response.setValue("amountCollected", Amount);*/
	}
	
	public void setAmount(ActionRequest request, ActionResponse response) {
		EventRegistration eventReg = request.getContext().asType(EventRegistration.class);
		Event event = request.getContext().getParent().asType(Event.class);
		BigDecimal discountamount = BigDecimal.ZERO;
		List<Discount> discount =	event.getDiscounts();
	
	for (Discount discount2 : discount) {
	discountamount = discount2.getDiscountAmount();
	}
	if(discount != null) {
	response.setValue("amount", discountamount );
	}else {
		response.setValue("amount", event.getEventFees());
	}
		}
		}
	

