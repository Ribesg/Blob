package fr.ribesg.blob.command.minecraft.mcstats;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.util.ArtUtil;
import fr.ribesg.alix.api.enums.Codes;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

public class GlobalMCStatsCommand extends Command {

	private static final Logger LOG = Logger.getLogger(GlobalMCStatsCommand.class.getName());

	private static final DecimalFormat formatter;

	static {
		formatter = new DecimalFormat();
		formatter.setMaximumFractionDigits(2);
		formatter.setGroupingSize(3);
		final DecimalFormatSymbols symbol = new DecimalFormatSymbols();
		symbol.setGroupingSeparator(' ');
		symbol.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(symbol);
	}

	public GlobalMCStatsCommand() {
		super("gstats");
	}

	@Override
	public void exec(final Server server, final Channel channel, final Source user, final String[] args) {
		if (args.length != 0 && args.length != 1) {
			nope(channel);
			return;
		}

		try {
			if (args.length == 0) {
				for (final String msg : new GlobalStats().getMessages()) {
					channel.sendMessage(msg);
				}
			} else {
				switch (args[0].toLowerCase()) {
					case "auth":
						channel.sendMessage(new AuthStats().getMessage());
					default:
						channel.sendMessage(Codes.RED + "Invalid argument.");
						break;
				}
			}
		} catch (final FileNotFoundException | MalformedURLException | IndexOutOfBoundsException | NumberFormatException |
				SocketTimeoutException e) {
			LOG.error("Could not contact MCStats API / or invalid response received", e);
			channel.sendMessage(Codes.RED + "Could not contact MCStats API / or invalid response received");
		} catch (final IOException e) {
			channel.sendMessage(Codes.RED + "Failed: " + e.getMessage());
		}
	}

	private String getPage(final String urlString) throws IOException {
		final URL url = new URL(urlString);

		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		connection.setUseCaches(false);

		final BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		final StringBuilder buffer = new StringBuilder();
		String line;

		while ((line = input.readLine()) != null) {
			buffer.append(line);
			buffer.append('\n');
		}

		final String page = buffer.toString();

		input.close();

		return page;
	}

	// !gstats
	private class GlobalStats {

		public final String serversAmount;
		public final String serversDiff;
		public final String serversMax;
		public final String serversMin;
		public final String serversAvg;
		public final String playersAmount;
		public final String playersDiff;
		public final String playersMax;
		public final String playersMin;
		public final String playersAvg;

		public GlobalStats() throws IOException {
			final String apiUrl = "http://api.mcstats.org/1.0/All+Servers/graph/Global+Statistics";
			final String jsonString = getPage(apiUrl);
			final JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
			final JsonObject data = jsonObject.getAsJsonObject("data");
			final JsonArray playersArray = data.getAsJsonArray("Players");
			final JsonArray serversArray = data.getAsJsonArray("Servers");

			// Players:

			// Convert the Json map to a reversed Java SortedMap
			final SortedMap<Long, Long> playersMap = new TreeMap<>(new Comparator<Long>() {

				@Override
				public int compare(final Long x, final Long y) {
					return -Long.compare(x, y);
				}
			});
			for (final JsonElement a : playersArray) {
				playersMap.put(a.getAsJsonArray().get(0).getAsLong(), a.getAsJsonArray().get(1).getAsLong());
			}

			long totalForAvg = 0, nbForAvg = 0;
			long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
			for (final Long value : playersMap.values()) {
				totalForAvg += value;
				nbForAvg++;
				if (value < min) {
					min = value;
				}
				if (value > max) {
					max = value;
				}
			}
			this.playersMin = formatter.format(min);
			this.playersMax = formatter.format(max);
			this.playersAvg = formatter.format(((double) totalForAvg) / ((double) nbForAvg));
			long lastAmount = playersMap.get(playersMap.lastKey());
			this.playersAmount = formatter.format(lastAmount);
			playersMap.remove(playersMap.lastKey());
			long diff = lastAmount - playersMap.get(playersMap.lastKey());
			if (diff > 0) {
				this.playersDiff = Codes.GREEN + Codes.BOLD + "+" + diff + Codes.RESET;
			} else if (diff == 0) {
				this.playersDiff = Codes.GRAY + Codes.BOLD + "±" + diff + Codes.RESET;
			} else {
				this.playersDiff = Codes.RED + Codes.BOLD + diff + Codes.RESET;
			}

			// Same thing for Servers:

			// Convert the Json map to a reversed Java SortedMap
			final SortedMap<Long, Long> serversMap = new TreeMap<>(new Comparator<Long>() {

				@Override
				public int compare(final Long x, final Long y) {
					return -Long.compare(x, y);
				}
			});
			for (final JsonElement a : serversArray) {
				serversMap.put(a.getAsJsonArray().get(0).getAsLong(), a.getAsJsonArray().get(1).getAsLong());
			}

			totalForAvg = 0;
			nbForAvg = 0;
			min = Long.MAX_VALUE;
			max = Long.MIN_VALUE;
			for (final Long value : serversMap.values()) {
				totalForAvg += value;
				nbForAvg++;
				if (value < min) {
					min = value;
				}
				if (value > max) {
					max = value;
				}
			}
			this.serversMin = formatter.format(min);
			this.serversMax = formatter.format(max);
			this.serversAvg = formatter.format(((double) totalForAvg) / ((double) nbForAvg));
			lastAmount = serversMap.get(serversMap.lastKey());
			this.serversAmount = formatter.format(lastAmount);
			serversMap.remove(serversMap.lastKey());
			diff = lastAmount - serversMap.get(serversMap.lastKey());
			if (diff > 0) {
				this.serversDiff = Codes.GREEN + Codes.BOLD + "+" + diff + Codes.RESET;
			} else if (diff == 0) {
				this.serversDiff = Codes.GRAY + Codes.BOLD + "±" + diff + Codes.RESET;
			} else {
				this.serversDiff = Codes.RED + Codes.BOLD + diff + Codes.RESET;
			}

		}

