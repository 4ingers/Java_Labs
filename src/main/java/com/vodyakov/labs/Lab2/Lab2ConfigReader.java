package com.vodyakov.labs.Lab2;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.vodyakov.labs.Tools.ResourceHandler;
import com.vodyakov.labs.Tools.Main;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;

class Lab2ConfigReader {

	static Cash getCashConfig(String fileName) {

		final Logger log = Logger.getLogger(Cash.class);
		log.setLevel(Level.ALL);

		try {
			String path = ResourceHandler.getFullFilePath(fileName);
			if (null == path) {
				String msg = "Failed loading config";
				Main.out(msg, Main.EnumOutType.ERROR);
				log.fatal(msg);
				return null;
			}

			Cash cash;
			try (JsonReader reader = new JsonReader(new FileReader(path))) {
				cash = new Gson().fromJson(reader, Cash.class);
			}
			return cash;
		}
		catch (IOException e) {
			Main.out(e.getMessage(), Main.EnumOutType.ERROR);
			log.fatal(e.getMessage());
			return null;
		}
	}
}
