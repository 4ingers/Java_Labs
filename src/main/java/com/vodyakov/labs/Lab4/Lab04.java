package com.vodyakov.labs.Lab4;

import com.vodyakov.labs.Tools.Main;
import com.vodyakov.labs.Tools.Main.EnumOutType;
import me.tongfei.progressbar.ProgressBar;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Lab04 {
	public static void main(String[] arg) {
		final Logger log = Logger.getLogger(Lab04.class);
		log.setLevel(Level.ALL);

		String[] args = {"input=lab4.txt", "8", "-h"};

		log.info("STARTED");

		if (!Main.batchArgs(args, 2,
						"Calculating the deviation of the queries execution speed from the input log.\nEach line of the" +
										" log has the following pattern: \n  YYYY-MM-DD HH:MM:SS STATE NAME\nSTATE == BEGIN || END" +
										"\nPattern mismatches are not processed.\nNot ended processes are also ignored.\n\nArguments:\n" +
										"  - input file;\n  - time limit (seconds)\n\n"))
			return;

		// === Список процессов ===
		Map<String, Long> procList = new HashMap<>();
		// === Крайнее время ===
		long limit;
		try {
			limit = Integer.parseInt(args[1]) * 1000;
		} catch (NumberFormatException e) {
			String msg = "LIMIT is NaN-value";
			Main.out(msg, EnumOutType.ERROR);
			log.fatal(msg);
			return;
		}
		if (limit < 0) {
			String msg = "Negative LIMIT";
			Main.out(msg, EnumOutType.ERROR);
			log.fatal(msg);
		}
		// === Среднее время работы всех процессов ===
		long curAvg = 0;
		// === Размер файла ===
		long procLength = 0;

		StringBuilder out = new StringBuilder();

		// ======================================================================================
		try {
			String line;
			Calendar calendar;
			File f = new File(args[0]);
			LineNumberReader file = new LineNumberReader(new FileReader(f));
			Pattern pattern = Pattern.compile(
							"^(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)\\s(\\d\\d):(\\d\\d):(\\d\\d)\\s(\\w*)\\s(.*)");

			ProgressBar progressBar = new ProgressBar("Dispersion", f.length());
			log.info("ProgressBar started");

			int n = 1;

			Main.out("LIMIT: " + limit/1000 + " seconds");

			// === Чтение файла ===
			while (file.ready()) {
				line = file.readLine();

				if (line.length() == 0)
					continue;

				procLength += line.length();

				// === RegEx ===
				Matcher matcher = pattern.matcher(line);
				if (matcher.find())

					if (matcher.groupCount() != 8) {
						String msg = "Pattern mismatch at " + file.getLineNumber();
						Main.out(msg, EnumOutType.WARNING);
						log.warn(msg);
						continue;
					} else {
						// === Достаём дату ===
						try {
							calendar = Calendar.getInstance();
							calendar.set(
											Integer.parseInt(matcher.group(1)),
											Integer.parseInt(matcher.group(2)),
											Integer.parseInt(matcher.group(3)),
											Integer.parseInt(matcher.group(4)),
											Integer.parseInt(matcher.group(5)),
											Integer.parseInt(matcher.group(6)));
						} catch (NumberFormatException e) {
							String msg = "Date-pattern mismatch at " + file.getLineNumber();
							Main.out(msg, EnumOutType.WARNING);
							log.warn(msg);
							continue;
						}
						// === Получение состояния и действие над процессом в зависимости от состояния ===
						if (matcher.group(7).equals("START"))
							procList.put(matcher.group(8), calendar.getTime().getTime());

						else if (matcher.group(7).equals("END")) {
							// === Разница во времени выполнения между началом и окончанием ===
							Long h = procList.get(matcher.group(8));
							if (h == null) {
								String msg = "Not started process ended at " + file.getLineNumber() + "))";
								Main.out(msg, EnumOutType.WARNING);
								log.warn(msg);
								continue;
							}

							long delta = (calendar.getTime().getTime() - h);

							// Сравнение с допустимым пределом времени
							if (delta > limit)
								out.append(" - ").append(matcher.group(8)).append(' ').append(delta).append('\n');

							// Пересчёт среднего времени
							curAvg += (delta - curAvg) / n++;

							procList.remove(matcher.group(8));
						} else {
							String msg = "!(START || END) at " + file.getLineNumber();
							Main.out(msg, EnumOutType.WARNING);
							log.warn(msg);
							continue;
						}
					}
				else {
					String msg = "Pattern mismatch at " + file.getLineNumber();
					Main.out(msg, EnumOutType.WARNING);
					log.warn(msg);
					continue;
				}
				progressBar.stepTo(procLength);
			}

			progressBar.stepTo(f.length());
			progressBar.close();
		} catch (IOException e) {
			String msg = "Reading input file failed";
			Main.out(msg, EnumOutType.ERROR);
			log.fatal(msg);
			return;
		}

		Main.out("Heaviest processes:\n" + out);
		Main.out("\nAverage working time: " + curAvg/1000 + " seconds");
		log.info("ENDED: " + curAvg);
	}
}
