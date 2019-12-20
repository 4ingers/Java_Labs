package com.vodyakov.labs.Lab8;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Client {
	private static Map<Integer, Integer> shipsPark = new HashMap<>();
	private static Random rand = new Random();
	private static boolean playerIsBusy = false;
	private static boolean gameStarted = false;
	private static boolean myTurn = true;
	private static int shipsCount = 0;
	private static Socket socket;
	private static ObjectOutputStream oos;
	private static ObjectInputStream ois;
	private static JFrame jFrame = getFrame();

	public static void main(String[] args) {
		jFrame.pack();
		jFrame.setLocationRelativeTo(null);
	}

	// === Создание окна ===
	private static JFrame getFrame() {
		final Logger log = Logger.getLogger(JFrame.class);
		log.setLevel(Level.ALL);

		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (IllegalAccessException | InstantiationException |
						UnsupportedLookAndFeelException | ClassNotFoundException e) {
			String msg = "Setting LookAndFeel failed";
			JOptionPane.showMessageDialog(jFrame,
							msg, "Fatal error",
							JOptionPane.ERROR_MESSAGE);
			log.fatal(msg);
			System.exit(-1);
		}

		JMenuBar jMenuBar = getMenu();
		Canvas mySea = new Canvas();
		Canvas enemySea = new Canvas();
		JLabel myLabel = new JLabel("Your ships:");
		JLabel enemyLabel = new JLabel("Enemy ships:");
		JLabel shipsTypesLabel = new JLabel("Ships:");
		JComboBox<String> shipsTypesBox = new JComboBox<>();
		JComboBox<String> orientationBox = new JComboBox<>();
		JButton reset = new JButton("Reset");
		JButton generate = new JButton("Generate");
		JButton battle = new JButton("Battle!");

		// === Настройка jFrame ===
		JFrame jFrame = new JFrame();
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setTitle("Battleship");
		jFrame.setJMenuBar(jMenuBar);
		jFrame.setResizable(false);
		jFrame.setVisible(true);

		// === Размеры полей, цвет ячеек и линий между ними ===
		mySea.setPreferredSize(new Dimension(400, 400));
		mySea.setBackground(Color.white);
		mySea.setBorder(BorderFactory.createLineBorder(Color.darkGray));
//		mySea.fillGrid();

		enemySea.setPreferredSize(new Dimension(400, 400));
		enemySea.setBackground(Color.white);
		enemySea.setBorder(BorderFactory.createLineBorder(Color.darkGray));

		// === Парк кораблей ===
		shipsPark.put(1, 4);
		shipsPark.put(2, 3);
		shipsPark.put(3, 2);
		shipsPark.put(4, 1);

		shipsTypesBox.addItem("Patrol Boat");
		shipsTypesBox.addItem("Submarine");
		shipsTypesBox.addItem("Destroyer");
		shipsTypesBox.addItem("Battleship");

		// === Направление постановки корабля ===
		orientationBox.addItem("Vertically");
		orientationBox.addItem("Horizontally");

		// === Компоновка ===
		JPanel shipsSettingsPanel = new JPanel();
		shipsSettingsPanel.setLayout(
						new BoxLayout(shipsSettingsPanel, BoxLayout.X_AXIS));
		shipsSettingsPanel.add(Box.createHorizontalStrut(10));
		shipsSettingsPanel.add(generate);
		shipsSettingsPanel.add(Box.createHorizontalStrut(10));
		shipsSettingsPanel.add(reset);
		shipsSettingsPanel.add(Box.createHorizontalStrut(10));
		shipsSettingsPanel.add(shipsTypesLabel);
		shipsSettingsPanel.add(Box.createHorizontalStrut(10));
		shipsSettingsPanel.add(shipsTypesBox);
		shipsSettingsPanel.add(Box.createHorizontalStrut(10));
		shipsSettingsPanel.add(orientationBox);
		shipsSettingsPanel.add(Box.createHorizontalStrut(10));
		shipsSettingsPanel.add(battle);
		shipsSettingsPanel.add(Box.createHorizontalStrut(10));

		JPanel labelsPanel = new JPanel();
		labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.X_AXIS));
		labelsPanel.add(Box.createHorizontalStrut(5));
		labelsPanel.add(myLabel);
		labelsPanel.add(Box.createHorizontalStrut(345));
		labelsPanel.add(enemyLabel);
		labelsPanel.add(Box.createHorizontalStrut(327));

		JPanel mapsPanel = new JPanel();
		mapsPanel.setLayout(new BoxLayout(mapsPanel, BoxLayout.X_AXIS));
		mapsPanel.add(Box.createHorizontalStrut(10));
		mapsPanel.add(mySea);
		mapsPanel.add(Box.createHorizontalStrut(10));
		mapsPanel.add(enemySea);
		mapsPanel.add(Box.createHorizontalStrut(10));

		Container container = jFrame.getContentPane();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.add(Box.createVerticalStrut(10));
		container.add(shipsSettingsPanel);
		container.add(Box.createVerticalStrut(10));
		container.add(new JSeparator());
		container.add(Box.createVerticalStrut(10));
		container.add(labelsPanel);
		container.add(Box.createVerticalStrut(10));
		container.add(mapsPanel);
		container.add(Box.createVerticalStrut(10));

		// === Координатные сетки ===
		State[][] myShips = mySea.cells();
		State[][] enemyShips = enemySea.cells();

		// === Очистка карты, доступна только до начала игры ===
		reset.addActionListener(e -> {
			if (shipsCount > 0 && !playerIsBusy && !gameStarted)
				clearMap(mySea);
		});

		// === Автогенерация поля ===
		generate.addActionListener(e -> {
			for (int i = 4; i > 0; --i) {
				while (shipsPark.get(i) > 0) {
					int x = rand.nextInt(10);
					int y = rand.nextInt(10);
					if (myShips[y][x] != State.SHIP) {
						int orientation = rand.nextInt(2);
						addShip(myShips, x, y, i, orientation, mySea);
					}
				}
			}
		});

		// === Начало игры ===
		battle.addActionListener(e -> {
			if (!playerIsBusy && !gameStarted && shipsCount == 10) {
				try {
					socket = new Socket("localhost", 5050);

					oos = new ObjectOutputStream(socket.getOutputStream());
					ois = new ObjectInputStream(socket.getInputStream());

					gameStarted = ois.readBoolean();

					oos.writeObject(myShips);
					oos.flush();

					playerIsBusy = true;

					new Thread(() -> {

						while (!gameStarted) {
							try {
								gameStarted = ois.readBoolean();
								myTurn = ois.readBoolean();

								if (myTurn) {
									jFrame.setTitle("Battleship: yours");
								} else {
									jFrame.setTitle("Battleship : wait..");
								}
								if (!myTurn) {
									new Thread(() -> {

										while (!myTurn) {
											try {
												int x = ois.readInt();
												int y = ois.readInt();

												if (myShips[y][x] == State.SHIP) {
													mySea.print(mySea.getGraphics(), x, y, State.DAMAGE);

													if (isGameOver(myShips)) {
														JOptionPane.showMessageDialog(jFrame,
																		"You lost!",
																		"Battleship",
																		JOptionPane.INFORMATION_MESSAGE);
														myTurn = false;
														ois.close();
														oos.close();
														socket.close();
														System.exit(0);
													}
												}
												else if (myShips[y][x] == State.NOTHING) {
													mySea.print(mySea.getGraphics(), x, y, State.FOOL);
													myTurn = true;
													jFrame.setTitle("Battleship: yours");
												}

											}
											catch (IOException ex) {
												JOptionPane.showMessageDialog(jFrame,
																"Enemy left the game",
																"Battleship ended",
																JOptionPane.INFORMATION_MESSAGE);
												log.info("Player left the game");
												myTurn = false;
												System.exit(-1);
											}
										}
									}).start();
								}
							}
							catch (IOException ex) {
								JOptionPane.showMessageDialog(jFrame,
												"Something failed", "Error",
												JOptionPane.ERROR_MESSAGE);
								log.fatal("Failed");
								myTurn = false;
							}
						}
					}).start();
				}
				catch (IOException ex) {
					JOptionPane.showMessageDialog(jFrame,
									"Something failed", "Error",
									JOptionPane.ERROR_MESSAGE);
					log.fatal("Failed");
					myTurn = false;
				}
			}
		});

		// === Добавление кораблей вручную ===
		mySea.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {

					super.mousePressed(e);

					int x = e.getX() / 40;
					int y = e.getY() / 40;

					int shipType = shipsTypesBox.getSelectedIndex() + 1;
					int orientation = orientationBox.getSelectedIndex();

					if (x >= 0 && y >= 0 && x < 10 && y < 10)
						addShip(myShips, x, y, shipType, orientation, mySea);
				}
			}
		});

		// === Выстрел/метка ===
		enemySea.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {

				super.mousePressed(e);

				if (playerIsBusy && gameStarted
								&& !isGameOver(myShips) && !isGameOver(enemyShips)) {
					int x = e.getX() / 40;
					int y = e.getY() / 40;

					// === Выстрел ===
					if (e.getButton() == MouseEvent.BUTTON1
									&& enemyShips[y][x] == State.NOTHING && myTurn) {
						if (x < 10 && y < 10) {
							if (enemyShips[y][x] == State.SHIP)
								enemySea.print(enemySea.getGraphics(), x, y, State.DAMAGE);
							else if (enemyShips[y][x] == State.NOTHING)
								enemySea.print(enemySea.getGraphics(), x, y, State.FOOL);

							try {
								oos.writeInt(x);
								oos.flush();
								oos.writeInt(y);
								oos.flush();

								myTurn = ois.readBoolean();

								if (myTurn)
									enemySea.print(enemySea.getGraphics(), x, y, State.DAMAGE);
								else {
									enemySea.print(enemySea.getGraphics(), x, y, State.FOOL);
									jFrame.setTitle("Battleship: wait..");
								}
							}
							catch (IOException ex) {
								JOptionPane.showMessageDialog(jFrame, "Something failed",
												"Error", JOptionPane.ERROR_MESSAGE);
								log.fatal("Failed");
								myTurn = false;
							}

							if (isGameOver(enemyShips)) {
								JOptionPane.showMessageDialog(jFrame,
												"Congratulations! *fanfare sounds*",
												"Battleship", JOptionPane.INFORMATION_MESSAGE);
								myTurn = false;

								try {
									ois.close();
									oos.close();
									socket.close();
									System.exit(0);
								}
								catch (IOException ex) {
									JOptionPane.showMessageDialog(jFrame,
													"Failed while closing resources", "Error",
													JOptionPane.ERROR_MESSAGE);
									log.fatal("Failed");
									myTurn = false;
								}
							}
							if (!myTurn) {
								new Thread(() -> {

									while (!myTurn) {
										try {
											int x1 = ois.readInt();
											int y1 = ois.readInt();

											if (myShips[y1][x1] == State.SHIP) {
												mySea.print(mySea.getGraphics(), x1, y1, State.DAMAGE);

												if (isGameOver(myShips))
													JOptionPane.showMessageDialog(jFrame,
																	"You lost!",
																	"Battleship",
																	JOptionPane.INFORMATION_MESSAGE);
											}
											else if (myShips[y1][x1] == State.NOTHING) {
												mySea.print(mySea.getGraphics(), x1, y1, State.FOOL);
												myTurn = true;
												jFrame.setTitle("Battleship: yours");
											}
										} catch (IOException ex) {
											JOptionPane.showMessageDialog(jFrame,
															"Enemy left the game",
															"Battleship ended",
															JOptionPane.INFORMATION_MESSAGE);
											log.info("Player left the game");
											myTurn = false;
											System.exit(-1);
										}
									}
								}).start();
							}
						}
					}

					// === Метка ===
					if (e.getButton() == MouseEvent.BUTTON3) {
						if (enemyShips[y][x] == State.NOTHING)
							enemySea.print(enemySea.getGraphics(), x, y, State.LABEL);
						else if (enemyShips[y][x] == State.LABEL)
							enemySea.print(enemySea.getGraphics(), x, y, State.NOTHING);
					}
				}
			}
		});
		return jFrame;
	}

	//Функция возвращает меню
	private static JMenuBar getMenu() {
		JMenuBar jMenuBar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenu info = new JMenu("Help");

		// === Выход ===
		JMenuItem exit = file.add(new JMenuItem("Exit"));
		exit.addActionListener(e -> {
			if (JOptionPane.showConfirmDialog(
							jFrame,
							"Do you want to leave?",
							"Battleship",
							JOptionPane.YES_NO_OPTION) == 0) {
				System.exit(0);
			}
		});
		exit.setAccelerator(KeyStroke.getKeyStroke("ctrl E"));

		// === О приложении ===
		JMenuItem rules = info.add(new JMenuItem("About"));
		rules.addActionListener(e -> JOptionPane.showMessageDialog(
						jFrame,
						"Online game \"Battleship\"",
						"Battleship", JOptionPane.INFORMATION_MESSAGE));

		// === Об авторе ===
		JMenuItem author = info.add(new JMenuItem("Author"));
		author.addActionListener(e -> JOptionPane.showMessageDialog(
						jFrame,
						"Group: M8О-313b-17\nStudent: Vodyakov Aleksandr",
						"Battleship", JOptionPane.INFORMATION_MESSAGE));

		jMenuBar.add(file);
		jMenuBar.add(info);

		return jMenuBar;
	}

	// === Очистка поля ===
	private static void clearMap(Canvas mainMap) {
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++)
				mainMap.print(mainMap.getGraphics(), i, j, State.NOTHING);
		}
		shipsCount = 0;
		shipsPark.replace(1, 4);
		shipsPark.replace(2, 3);
		shipsPark.replace(3, 2);
		shipsPark.replace(4, 1);
	}

	// === Добавление корабля ===
	private static void addShip(State[][] ships, int x, int y, int shipLength,
															int orientation, Canvas canvas) {
		if ((shipsPark.get(shipLength) != 0)
						&& ships[y][x] != State.SHIP
						&& isValid(x, y, shipLength, orientation)
						&& !isCrossed(ships, x, y, shipLength, orientation)
						&& !isCollised(ships, x, y, shipLength, orientation)) {
			for (int i = 0; i < shipLength; i++) {
				canvas.print(canvas.getGraphics(), x, y, State.SHIP);
				if (orientation == 0)
					y++;
				else
					x++;
			}
			int shipsCounter = shipsPark.get(shipLength) - 1;

			shipsPark.replace(shipLength, shipsCounter);

			shipsCount++;
		}
	}

	// === Все ли корабли убиты? ===
	private static boolean isGameOver(State[][] ships) {
		int deadCells = 0;
		for (int i = 0; i < 10; ++i)
			for (int j = 0; j < 10; ++j)
				if (ships[i][j] == State.DAMAGE)
					deadCells++;
		return deadCells == 20;
	}

	//Проверка на выход за границы карты
	private static boolean isValid(int x, int y, int shipType, int orientation) {
		if (orientation == 1)
			return x + shipType - 1 < 10 && x >= 0 && y >= 0;
		return y + shipType - 1 < 10 && y >= 0 && x >= 0;
	}

	// === Проверка на пересечение с имеющимися кораблями ===
	private static boolean isCrossed(State[][] ships, int x, int y,
																	 int shipType, int orientation) {
		if (orientation == 1) {
			for (int i = x + 1; i < x + shipType; i++)
				if (ships[y][i] == State.SHIP)
					return true;
		}
		else
			for (int i = y + 1; i < y + shipType; i++)
				if (ships[i][x] == State.SHIP)
					return true;
		return false;
	}

	// === Проверка на коллизию ===
	private static boolean isCollised(State[][] ships, int x, int y,
																		int shipType, int orientation) {
		if (orientation == 1)
			for (int i = x; i < x + shipType; i++) {
				if (	 i - 1 >= 0 && (y - 1 >= 0 && ships[y - 1][i - 1] == State.SHIP
														|| ships[y][i - 1] == State.SHIP
														|| y + 1 < 10 && ships[y + 1][i - 1] == State.SHIP)
						|| y - 1 >= 0 && ships[y - 1][i] == State.SHIP
						|| y + 1 < 10 && ships[y + 1][i] == State.SHIP
						|| i + 1 < 10 && (y - 1 >= 0 && ships[y - 1][i + 1] == State.SHIP
														|| ships[y][i + 1] == State.SHIP
														|| y + 1 < 10 && ships[y + 1][i + 1] == State.SHIP))
					return true;
			}
		 else
			for (int i = y; i < y + shipType; i++)
				if (i - 1 >= 0 && (x - 1 >= 0 && ships[i - 1][x - 1] == State.SHIP
													|| (ships[i - 1][x] == State.SHIP
														|| x + 1 < 10 && ships[i - 1][x + 1] == State.SHIP))
						|| x - 1 >= 0 && ships[i][x - 1] == State.SHIP
						|| x + 1 < 10 && ships[i][x + 1] == State.SHIP
						|| i + 1 < 10 && (x - 1 >= 0 && ships[i + 1][x - 1] == State.SHIP
														|| ships[i + 1][x] == State.SHIP
														|| x + 1 < 10 && ships[i + 1][x + 1] == State.SHIP))
					return true;
		return false;
	}
}
