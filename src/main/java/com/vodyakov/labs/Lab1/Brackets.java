package com.vodyakov.labs.Lab1;

import java.util.HashMap;
import java.util.HashSet;

class Brackets {

	private HashMap<Character, Character> map;

	Brackets(HashMap<Character, Character> rhs) {
		this.map = rhs;
	}

	boolean containsOpening(Character opening) {
		return this.map.containsKey(opening);
	}

	Character getPairedTo(Character opening) {
		return map.get(opening);
	}

	HashSet<Character> closingSet() {
		return new HashSet<>(this.map.values());
	}

	boolean hasWhitespace() {
		for (Character character : map.keySet())
			if (Character.isWhitespace(character) || Character.isWhitespace(getPairedTo(character)))
				return true;
		return false;
	}
}
