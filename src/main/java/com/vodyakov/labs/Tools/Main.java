package com.vodyakov.labs.Tools;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Main {

	public static String getPath(String filename) {
		return ResourceHandler.getFullFilePath(filename);
	}

	public static void out(String arg, EnumOutType type) {
		if (EnumOutType.INFO == type)
			System.out.println("INFO: " + arg);
		else if (EnumOutType.WARNING == type)
			System.out.println("WARNING: " + arg);
		else if (EnumOutType.ERROR == type)
			System.err.println("ERROR: " + arg);
		else
			throw new IllegalArgumentException("Unexpected value: " + type);
	}

	public static void out(String arg) {
		System.out.println(arg);
	}

	// Разбор аргументов командной строки, общий для всех программ.
	public static boolean batchArgs(String[] args, int numberOfArgs, String help) {
		Logger log = Logger.getLogger(Main.class);
		log.setLevel(Level.ALL);

		if (args.length < numberOfArgs || 0 == args.length) {
			System.out.print(help);
			return false;
		}
		else if (args[args.length - 1].equals("-h"))
			System.out.print(help);

		for (int i = 0; i < args.length; ++i) {
			if (args[i].contains("=")) {
				String[] splitted = args[i].split("=");
				if (splitted[1].matches("^[\\w\\s-_.()]+\\.[a-z\\d]+$")) {
					if (splitted[0].equals("input")) {
						String path = ResourceHandler.getFullFilePath(splitted[1]);
						if (Optional.ofNullable(path).isPresent())
							args[i] = path;
						else {
							out("File not found", EnumOutType.ERROR);
							log.fatal("File not found");
							return false;
						}
					}
					else if (splitted[0].equals("output")) {
						try {
							new File(splitted[1]).createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return true;
	}

	public enum EnumOutType {INFO, WARNING, ERROR}
}
