package com.vodyakov.labs.Lab8;

import javax.swing.*;
import java.awt.*;

public class Canvas extends JPanel {

	private State[][] cells = new State[10][10];

	Canvas() {
		for (int i = 0; i < 10; ++i) {
			for (int j = 0; j < 10; ++j) {
				cells[i][j] = State.NOTHING;
			}
		}
	}

	State[][] cells() {
		return cells;
	}

	public void paint(Graphics graphics) {
		super.paint(graphics);
		int fieldSize = 10;
		int cellSize = (int)this.getSize().getWidth() / fieldSize;
		graphics.setColor(Color.darkGray);
		for (int i = 1; i < fieldSize; ++i) {
			graphics.drawLine(0, i * cellSize,
							fieldSize * cellSize, i * cellSize);
			graphics.drawLine(i * cellSize, 0,
							i * cellSize, fieldSize * cellSize);
		}
		for (int i = 0; i < 10; ++i) {
			for (int j = 0; j < 10; ++j) {
				switch (cells[i][j]) {
					case NOTHING: {
						print(graphics, j, i, State.NOTHING);
						break;
					}
					case SHIP: {
						print(graphics, j, i, State.SHIP);
						break;
					}
					case DAMAGE: {
						print(graphics, j, i, State.DAMAGE);
						break;
					}
				}
			}
		}
	}

	void print(Graphics graphics, int x, int y, State state) {
		switch (state) {
			case NOTHING: {
				graphics.setColor(Color.white);
				cells[y][x] = State.NOTHING;
				graphics.fillRect(x * 40 + 2, y * 40 + 2, 37, 37);
				break;
			}
			case SHIP: {
				graphics.setColor(Color.LIGHT_GRAY);
				cells[y][x] = State.SHIP;
				graphics.fillRect(x * 40 + 2, y * 40 + 2, 37, 37);
				break;
			}
			case DAMAGE: {
				cells[y][x] = State.DAMAGE;
				graphics.setColor(Color.LIGHT_GRAY);
				graphics.fillRect(x * 40 + 2, y * 40 + 2, 37, 37);
				graphics.setColor(Color.ORANGE);
				graphics.fillOval(x * 40 + 11, y * 40 + 11, 19, 19);
				break;
			}
			case FOOL: {
				graphics.setColor(Color.black);
				cells[y][x] = State.FOOL;
				graphics.fillOval(x * 40 + 11, y * 40 + 11, 19, 19);
				break;
			}
			case LABEL: {
				graphics.setColor(Color.cyan);
				cells[y][x] = State.LABEL;
				graphics.fillOval(x * 40 + 11, y * 40 + 11, 19, 19);
				break;
			}
			default: {
			}
		}
	}
}
