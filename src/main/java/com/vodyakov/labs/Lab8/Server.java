package com.vodyakov.labs.Lab8;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private static ServerSocket serverSocket;
	private static Socket client1socket;
	private static Socket client2socket;
	private static boolean player1turn = true;

	private static ObjectOutputStream player1oos;
	private static ObjectOutputStream player2oos;
	private static ObjectInputStream player1ois;
	private static ObjectInputStream player2ois;

	public static void main(String[] args) {
		try {
			serverSocket = new ServerSocket(5050, 2);

			client1socket = serverSocket.accept();
			player1ois = new ObjectInputStream(client1socket.getInputStream());
			player1oos = new ObjectOutputStream(client1socket.getOutputStream());
			player1oos.writeBoolean(false);
			player1oos.flush();
			State[][] client1map = (State[][])player1ois.readObject();

			client2socket = serverSocket.accept();
			player2ois = new ObjectInputStream(client2socket.getInputStream());
			player2oos = new ObjectOutputStream(client2socket.getOutputStream());
			player2oos.writeBoolean(false);
			player2oos.flush();
			State[][] client2map = (State[][])player2ois.readObject();

			player1oos.writeBoolean(true);
			player1oos.flush();
			player1oos.writeBoolean(true);
			player1oos.flush();

			player2oos.writeBoolean(true);
			player2oos.flush();
			player2oos.writeBoolean(false);
			player2oos.flush();

			while (gameGoesOn(client1map) && gameGoesOn(client2map)) {
				do {
					int x = player1ois.readInt();
					int y = player1ois.readInt();
					if (client2map[y][x] == State.SHIP) {
						client2map[y][x] = State.DAMAGE;
						player1oos.writeBoolean(true);
						player1oos.flush();
					} else if (client2map[y][x] == State.NOTHING) {
						client2map[y][x] = State.FOOL;
						player1oos.writeBoolean(false);
						player1oos.flush();
						player1turn = false;
					}
					player2oos.writeInt(x);
					player2oos.flush();
					player2oos.writeInt(y);
					player2oos.flush();
				} while (player1turn);

				do {
					int x = player2ois.readInt();
					int y = player2ois.readInt();
					if (client1map[y][x] == State.SHIP) {
						client1map[y][x] = State.DAMAGE;
						player2oos.writeBoolean(true);
						player2oos.flush();
					} else if (client1map[y][x] == State.NOTHING) {
						client1map[y][x] = State.FOOL;
						player2oos.writeBoolean(false);
						player2oos.flush();
						player1turn = true;
					}
					player1oos.writeInt(x);
					player1oos.flush();
					player1oos.writeInt(y);
					player1oos.flush();
				} while (!player1turn);
			}

			player1ois.close();
			player2ois.close();
			player1oos.close();
			player2oos.close();
		} catch (IOException | ClassNotFoundException e) {
			System.out.println(e);
		}
		try {
			client1socket.close();
			client2socket.close();
			serverSocket.close();
		} catch (IOException ignored) {
		}
	}

	//Функция проверки живых кораблей
	private static boolean gameGoesOn(State[][] ships) {
		int deadCells = 0;
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				if (ships[i][j] == State.DAMAGE)
					deadCells++;
			}
		}
		return deadCells != 20;
	}
}
