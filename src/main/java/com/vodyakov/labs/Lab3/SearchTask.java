package com.vodyakov.labs.Lab3;

import com.vodyakov.labs.Tools.Main;
import me.tongfei.progressbar.ProgressBar;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

public class SearchTask implements Callable<ArrayList<ArrayList<String>>> {

	ArrayList<ArrayList<String>> matchList;
	ProgressBar progressBar;
	private long start;
	private long end;
	private String token;
	private int backwardOffset;
	private int forwardOffset;
	private AtomicLong progress;
	private AtomicLong matchCount;
	private boolean isValid = true;
	private RandomAccessFile raf;

	public SearchTask(long start,
										long end,
										String fileName,
										String token,
										int backwardOffset,
										int forwardOffset,
										AtomicLong progress,
										AtomicLong matchCount,
										ProgressBar progressBar) {
		try {
			raf = new RandomAccessFile(fileName, "r");
		}
		catch (FileNotFoundException e) {
			isValid = false;
		}
		this.start = start;
		this.end = end;
		this.token = token;
		this.backwardOffset = backwardOffset;
		this.forwardOffset = forwardOffset;
		this.progress = progress;
		this.matchCount = matchCount;
		this.progressBar = progressBar;
	}

	public boolean isValid() {
		return isValid;
	}

	@Override
	public ArrayList<ArrayList<String>> call() throws Exception {

		final Logger log = Logger.getLogger(SearchTask.class);
		log.setLevel(Level.ALL);

		if (!isValid())
			return null;

		matchList = new ArrayList<>();

		process();

		try {
			raf.close();
		}
		catch (IOException e) {
			String msg = e.toString();
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
			return null;
		}
		return matchList;
	}

	private void process() {

		final Logger log = Logger.getLogger(SearchTask.class);
		log.setLevel(Level.ALL);

		try {
			raf.seek(start);
		}
		catch (IOException e) {
			String msg = e.toString();
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
			return;
		}
		moveToStartCurrentLine();
		String currentLine;
		long currentByte = 0;

		try {
			currentByte = raf.getFilePointer();
		}
		catch (IOException e) {
			String msg = e.toString();
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
			return;
		}

		while (currentByte < end) {
			try {
				if (raf.getFilePointer() == raf.length())
					break;
				currentLine = raf.readLine();
				if (currentLine.contains(token)) {
					handleMatch();
					progressBar.setExtraMessage(String.format(
									"==> %d", matchCount.incrementAndGet()));
				}
				progress.getAndAdd(currentLine.length() + 2);
				currentByte = raf.getFilePointer();
			}
			catch (IOException e) {
				String msg = e.toString();
				Main.out(msg, Main.EnumOutType.ERROR);
				log.fatal(msg);
				return;
			}
		}
	}

	private void handleMatch() {

		final Logger log = Logger.getLogger(SearchTask.class);
		log.setLevel(Level.ALL);

		long checkpoint;
		try {
			checkpoint = raf.getFilePointer();
			accumulateMatch();
			raf.seek(checkpoint);
			raf.readLine();
		}
		catch (IOException e) {
			String msg = e.toString();
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
			return;
		}
	}

	private void accumulateMatch() {

		final Logger log = Logger.getLogger(SearchTask.class);
		log.setLevel(Level.ALL);

		moveToBackwardsOffset();
		ArrayList<String> accumulator = new ArrayList<>();
		for (int i = -backwardOffset; i <= forwardOffset; ++i) {
			try {
				Optional.ofNullable(raf.readLine())
								.ifPresent(accumulator::add);
			}
			catch (IOException e) {
				String msg = e.toString();
				Main.out(msg, Main.EnumOutType.ERROR);
				log.fatal(msg);
				break;
			}
		}
		if (!accumulator.isEmpty())
			matchList.add(accumulator);
	}

	private void moveToBackwardsOffset() {
		for (int i = 0; i <= backwardOffset && moveToStartPreviousLine(); ++i);
	}

	private boolean moveToStartPreviousLine() {

		final Logger log = Logger.getLogger(SearchTask.class);
		log.setLevel(Level.ALL);

		try {
			moveToStartCurrentLine();
			long currentPos = raf.getFilePointer();
			if (0 == currentPos) {
				return false;
			}
			raf.seek(currentPos - 2);
			moveToStartCurrentLine();
		}
		catch (IOException e) {
			String msg = e.toString();
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
			return false;
		}
		return true;
	}


	private void moveToStartCurrentLine() {

		final Logger log = Logger.getLogger(SearchTask.class);
		log.setLevel(Level.ALL);

		try {
			for (long i = raf.getFilePointer();
					 i > 0 && i != raf.length() && raf.readByte() != '\n';
					 i--)
				raf.seek(i);
		}
		catch (IOException e) {
			String msg = e.toString();
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
		}
	}
}

