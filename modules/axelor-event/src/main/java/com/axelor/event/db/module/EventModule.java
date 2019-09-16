package com.axelor.event.db.module;

import com.axelor.app.AxelorModule;
import com.axelor.event.db.service.EventService;
import com.axelor.event.db.service.EventServiceimpl;

public class EventModule extends AxelorModule {

  @Override
  protected void configure() {
    // TODO Auto-generated method stub
    bind(EventService.class).to(EventServiceimpl.class);
  }
}
