package com.vodyakov.labs.Lab5;

import com.vodyakov.labs.Tools.Main;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;


public class Lab05 {

	private static List<Student> students = new ArrayList<>();
	private static List<Group> groups = new ArrayList<>();
	private static List<Subject> subjects = new ArrayList<>();
	private static List<Rating> ratings = new ArrayList<>();

	private static String cacheA = "";
	private static String cacheB = "";
	private static String cacheC = "";
	private static String cachePrint = "";

	public static void main(String[] arg) {

		final Logger log = Logger.getLogger(Lab05.class);
		log.setLevel(Level.ALL);


		String studentsPath = Main.getPath("lab5students.txt");
		String subjectsPath = Main.getPath("lab5subjects.txt");
		String groupsPath = Main.getPath("lab5groups.txt");
		String marksPath = Main.getPath("lab5marks.txt");

		String[] args = {studentsPath, subjectsPath, groupsPath, marksPath, "-h"};

		if (!Main.batchArgs(args, 4,
						"Tables batch processing.\nTables are text files: every record is a string\n(values are splitted " +
										"by spaces).\n\nArguments:\n  - students table;\n  - subjects table;\n  - groups table;\n" +
										"  - marks table.\n\n"))
			return;

		log.info("STARTED");

		setUpTables(args);

		// === Последнее обновление файлов ===
		long[] lastUpdates = new long[4];
		for (int i = 0; i < 4; ++ i)
			lastUpdates[i] = new File(args[i]).lastModified();

		// === Чтение ввода ===
		Scanner scanner = new Scanner(System.in);
		String input;
		String defaultMeal = "Enter:\n" +
						"> 'a'||'b'||'c' -- to execute process\n" +
						"> \"print\" -- to print raw tables\n" +
						"> \"exit\" -- to stay true\n";
		try {
			Main.out(defaultMeal);
			while (true) {
				System.out.print(">> ");
				input = scanner.nextLine();

				// === Проверка на изменения файлов ===
				for (int i = 0; i < 4; ++ i)
					if (lastUpdates[i] != new File(args[i]).lastModified()) {
						cacheA = "";
						cacheB = "";
						cacheC = "";
						cachePrint = "";

						String msg = "Files were changed";
						Main.out(msg, Main.EnumOutType.INFO);
						log.info(msg);
						break;
					}
				switch (input) {
					case "a":
						Main.out(callA());
						break;
					case "b":
						Main.out(callB());
						break;
					case "c":
						Main.out(callC());
						break;
					case "print":
						Main.out(printAll());
						break;
					case "exit":
						scanner.close();
						return;
					default:
						Main.out("Напишите a, b или c для выполнения укзанного в задании запроса,\n"
										+ "или же print, что бы написать содержимое всех таблиц в сыром виде.\n"
										+ "Что бы выйти, напишите exit. Прочие входные строки будут игнорироваться.");
						break;
				}
			}
		} catch (IllegalStateException e) {
			String msg = "\"Menu\" threw exception";
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
			scanner.close();
		}
	}

	// =====================
	// === Чтение таблиц ===
	// =====================
	private static void setUpTables(String[] args) {
		final Logger log = Logger.getLogger(Lab05.class);
		log.setLevel(Level.ALL);

		BufferedReader file;
		String line;
		String[] strings;

		// === Ученики ===
		try {
			file = new BufferedReader(new FileReader(args[0]));

			while (file.ready()) {
				line = file.readLine();
				if (line.isEmpty())
					continue;
				strings = line.split(" ");
				students.add(new Student(Integer.parseInt(strings[0]), strings[1]));
			}
			file.close();
		} catch (Exception e) {
			String msg = "Reading \"students\" failed";
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
			return;
		}

		// === Предметы ===
		try {
			file = new BufferedReader(new FileReader(args[1]));

			while (file.ready()) {
				line = file.readLine();
				if (line.isEmpty())
					continue;
				strings = line.split(" ");
				subjects.add(new Subject(Integer.parseInt(strings[0]), strings[1]));
			}
			file.close();
		} catch (Exception e) {
			String msg = "Reading \"subjects\" failed";
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
			return;
		}

		// === Группы ===
		try {
			file = new BufferedReader(new FileReader(args[2]));
			Group group;

			while (file.ready()) {
				line = file.readLine();
				if (line.isEmpty())
					continue;
				strings = line.split(" ");
				group = new Group(Integer.parseInt(strings[0]), strings[1]);
				groups.add(group);

				// === Список учеников ===
				for (int i = 2; i < strings.length; ++ i)
					group.studentKeys.add(getStudentById(strings[i]));
			}
			file.close();
		} catch (Exception e) {
			String msg = "Reading \"groups\" failed";
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
			return;
		}

		// === Оценки ===
		try {
			file = new BufferedReader(new FileReader(args[3]));

			while (file.ready()) {
				line = file.readLine();
				if (line.isEmpty())
					continue;
				strings = line.split(" ");
				ratings.add(new Rating(getSubjectById(strings[0]), getStudentById(strings[1]),
								Integer.parseInt(strings[2]), strings[3]));
			}

			file.close();
		} catch (Exception e) {
			String msg = "Reading \"marks\" failed";
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
		}
	}