		public String[] getMessages() {
			final String[] res = new String[4];

			String serversMessage1 = Codes.BOLD + "Servers: " + Codes.RESET;
			serversMessage1 += "Current: " + Codes.BLUE + Codes.BOLD + this.serversAmount + Codes.RESET;
			serversMessage1 += " (" + this.serversDiff + ")";
			res[0] = serversMessage1;

			String serversMessage2 = Codes.BOLD + "Servers: " + Codes.RESET;
			serversMessage2 += "Min: " + Codes.BLUE + Codes.BOLD + this.serversMin + Codes.RESET;
			serversMessage2 += " | Max: " + Codes.BLUE + Codes.BOLD + this.serversMax + Codes.RESET;
			serversMessage2 += " | Average: " + Codes.BLUE + Codes.BOLD + this.serversAvg + Codes.RESET;
			res[1] = serversMessage2;

			String playersMessage1 = Codes.BOLD + "Players: " + Codes.RESET;
			playersMessage1 += "Current: " + Codes.BLUE + Codes.BOLD + this.playersAmount + Codes.RESET;
			playersMessage1 += " (" + this.playersDiff + ")";
			res[2] = playersMessage1;

			String playersMessage2 = Codes.BOLD + "Players: " + Codes.RESET;
			playersMessage2 += "Min: " + Codes.BLUE + Codes.BOLD + this.playersMin + Codes.RESET;
			playersMessage2 += " | Max: " + Codes.BLUE + Codes.BOLD + this.playersMax + Codes.RESET;
			playersMessage2 += " | Average: " + Codes.BLUE + Codes.BOLD + this.playersAvg + Codes.RESET;
			res[3] = playersMessage2;

			return res;
		}
	}

	// !gstats auth
	private class AuthStats {

		public final double authOnPercentage;
		public final double authOffPercentage;

		public AuthStats() throws IOException {
			final String authUrl = "http://api.mcstats.org/1.0/All+Servers/graph/Auth+Mode";
			final String jsonString = getPage(authUrl);
			final JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
			final JsonArray data = jsonObject.getAsJsonArray("data");
			final JsonArray firstArray = data.get(0).getAsJsonArray();
			final JsonArray secondArray = data.get(1).getAsJsonArray();
			final String firstArrayString1 = firstArray.get(0).getAsString();
			final String firstArrayString2 = firstArray.get(1).getAsString();
			final String secondArrayString2 = secondArray.get(1).getAsString();

			if (firstArrayString1.contains("Online")) {
				// First is Online, Second is Offline
				this.authOnPercentage = Double.parseDouble(firstArrayString2);
				this.authOffPercentage = Double.parseDouble(secondArrayString2);
			} else {
				// First is Offline, Second is Online
				this.authOffPercentage = Double.parseDouble(firstArrayString2);
				this.authOnPercentage = Double.parseDouble(secondArrayString2);
			}
		}

		public String getMessage() {
			return ArtUtil.asciiBar(this.authOnPercentage, Codes.GREEN, this.authOffPercentage, Codes.RED, 20, '█', '|', Codes.GRAY);
		}
	}

	/*
	 TODO
	 // !gstats java
	 // !gstats os
	 // !gstats arch
	 // !gstats cores
	 // !gstats location
	 // !gstats version
	 // !gstats software

	 */
	private void nope(final Channel channel) {
		channel.sendMessage(Codes.RED + "Global MCStats stats with !gstats [auth]");
	}
}