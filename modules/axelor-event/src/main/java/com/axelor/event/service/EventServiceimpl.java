package com.axelor.event.service;

import com.axelor.data.Importer;
import com.axelor.data.csv.CSVImporter;
import com.axelor.event.db.Discount;
import com.axelor.event.db.Event;
import com.axelor.event.db.EventRegistration;
import com.axelor.event.db.repo.EventRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class EventServiceimpl implements EventService {

  @Override
  public BigDecimal setTotalAmount(Event event) {
    BigDecimal TotalAmount = BigDecimal.ZERO;
    try {
      List<EventRegistration> evntRegList = event.getEventRegistrationList();
      for (EventRegistration eventRegistration : evntRegList) {
        TotalAmount = TotalAmount.add(eventRegistration.getAmount());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return TotalAmount;
  }

  @Override
  public List<Discount> setDiscountAmount(Event event) {

    BigDecimal discountPercentage = BigDecimal.ZERO;
    BigDecimal DiscountAmount = BigDecimal.ZERO;
    List<Discount> NewdiscountList = new ArrayList<Discount>();
    try {
      if (event.getDiscountList() != null) {
        List<Discount> discountList = event.getDiscountList();
        for (Discount discount : discountList) {
          discountPercentage = discount.getDiscountPercentage();
          DiscountAmount = event.getEventFees().multiply(discountPercentage);
          discount.setDiscountAmount(DiscountAmount);
          NewdiscountList.add(discount);
        }
        return NewdiscountList;
      } else {
        return NewdiscountList;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return NewdiscountList;
    }
  }

  @Override
  public String importCsv(MetaFile dataFile, Integer id) throws IOException {

    Event event = (Beans.get(EventRepository.class).all().filter("self.id=?", id).fetchOne());
    File configXmlFile = this.getConfigXmlFile();
    File dataCsvFile = MetaFiles.getPath(dataFile).toFile();
    String message = null;
    Map<String, Object> context = new HashMap<String, Object>();
    context.put("_event_id", id);
    Importer importer =
        new CSVImporter(configXmlFile.getAbsolutePath(), dataCsvFile.getParent().toString());
    importer.setContext(context);
    importer.run();
    this.deleteTempFiles(configXmlFile, dataCsvFile);
    message = "Event Registration import is successfully done";
    return message;
  }

  @SuppressWarnings("deprecation")
  private File getConfigXmlFile() {
    File configurationFile = null;
    try {
      configurationFile = File.createTempFile("input-config", ".xml");
      InputStream bindFileInputStream =
          this.getClass().getResourceAsStream("/data/" + "input-config.xml");
      if (bindFileInputStream == null) {
        throw new AxelorException();
      }
      FileOutputStream outputStream = new FileOutputStream(configurationFile);
      IOUtils.copy(bindFileInputStream, outputStream);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return configurationFile;
  }

  private void deleteTempFiles(File configXmlFile, File dataCsvFile) {

    try {
      if (configXmlFile.isDirectory() && dataCsvFile.isDirectory()) {
        FileUtils.deleteDirectory(configXmlFile);
        FileUtils.deleteDirectory(dataCsvFile);
      } else {
        configXmlFile.delete();
        dataCsvFile.delete();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public Event calculateEventCalculation(Event event) {
    BigDecimal eventFees = event.getEventFees();
    BigDecimal Discountamount = BigDecimal.ZERO;
    List<EventRegistration> NewEventRegistrationList = new ArrayList<EventRegistration>();
    try {
      List<EventRegistration> eventRegistionList = event.getEventRegistrationList();
      if (eventRegistionList != null) {
        for (EventRegistration eventRegistration : eventRegistionList) {
          Period peroidBetweeneCloseDateAndRegDate =
              Period.between(
                  eventRegistration.getRegistrationDate().toLocalDate(),
                  event.getRegistrationClose());
          Integer duration = peroidBetweeneCloseDateAndRegDate.getDays();
          List<Discount> discountList = event.getDiscountList();
          discountList.sort(Comparator.comparing(Discount::getBeforeDays));
          for (Discount discount : discountList) {
            if (duration >= discount.getBeforeDays()) {
              Discountamount = discount.getDiscountAmount();
              break;
            } else {
              Discountamount = BigDecimal.ZERO;
            }
          }
          eventRegistration.setAmount(eventFees.subtract(Discountamount));
          NewEventRegistrationList.add(eventRegistration);
        }
      } else {
        NewEventRegistrationList.add(null);
      }
      event.setEventRegistrationList(NewEventRegistrationList);
      BigDecimal TotalAmount = BigDecimal.ZERO;
      List<EventRegistration> evntRegList = event.getEventRegistrationList();
      for (EventRegistration eventRegistration : evntRegList) {
        TotalAmount = TotalAmount.add(eventRegistration.getAmount());
      }
      event.setAmountCollected(TotalAmount);
      int eventListSize = event.getEventRegistrationList().size();
      BigDecimal totalAmountwithoutdiscount =
          event.getEventFees().multiply(new BigDecimal(eventListSize));
      BigDecimal totalDiscount = totalAmountwithoutdiscount.subtract(event.getAmountCollected());
      event.setTotalDiscount(totalDiscount);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return event;
  }
}