	// ==============
	// === Печать ===
	// ==============

	private static String printAll() {

		if (! cachePrint.equals(""))
			return "<CACHE>\n" + cachePrint + "\n</CACHE>\n";

		StringBuilder out = new StringBuilder();

		out.append("\nGroups:\n");

		for (Group group : groups) {
			out.append(group.id).append(" ").append(group.name).append(" ").append(group.studentKeys.size());
			for (int i = 0; i < group.studentKeys.size(); ++ i)
				out.append(group.studentKeys.get(i).id);
			out.append('\n');
		}

		out.append("\nStudents:\n");

		for (Student student : students)
			out.append(student.id).append(" ").append(student.name).append('\n');

		out.append("\nSubjects:\n");

		for (Subject subject : subjects)
			out.append(subject.id).append(" ").append(subject.name).append('\n');

		out.append("\nMarks:\n");

		for (Rating rating : ratings)
			out.append(rating.id).append(" ").append(rating.rating).append(" ").append(rating.studentKey.id).append(" ")
							.append(rating.subjectKey.id).append(" ").append(rating.date).append('\n');

		cachePrint = out.toString();
		return cachePrint;
	}

	// ================
	// === Запрос A ===
	// ================
	private static String callA() {

		if (! Objects.equals(cacheA, ""))
			return "\n<CACHE>\n" + cacheA + "</CACHE>\n";

		StringBuilder out = new StringBuilder();
		out.append('\n');

		Student[] top = new Student[5];
		double[] top_data = new double[5];
		double curAvg, groupAvg = 0;
		int n;

		// Цикл по группам
		for (Group group : groups) {
			// --- Первое место на нулевом индексе ---
			for (int i = 0; i < 5; ++ i) {
				top[i] = null;
				top_data[i] = 0;
			}

			// --- Цикл по ученикам ---
			for (Student student : group.studentKeys) {
				curAvg = 0;
				n = 1;

				// --- Цикл по оценкам ---
				for (Rating rating : ratings)
					if (rating.studentKey == student)
						curAvg += (rating.rating - curAvg) / n++;

				curAvg = Math.floor(curAvg * 100) / 100;

				// --- Обновление топа ---
				for (int i = 0; i < 5; ++ i) {

					if (curAvg > top_data[i]) {
						for (int j = 4; j > i; -- j) {
							top_data[j] = top_data[j - 1];
							top[j] = top[j - 1];
						}
						top_data[i] = curAvg;
						top[i] = student;
						break;
					}
				}

				groupAvg += curAvg;

			}

			// === Среднее по группе ===
			groupAvg = Math.floor(groupAvg / group.studentKeys.size() * 100) / 100;

			out.append(group.name).append(": ").append(groupAvg).append("\n");

			// === Вывод топа ===
			for (int i = 0; i < 5; ++ i) {
				if (top[i] == null)
					break;
				else
					out.append(top[i].name).append(" - ").append(top_data[i]).append("\n");
			}
			out.append('\n');
			groupAvg = 0;
		}

		cacheA = out.toString();
		return cacheA;
	}

	// ================
	// === Запрос B ===
	// ================

