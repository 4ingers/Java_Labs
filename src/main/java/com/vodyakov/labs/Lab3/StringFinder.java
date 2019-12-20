package com.vodyakov.labs.Lab3;

import com.vodyakov.labs.Tools.ResourceHandler;
import com.vodyakov.labs.Tools.Main;
import me.tongfei.progressbar.ProgressBar;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;


class StringFinder {
	static void exec(String[] args) {

		final Logger log = Logger.getLogger(StringFinder.class);
		log.setLevel(Level.ALL);

		String innerFileName = args[0];
		String outerFileName = args[1];
		String token = args[4];
		int backwardOffset = 0;
		int forwardOffset = 0;
		int threadCount = 0;
		boolean succeed = true;
		try {
			backwardOffset = Integer.parseInt(args[2]);
			forwardOffset = Integer.parseInt(args[3]);
			threadCount = Integer.parseInt(args[5]);
		}
		catch (NumberFormatException e) {
			succeed = false;
		}
		if (backwardOffset < 0 || forwardOffset < 0)
			succeed = false;

		if (!succeed) {
			String msg = "Failed";
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
		}
		else if (!StringFinder.launch(innerFileName, outerFileName,
						backwardOffset, forwardOffset, token, threadCount)) {
			Main.out("\nSucceeded", Main.EnumOutType.INFO);
			log.info("Succeeded");
		}
	}

	private static boolean launch(String innerFileName, String outerFileName,
																int backwardOffset, int forwardOffset, String token,
																int threadCount) {

		final Logger log = Logger.getLogger(StringFinder.class);
		log.setLevel(Level.ALL);

		String fullFileName = ResourceHandler.getFullFilePath(innerFileName);
		if (!Optional.ofNullable(fullFileName).isPresent())
			return false;

		long fileLength = new File(fullFileName).length();
		long chunk = fileLength / threadCount;

		AtomicLong progress = new AtomicLong();
		AtomicLong matchCount = new AtomicLong();

		ProgressBar progressBar = new ProgressBar("Matcher", fileLength);

		ScheduledExecutorService progressExecutorService
						= Executors.newSingleThreadScheduledExecutor();
		progressExecutorService.scheduleAtFixedRate(() -> {
							progressBar.stepTo(progress.longValue());
							progressBar.getExtraMessage();
						},
						0, 100, TimeUnit.MILLISECONDS);


		List<Callable<ArrayList<ArrayList<String>>>> tasks = new ArrayList<>();
		for (int i = 0; i < threadCount; ++i) {
			long taskBegins = chunk * i;
			long taskEnds = fileLength - (chunk * (threadCount - i - 1));
			tasks.add(new SearchTask(taskBegins, taskEnds,
							fullFileName, token,
							backwardOffset, forwardOffset,
							progress, matchCount,
							progressBar));
		}

		ExecutorService executorService = Executors.newCachedThreadPool();
		List<Future<ArrayList<ArrayList<String>>>> results = null;
		try {
			results = executorService.invokeAll(tasks);
		}
		catch (InterruptedException e) {
			String msg = e.toString();
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
			executorService.shutdown();
			progressExecutorService.shutdown();
			progressBar.stepTo(fileLength);
			progressBar.close();
			return false;
		}

		for (Future<ArrayList<ArrayList<String>>> future : results) {
			try (Writer writer = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(outerFileName)))) {
				ArrayList<ArrayList<String>> object = future.get();
				if (Optional.ofNullable(object).isPresent()) {
					for (ArrayList<String> strings : object) {
						strings.forEach(string -> {
							try {
								writer.append(string).append(System.lineSeparator());
							}
							catch (IOException e) {
								String msg = e.toString();
								Main.out(msg, Main.EnumOutType.ERROR);
								log.fatal(msg);
								executorService.shutdown();
								progressExecutorService.shutdown();
								progressBar.stepTo(fileLength);
								progressBar.close();
							}
						});
						writer.append(System.lineSeparator());
					}
				}
			}
			catch (InterruptedException | ExecutionException | IOException e) {
				String msg = e.toString();
				Main.out(msg, Main.EnumOutType.ERROR);
				log.fatal(msg);
			}
		}
		executorService.shutdown();
		progressExecutorService.shutdown();
		progressBar.stepTo(fileLength);
		progressBar.close();
		return true;
	}
}
