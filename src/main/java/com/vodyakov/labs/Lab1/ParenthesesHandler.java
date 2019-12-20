package com.vodyakov.labs.Lab1;

import com.vodyakov.labs.Tools.Main;
import com.vodyakov.labs.Tools.ResourceHandler;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Stack;


class ParenthesesHandler {
	static boolean exec(String fileName, String configFileName) {

		final Logger log = Logger.getLogger(ParenthesesHandler.class);
		log.setLevel(Level.ALL);

		Brackets brackets = new Lab1ConfigReader(configFileName).getBracketsConfig();

		if (null == brackets) {
			String msg = "BracketsConfig wasn't set";
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
			return false;
		}

		if (brackets.hasWhitespace()) {
			String msg = "Whitespace characters are inadmissible";
			Main.out(msg, Main.EnumOutType.ERROR);
			log.error(msg);
			return false;
		}

		FileReader reader;
		try {
			String path = ResourceHandler.getFullFilePath(fileName);
			if (null == path) {
				String msg = "Input file doesn't exist";
				Main.out(msg, Main.EnumOutType.ERROR);
				log.fatal(msg);
				return false;
			}

			reader = new FileReader(path);
			if (0 == new File(path).length()) {
				String msg = "Input file is empty";
				Main.out(msg, Main.EnumOutType.ERROR);
				log.fatal(msg);
				return false;
			}
		}
		catch (FileNotFoundException e) {
			String msg = "Input file doesn't exist";
			Main.out(msg);
			log.fatal(msg);
			return false;
		}

		Stack<LocatedCharacter> stack = new Stack<>();
		HashSet<Character> closers = brackets.closingSet();

		try {
			for (int curCh = 0, row = 1, col = 0;
					 curCh != -1;
					 curCh = reader.read(), ++col) {

				char character = (char)curCh;

				if (Character.isWhitespace(character)) {
					if ('\n' == character) {
						++row;
						col = 0;
					}
				}

				else if (brackets.containsOpening(character)
								&& closers.contains(character)) {
					if (!stack.empty() && stack.peek().getCharacter() == character)
						stack.pop();
					else
						stack.push(new LocatedCharacter(character, row, col));
				}

				else if (brackets.containsOpening(character))
					stack.push(new LocatedCharacter(character, row, col));

				else if (closers.contains(character)) {

					if (stack.empty()) {
						String msg = "Outweighing closing symbol " + character +
										" at " + row + "," + col;
						Main.out(msg, Main.EnumOutType.ERROR);
						log.error(msg);
						return false;
					}

					LocatedCharacter top = stack.pop();
					char awaited = brackets.getPairedTo(top.getCharacter());

					if (awaited != character) {
						String msg = "Non-matching closing symbol " + character +
										" at " + row + "," + col + ". Awaited : " + awaited;
						Main.out(msg, Main.EnumOutType.ERROR);
						log.error(msg);
						return false;
					}
				}
			}
			if (!stack.isEmpty()) {
				String msg = ("Bracket " + stack.pop().getInfo() + " wasn't closed");
				Main.out(msg, Main.EnumOutType.ERROR);
				log.error(msg);
			}
			else {
				String msg = "Succeed";
				Main.out(msg, Main.EnumOutType.INFO);
				log.info(msg);
			}
		}
		catch (IOException e) {
			String msg = e.toString();
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
		}
		return true;
	}
}
