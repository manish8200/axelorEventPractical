package com.axelor.event.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

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

public class EventServiceimpl implements EventService {

	@Override
	public BigDecimal setTotalAmount(Event event) {
		// TODO Auto-generated method stub
		BigDecimal TotalAmount = BigDecimal.ZERO;
		try {
			List<EventRegistration> evntRegList = event.getEventRegistrationList();
			for (EventRegistration eventRegistration : evntRegList) {
				TotalAmount = TotalAmount.add(eventRegistration.getAmount());
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return TotalAmount;
	}

	@Override
	public List<Discount> setDiscountAmount(Event event) {
		// TODO Auto-generated method stub

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
			// TODO: handle exception
			e.printStackTrace();
			return NewdiscountList;
		}
	}

	@Override
	public String importCsv(MetaFile dataFile, Integer id) {
		// TODO Auto-generated method stub

		int noOfCsvLines = 0;
		String message = null;
		Event event = (Beans.get(EventRepository.class).all().filter("self.id=?", id).fetchOne());
		File configXmlFile = this.getConfigXmlFile();
		File dataCsvFile = MetaFiles.getPath(dataFile).toFile();
		
		BufferedReader bufferedReader;
		try {
			bufferedReader = new BufferedReader(new FileReader(dataCsvFile));
			while ((bufferedReader.readLine()) != null) {
				noOfCsvLines++;
			}
			if (noOfCsvLines - 1 > (event.getCapacity() - event.getTotalEntry())) {
				message = "Capacity Exceeded";
			} else {
				Map<String, Object> context = new HashMap<String, Object>();
				context.put("_event_id", id);
				//System.err.println(dataCsvFile.getParent().toString() + "/");
				Importer importer = new CSVImporter(configXmlFile.getAbsolutePath(), dataCsvFile.getParent().toString());
				importer.setContext(context);
				importer.run();
				message = "Registration imported Successfully";
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return message;
	}

	@SuppressWarnings("deprecation")
	private File getConfigXmlFile() {

		File configFile = null;
		try {
			configFile = File.createTempFile("input-config", ".xml");

			InputStream bindFileInputStream = this.getClass().getResourceAsStream("/data/" + "input-config.xml");

			if (bindFileInputStream == null) {
				throw new AxelorException();
			}

			FileOutputStream outputStream = new FileOutputStream(configFile);

			IOUtils.copy(bindFileInputStream, outputStream);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return configFile;
	}

	@Override
	public Event CalculateEventCalculation(Event event) {
		BigDecimal eventFees = event.getEventFees();
		BigDecimal Discountamount = BigDecimal.ZERO;
		List<EventRegistration> NewEventRegistrationList = new ArrayList<EventRegistration>();
		try {
			List<EventRegistration> eventRegistionList = event.getEventRegistrationList();
			if (eventRegistionList != null) {
				for (EventRegistration eventRegistration : eventRegistionList) {
					Period peroidBetweeneCloseDateAndRegDate = Period.between(eventRegistration.getRegistrationDate().toLocalDate(), event.getRegistrationClose());
					Integer duration = peroidBetweeneCloseDateAndRegDate.getDays();
					List<Discount> discountList = event.getDiscountList();
					discountList.sort(Comparator.comparing(Discount::getBeforeDays));
					for (Discount discount : discountList) {
						if (duration <= discount.getBeforeDays()) {
							Discountamount = discount.getDiscountAmount();
							break;
						} else {
							Discountamount = BigDecimal.ZERO;
						}
					}
					eventRegistration.setAmount(eventFees.subtract(Discountamount));
	 			}
			} else {
				NewEventRegistrationList.add(null);
			}
			event.setEventRegistrationList(NewEventRegistrationList);
			System.out.println(event.getEventRegistrationList());
			BigDecimal TotalAmount = BigDecimal.ZERO;
			List<EventRegistration> evntRegList = event.getEventRegistrationList();
			for (EventRegistration eventRegistration : evntRegList) {
				TotalAmount = TotalAmount.add(eventRegistration.getAmount());
			}
			event.setAmountCollected(TotalAmount);
			int eventListSize = event.getEventRegistrationList().size();
			System.err.println(eventListSize);
			BigDecimal totalAmountwithoutdiscount = event.getEventFees().multiply(new BigDecimal(eventListSize));
			BigDecimal totalDiscount = totalAmountwithoutdiscount.subtract(event.getAmountCollected());

			event.setTotalDiscount(totalDiscount);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	
		return event;
	}
}
