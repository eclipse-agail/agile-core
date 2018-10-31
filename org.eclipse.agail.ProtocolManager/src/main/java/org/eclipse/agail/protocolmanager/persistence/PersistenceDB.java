package org.eclipse.agail.protocolmanager.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.agail.protocols.config.ProtocolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PersistenceDB {
	protected final Logger logger = LoggerFactory.getLogger(PersistenceDB.class);

	private String dbFileName = "/protocolsdb.json";
	private File dbFile;

	private void setFile() {
		logger.debug("System.getenv(\"DBFILE\") {}", System.getenv("DBFILE"));
		if(System.getenv("DBFILE") != null) {
			dbFileName = System.getenv("DBFILE");
		}
		logger.debug("PersistenceDB File found {}", dbFileName);
		if (dbFile == null) {
			dbFile = new File(dbFileName);
			if (dbFile.exists()) {
				logger.info("DB File {} found.", dbFileName);
			}
		}
	}

	public void saveProtocol(String protocol, List<ProtocolConfig> configs) {

		setFile();

		boolean found = false;
		List<ProtocolWithConfig> protocols = readData();
		for (int i = 0; i < protocols.size(); i++) {
			if (protocols.get(i).getProtocolId().equals(protocol)) {
				protocols.get(i).setConfigurations(configs);
				found = true;
			}
		}
		if (!found) {
			ProtocolWithConfig d = new ProtocolWithConfig(protocol, configs);
			protocols.add(d);
		}

		updateFile(protocols);
	}

	public List<ProtocolWithConfig> readData() {

		setFile();

		List<ProtocolWithConfig> protocols = new ArrayList<ProtocolWithConfig>();
		BufferedReader bufferedReader;
		try {
			if (dbFile.length() == 0) {
				return protocols;
			}

			FileReader f = new FileReader(dbFile);
			bufferedReader = new BufferedReader(f);

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
			protocols = mapper.readValue(bufferedReader, new TypeReference<List<ProtocolWithConfig>>() {
			});

			for (int i = 0; i < protocols.size(); i++) {
				System.out.println(protocols.get(i).getProtocolId());
			}

			logger.info("protocols {} fround in jsonDB.", protocols.size());

		} catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		return protocols;
	}
	
	public ProtocolWithConfig getProtocolWithConfig(String protocolId) {

		setFile();

		ProtocolWithConfig protocol = null;
		BufferedReader bufferedReader;
		try {

			if (dbFile.length() == 0) {
				return protocol;
			}

			bufferedReader = new BufferedReader(new FileReader(dbFile));
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
			List<ProtocolWithConfig> jsonprotocols = mapper.readValue(bufferedReader,
					new TypeReference<List<ProtocolWithConfig>>() {
					});

			for (int i = 0; i < jsonprotocols.size(); i++) {
				if (jsonprotocols.get(i).protocolId.equals(protocolId)) {
					protocol = jsonprotocols.get(i);
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
		logger.info("protocol (id= {}) fround in persistenceDB. {}", protocol.getProtocolId(), protocol.getConfigurations());
		return protocol;
	}

	public void deletprotocol(String id) {

		setFile();

		logger.debug("Deleting protocol {} from database", id);

		BufferedReader bufferedReader;
		try {

			if (dbFile.length() == 0) {
				logger.debug("protocol {} not found in database", id);
				return;
			}

			bufferedReader = new BufferedReader(new FileReader(dbFile));
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
			List<ProtocolWithConfig> jsonprotocols = mapper.readValue(bufferedReader,
					new TypeReference<List<ProtocolWithConfig>>() {
					});

			ProtocolWithConfig d = null;
			logger.debug("protocols {} found in database", jsonprotocols.size());
			for (int i = 0; i < jsonprotocols.size(); i++) {
				if (jsonprotocols.get(i).getProtocolId().equals(id)) {
					d = jsonprotocols.get(i);
					logger.debug("protocol {} found in database", id);
				}
			}

			if (d != null) {
				jsonprotocols.remove(d);
				logger.debug("protocol {} deleted from database", id);
				updateFile(jsonprotocols);
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

	private void updateFile(List<ProtocolWithConfig> protocols) {
		// if(protocols.isEmpty()) {
		// return;
		// }
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);

		try (BufferedWriter out = new BufferedWriter(new FileWriter(dbFile))) {

			// out.write(jsonString);

			mapper.writeValue(out, protocols);
			String jsonString = mapper.writeValueAsString(protocols);

			logger.info("protocol object {} saved in jsonDB.", jsonString);
		} catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}
}
