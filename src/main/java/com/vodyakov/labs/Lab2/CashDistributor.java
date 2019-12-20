package com.vodyakov.labs.Lab2;

import com.vodyakov.labs.Tools.Main;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CashDistributor {

	static boolean exec(String configFileName) {

		final Logger log = Logger.getLogger(Cash.class);
		log.setLevel(Level.ALL);

		Cash cash = Lab2ConfigReader.getCashConfig(configFileName);
		if (null == cash) {
			String msg = "Config wasn't read";
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
			return false;
		}

		int amount = cash.getAmount();
		if (amount <= 0) {
			String msg = "Failed getting amount";
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
			return false;
		}

		List<Integer> coins = cash.getCoins();
		if (null == coins) {
			String msg = "Failed getting coins";
			Main.out(msg, Main.EnumOutType.ERROR);
			log.fatal(msg);
			return false;
		}

		List<Integer> distribution = new ArrayList<>();
		distribute(cash.getAmount(), cash.getCoins(), distribution);
		String result = Arrays.toString(distribution.toArray());

		if (result.equals("[]")) {
			String msg = "There's no possible exchange for this cash";
			Main.out(msg, Main.EnumOutType.INFO);
			log.info(msg);
			return true;
		}
		else {
			Main.out(result, Main.EnumOutType.INFO);
			log.info(result);
		}
		return true;
	}


	private static void distribute(int amount,
																 List<Integer> coins,
																 List<Integer> distribution) {
		while (!coins.isEmpty()) {
			Integer coin = coins.get(0);

			if (0 < amount - distribution.stream()
							.mapToInt(Integer::intValue).sum() - coin)
				distribute(amount - coin, new ArrayList<>(coins), distribution);

			if (0 == amount - distribution.stream()
							.mapToInt(Integer::intValue).sum() - coin)
				distribution.add(0, coin);
			else
				coins.remove(coin);
		}
	}
}