	private static String callB() {

		if (! cacheB.equals(""))
			return "\n<CACHE>\n" + cacheB + "</CACHE>\n";

		StringBuilder out = new StringBuilder();
		out.append('\n');

		Group[] top = new Group[5];
		double[] top_data = new double[5];
		double n;

		// --- Цикл по группам ---
		for (Group group : groups) {
			n = 0;
			// --- Цикл по ученикам ---
			for (Student student : group.studentKeys) {
				// --- Цикл по оценкам ---
				List<Student> m = new ArrayList<>(group.studentKeys);
				for (Rating rating : ratings)
					if (rating.rating == 2 && rating.studentKey == student && m.contains(student)) {
						++ n;
						m.remove(student);
					}
			}
			n /= group.studentKeys.size();
			n = Math.floor(n * 100);

			// --- Обновление топа ---
			for (int i = 0; i < 5; ++ i) {

				if (n > top_data[i]) {
					for (int j = 4; j > i; -- j) {
						top_data[j] = top_data[j - 1];
						top[j] = top[j - 1];
					}
					top_data[i] = n;
					top[i] = group;
					break;
				}
			}
		}

		// --- Вывод топа ---
		for (int i = 0; i < 5; ++ i) {
			if (top[i] == null)
				break;
			else
				out.append(top[i].name).append(" - ").append(Math.round(top_data[i] * top[i].studentKeys.size() / 100))
								.append(" ").append(top_data[i]).append("%\n");
		}
		out.append('\n');

		cacheB = out.toString();
		return cacheB;
	}

	// ================
	// === Запрос C ===
	// ================

	private static String callC() {

		if (! cacheC.equals(""))
			return "\n<CACHE>\n" + cacheC + "</CACHE>\n";

		StringBuilder out = new StringBuilder();
		out.append('\n');

		Subject[] top = new Subject[5];
		double[] top_data = new double[5];
		String[] top_out = new String[5];
		StringBuilder line;
		double curAvg;
		double n;

		for (int i = 0; i < 5; ++ i) {
			top[i] = null;
			top_data[i] = 0;
			top_out[i] = "";
		}

		// --- Цикл по предметам ---
		for (Subject subject : subjects) {

			// --- Первое место на нулевом индексе ---

			curAvg = 0;
			line = new StringBuilder();

			// --- Цикл по группам ---
			for (Group group : groups) {
				n = 0;

				// --- Цикл по оценкам ---
				for (Rating rating : ratings)
					if (rating.subjectKey == subject && group.studentKeys.contains(rating.studentKey) && rating.rating != 2)
						++ n;

				n /= group.studentKeys.size();
				n = Math.floor(n * 100);
				curAvg += n;

				line.append("  ").append(group.name).append(" ").append(n).append("%\n");
			}

			curAvg /= groups.size();
			curAvg = Math.floor(curAvg * 100) / 100;
			line.insert(0, subject.name + ": " + curAvg + "%\n");

			// --- Обновление топа ---
			for (int i = 0; i < 5; ++ i) {
				if (curAvg > top_data[i]) {
					for (int j = 4; j > i; -- j) {
						top_data[j] = top_data[j - 1];
						top[j] = top[j - 1];
					}
					top_data[i] = curAvg;
					top_out[i] = line.toString();
					top[i] = subject;
					break;
				}
			}
		}

		// --- Вывод топа ---
		for (int i = 0; i < 5; ++ i) {
			if (top[i] == null) {
				break;
			} else {
				out.append(top_out[i]);
			}
		}
		out.append('\n');

		cacheC = out.toString();
		return cacheC;
	}

	// ==============================
	// === Вспомогательные методы ===
	// ==============================

	private static Student getStudentById(String id) {
		int studId = Integer.parseInt(id);
		for (Student i : students)
			if (i.id == studId)
				return i;
		return null;
	}

	private static Subject getSubjectById(String id) {
		int subId = Integer.parseInt(id);
		for (Subject i : subjects)
			if (i.id == subId)
				return i;
		return null;
	}


	// ==============
	// === Классы ===
	// ==============

	private static class Object {
		int id = 0;
	}

	private static class Student extends Object {
		String name;

		Student(int id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	private static class Group extends Object {
		String name;
		List<Student> studentKeys = new ArrayList<>();

		Group(int id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	private static class Subject extends Object {
		String name;

		Subject(int id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	private static class Rating extends Object {
		Subject subjectKey;
		Student studentKey;
		int rating;
		String date;

		Rating(Subject subjectKey, Student studentKey, int rating, String date) {
			this.subjectKey = subjectKey;
			this.studentKey = studentKey;
			this.rating = rating;
			this.date = date;
		}
	}
}
