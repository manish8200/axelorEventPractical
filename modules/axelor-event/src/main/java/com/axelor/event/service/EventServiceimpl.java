package com.axelor.event.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
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
import com.google.common.io.Files;

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
		File dataCsvFile = this.getDataCsvFile(dataFile);

		BufferedReader bufferedReader;
		try {
			bufferedReader = new BufferedReader(new FileReader(dataCsvFile));

			while ((bufferedReader.readLine()) != null) {
				noOfCsvLines++;
			}

			// - 1 since first line in csv is title line
			if (noOfCsvLines - 1 > (event.getCapacity() - event.getTotalEntry())) {

				message = "Capacity Exceeded";

			} else {

				Map<String, Object> context = new HashMap<String, Object>();
				context.put("_event_id", id);
				Importer importer = new CSVImporter(configXmlFile.getAbsolutePath(),
						"/home/axelor/axelor-practical/axelor-event-app/modules/axelor-event/src/main/resources/input/");
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

	private File getDataCsvFile(MetaFile dataFile) {

		File csvFile = null;
		try {
			File tempDir = Files.createTempDir();
			csvFile = new File(tempDir, "registrations.csv");

			Files.copy(MetaFiles.getPath(dataFile).toFile(), csvFile);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return csvFile;
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
}
