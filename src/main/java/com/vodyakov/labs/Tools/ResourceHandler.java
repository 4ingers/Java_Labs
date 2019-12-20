package com.vodyakov.labs.Tools;


import java.net.URL;
import java.util.Optional;

public class ResourceHandler {
	public static String getFullFilePath(String fileName) {
		ClassLoader classloader = ResourceHandler.class.getClassLoader();
		URL url = classloader.getResource(fileName);
		if (!Optional.ofNullable(url).isPresent())
			return null;
		return url.getFile();
	}
}
