package com.axelor.event.module;

import com.axelor.app.AxelorModule;
import com.axelor.event.service.EventRegService;
import com.axelor.event.service.EventRegServiceImple;
import com.axelor.event.service.EventService;
import com.axelor.event.service.EventServiceimpl;

public class EventModule extends AxelorModule {

	@Override
	protected void configure() {
		// TODO Auto-generated method stub
		bind(EventService.class).to(EventServiceimpl.class);
		bind(EventRegService.class).to(EventRegServiceImple.class);
	}
}
