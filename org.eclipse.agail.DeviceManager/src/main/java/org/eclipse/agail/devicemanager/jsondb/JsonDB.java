package org.eclipse.agail.devicemanager.jsondb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.agail.object.DeviceOverview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonDB {

	protected final Logger logger = LoggerFactory.getLogger(JsonDB.class);

	private String dbFileName = "/devicesdb.json";
	private File dbFile;

	private void setFile() {
		logger.debug("System.getenv(\"DBFILE\") {}", System.getenv("DBFILE"));
		if(System.getenv("DBFILE") != null) {
			dbFileName = System.getenv("DBFILE");
		}
		logger.debug("DBdevice File found {}", dbFileName);
		if (dbFile == null) {
			dbFile = new File(dbFileName);
			if (dbFile.exists()) {
				logger.info("DB File {} found.", dbFileName);
			}
		}
	}

	public void saveDevice(DeviceOverview dev, String type) {

		setFile();

		boolean found = false;
		List<DeviceWithType> devices = readData();
		for (int i = 0; i < devices.size(); i++) {
			if (devices.get(i).deviceOverview.getId().equals(dev.getId())) {
				found = true;
			}
		}
		if (!found) {
			DeviceWithType d = new DeviceWithType(type, dev);
			devices.add(d);
		}

		updateFile(devices);
	}

	public List<DeviceWithType> readData() {

		setFile();

		List<DeviceWithType> devices = new ArrayList<DeviceWithType>();
		BufferedReader bufferedReader;
		try {
			if (dbFile.length() == 0) {
				return devices;
			}

			FileReader f = new FileReader(dbFile);
			bufferedReader = new BufferedReader(f);

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
			devices = mapper.readValue(bufferedReader, new TypeReference<List<DeviceWithType>>() {
			});

			for (int i = 0; i < devices.size(); i++) {
				System.out.println(devices.get(i).deviceOverview.id);
			}

			logger.info("Devices {} fround in jsonDB.", devices.size());

		} catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		return devices;
	}

	public DeviceOverview getDevice(String id) {

		setFile();

		DeviceOverview device = null;
		BufferedReader bufferedReader;
		try {

			if (dbFile.length() == 0) {
				return device;
			}

			bufferedReader = new BufferedReader(new FileReader(dbFile));
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
			List<DeviceWithType> jsonDevices = mapper.readValue(bufferedReader,
					new TypeReference<List<DeviceWithType>>() {
					});

			for (int i = 0; i < jsonDevices.size(); i++) {
				if (jsonDevices.get(i).deviceOverview.id.equals(id)) {
					device = jsonDevices.get(i).deviceOverview;
				}
			}

		} catch (FileNotFoundException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Devices (id= {}, name= {}) fround in jsonDB.", device.id, device.name);
		return device;
	}

	public void deletDevice(String id) {

		setFile();

		logger.debug("Deleting device {} from database", id);

		BufferedReader bufferedReader;
		try {

			if (dbFile.length() == 0) {
				logger.debug("Device {} not found in database", id);
				return;
			}

			bufferedReader = new BufferedReader(new FileReader(dbFile));
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
			List<DeviceWithType> jsonDevices = mapper.readValue(bufferedReader,
					new TypeReference<List<DeviceWithType>>() {
					});

			DeviceWithType d = null;
			logger.debug("Devices {} found in database", jsonDevices.size());
			for (int i = 0; i < jsonDevices.size(); i++) {
				if (jsonDevices.get(i).deviceOverview.id.equals(id)) {
					d = jsonDevices.get(i);
					logger.debug("Device {} found in database", id);
				}
			}

			if (d != null) {
				jsonDevices.remove(d);
				logger.debug("Device {} deleted from database", id);
				updateFile(jsonDevices);
			}

		} catch (FileNotFoundException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateFile(List<DeviceWithType> devices) {
		// if(devices.isEmpty()) {
		// return;
		// }
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);

		try (BufferedWriter out = new BufferedWriter(new FileWriter(dbFile))) {

			// out.write(jsonString);

			mapper.writeValue(out, devices);
			String jsonString = mapper.writeValueAsString(devices);

			logger.info("Device object {} saved in jsonDB.", jsonString);
		} catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}

}
