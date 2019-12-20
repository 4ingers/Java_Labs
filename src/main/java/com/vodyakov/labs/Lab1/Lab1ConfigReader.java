package com.vodyakov.labs.Lab1;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.vodyakov.labs.Tools.Main;
import com.vodyakov.labs.Tools.ResourceHandler;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

class Lab1ConfigReader {
	private final String fileName;

	Lab1ConfigReader(String fileName) {
		this.fileName = fileName;
	}

	Brackets getBracketsConfig() {

		final Logger log = Logger.getLogger(ParenthesesHandler.class);
		log.setLevel(Level.ALL);

		final Type targetType
						= new TypeToken<HashMap<Character, Character>>() {
		}.getType();

		JsonReader reader;

		try {
			String path = ResourceHandler.getFullFilePath(fileName);
			if (null == path) {
				String msg = "BracketsConfig doesn't exist";
				Main.out(msg, Main.EnumOutType.ERROR);
				log.fatal(msg);
				return null;
			}
			reader = new JsonReader(new FileReader(path));
			HashMap<Character, Character> map
							= new Gson().fromJson(reader, targetType);
			reader.close();
			return new Brackets(map);
		}
		catch (IOException e) {
			return null;
		}
	}
}
