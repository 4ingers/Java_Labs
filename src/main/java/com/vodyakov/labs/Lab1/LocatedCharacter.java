package com.vodyakov.labs.Lab1;

class LocatedCharacter {

	private int row, column;
	private char character;

	LocatedCharacter(char ch, int row, int col) {
		this.character = ch;
		this.row = row;
		this.column = col;
	}

	char getCharacter() {
		return this.character;
	}

	String getInfo() {
		return character + " at " + row + "," + column;
	}
}
