package fr.ribesg.blob.command.minecraft.mcstats;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Log;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.command.CommandManager;
import fr.ribesg.alix.api.bot.util.ArtUtil;
import fr.ribesg.alix.api.bot.util.WebUtil;
import fr.ribesg.alix.api.enums.Codes;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MCStatsCommand extends Command {

	public MCStatsCommand(final CommandManager manager) {
		super(manager, "stats", new String[] {
				"Look up a BukkitDev Plugin statistics from MCStats",
				"Usage: ## <name>"
		});
	}

	@Override
	public void exec(final Server server, final Channel channel, final Source user,final String primaryArgument,  final String[] args) {
		final Receiver receiver = channel == null ? user : channel;

		if (args.length != 1) {
			sendUsage(receiver);
			return;
		}

		try {
			final List<String> messages = new ArrayList<>();

			final String mcStatsURL = "http://mcstats.org/plugin/";
			final String pluginStatsURL = mcStatsURL + args[0];
			final Document doc = WebUtil.parseHtml(WebUtil.get(pluginStatsURL));
			final PluginStats stats = PluginStats.get(doc);

			try {
				final String globalStatsJsonString = WebUtil.get("http://api.mcstats.org/1.0/" + stats.name + "/graph/Global+Statistics");
				stats.getMaxAverage(new JsonParser().parse(globalStatsJsonString).getAsJsonObject());
			} catch (Exception e) {
				Log.error(e.getMessage(), e);
				stats.serversMax = "?";
				stats.playersMax = "?";
				stats.serversAverage = "?";
				stats.playersAverage = "?";
			}
			
			String shortUrl;
			try {
				shortUrl = WebUtil.shortenUrl(pluginStatsURL);
			}catch(final IOException e) {
				shortUrl = pluginStatsURL;
			}
			messages.add(Codes.BOLD + "MCStats " + Codes.GREEN + stats.name + Codes.RESET + " - Rank: " + Codes.BOLD + stats.rank + Codes.RESET + " (" + colorizeDiff(stats.rankDiff, true) + ") - " + shortUrl);
			messages.add(Codes.UNDERLINE + "Servers|" + Codes.RESET + " Now: " + Codes.BOLD + stats.servers + Codes.RESET + " | Diff: " + colorizeDiff(stats.serversDiff, false) + " | Max: " + Codes.LIGHT_GREEN + stats.serversMax + Codes.RESET + " | Month: ~" + Codes.LIGHT_GREEN + stats.serversAverage);
			messages.add(Codes.UNDERLINE + "Players|" + Codes.RESET + " Now: " + Codes.BOLD + stats.players + Codes.RESET + " | Diff: " + colorizeDiff(stats.playersDiff, false) + " | Max: " + Codes.LIGHT_GREEN + stats.playersMax + Codes.RESET + " | Month: ~" + Codes.LIGHT_GREEN + stats.playersAverage);

			final String authModeJsonString = WebUtil.get("http://api.mcstats.org/1.0/" + stats.name + "/graph/Auth+Mode");
			if (!authModeJsonString.contains("NO DATA")) {
				final JsonObject authModeJson = new JsonParser().parse(authModeJsonString).getAsJsonObject();
				final JsonArray array = authModeJson.getAsJsonArray("data");

				final int offlineModeIndex = array.get(0).toString().contains("Online") ? 1 : 0;
				final int onlineModeIndex = offlineModeIndex == 1 ? 0 : 1;

				String offlineModeAmount = array.get(offlineModeIndex).getAsJsonArray().get(0).getAsString().substring(9);
				offlineModeAmount = offlineModeAmount.substring(0, offlineModeAmount.length() - 1);
				final String offlineModePercentage = array.get(offlineModeIndex).getAsJsonArray().get(1).getAsString();

				String onlineModeAmount = array.get(onlineModeIndex).getAsJsonArray().get(0).getAsString().substring(8);
				onlineModeAmount = onlineModeAmount.substring(0, onlineModeAmount.length() - 1);
				final String onlineModePercentage = array.get(onlineModeIndex).getAsJsonArray().get(1).getAsString();

				final double left = Double.parseDouble(onlineModePercentage);
				final double right = Double.parseDouble(offlineModePercentage);

				messages.add("Auth: " + ArtUtil.asciiBar(left, Codes.GREEN, right, Codes.RED, 20, '█', '|', Codes.GRAY) + " | " + Codes.GREEN + onlineModePercentage + "% (" + onlineModeAmount + ")" + Codes.RESET + " - " + Codes.RED + offlineModePercentage + "% (" + offlineModeAmount + ")");
			} else {
				messages.add("Sorry, no auth information :-(");
			}

			messages.forEach(receiver::sendMessage);
		} catch (final FileNotFoundException | MalformedURLException | IndexOutOfBoundsException | NumberFormatException e) {
			Log.info("No stats found for plugin " + args[0], e);
			receiver.sendMessage(Codes.RED + "No stats found for plugin " + args[0]);
		} catch (final IOException e) {
			receiver.sendMessage(Codes.RED + "Failed: " + e.getMessage());
		}
	}

	private static class PluginStats {

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

		public String name;
		public String rank;
		public String rankDiff;
		public String servers;
		public String serversDiff;
		public String serversMax;
		public String serversAverage;
		public String players;
		public String playersDiff;
		public String playersMax;
		public String playersAverage;

		private PluginStats() {}

		public void getMaxAverage(final JsonObject json) {
			final JsonObject data = json.getAsJsonObject("data");
			final JsonArray players = data.getAsJsonArray("Players");
			final Iterator<JsonElement> itPlayers = players.iterator();
			long maxPlayers = 0;
			double totalPlayers = 0, nbPlayers = 0;
			while (itPlayers.hasNext()) {
				long amountPlayers = itPlayers.next().getAsJsonArray().get(1).getAsLong();
				if (amountPlayers > maxPlayers) {
					maxPlayers = amountPlayers;
				}
				totalPlayers += amountPlayers;
				nbPlayers++;
			}
			this.playersMax = formatter.format(maxPlayers);
			this.playersAverage = formatter.format(totalPlayers / nbPlayers);
			final JsonArray servers = data.getAsJsonArray("Servers");
			final Iterator<JsonElement> itServers = servers.iterator();
			long maxServers = 0;
			double totalServers = 0, nbServers = 0;
			while (itServers.hasNext()) {
				long amountServers = itServers.next().getAsJsonArray().get(1).getAsLong();
				if (amountServers > maxServers) {
					maxServers = amountServers;
				}
				totalServers += amountServers;
				nbServers++;
			}
			this.serversMax = formatter.format(maxServers);
			this.serversAverage = formatter.format(totalServers / nbServers);
		}

		public static PluginStats get(final Document doc) {
			final PluginStats res = new PluginStats();
			res.name = doc.getElementsByClass("open").get(0).getElementsByTag("strong").get(0).ownText().trim();

			final Element statBoxes = doc.getElementsByClass("stat-boxes").get(0);

			final Element rankLi = statBoxes.child(0);
			final Element serversLi = statBoxes.child(1);
			final Element playersLi = statBoxes.child(2);

			final Element rankLiStrong = rankLi.getElementsByClass("right").get(0).getElementsByTag("strong").get(0);
			if (rankLiStrong.children().size() == 0) {
				res.rank = formatter.format(Long.valueOf(rankLiStrong.ownText().trim().replace(",", "")));
			} else {
				res.rank = formatter.format(Long.valueOf(rankLiStrong.child(0).ownText().trim().replace(",", "")));
			}
			res.rankDiff = rankLi.getElementsByClass("left").get(0).ownText().trim();
			res.rankDiff = res.rankDiff.replace("&plusmn;", "±");

			final Element serversLiStrong = serversLi.getElementsByClass("right").get(0).getElementsByTag("strong").get(0);
			if (serversLiStrong.children().size() == 0) {
				res.servers = formatter.format(Long.valueOf(serversLiStrong.ownText().trim().replace(",", "")));
			} else {
				res.servers = formatter.format(Long.valueOf(serversLiStrong.child(0).ownText().trim().replace(",", "")));
			}
			res.serversDiff = serversLi.getElementsByClass("left").get(0).ownText().trim();
			res.serversDiff = res.serversDiff.replace("&plusmn;", "±");

			final Element playersLiStrong = playersLi.getElementsByClass("right").get(0).getElementsByTag("strong").get(0);
			if (playersLiStrong.children().size() == 0) {
				res.players = formatter.format(Long.valueOf(playersLiStrong.ownText().trim().replace(",", "")));
			} else {
				res.players = formatter.format(Long.valueOf(playersLiStrong.child(0).ownText().trim().replace(",", "")));
			}
			res.playersDiff = playersLi.getElementsByClass("left").get(0).ownText().trim();
			res.playersDiff = res.playersDiff.replace("&plusmn;", "±");

			return res;
		}
	}

	private String colorizeDiff(final String diff, final boolean reverse) {
		if (!reverse && diff.contains("+") || reverse && diff.contains("-")) {
			return "" + Codes.BOLD + Codes.GREEN + diff + Codes.RESET;
		} else if (!reverse && diff.contains("-") || reverse && diff.contains("+")) {
			return "" + Codes.BOLD + Codes.RED + diff + Codes.RESET;
		} else {
			return "" + Codes.BOLD + Codes.GRAY + diff + Codes.RESET;
		}
	}
}