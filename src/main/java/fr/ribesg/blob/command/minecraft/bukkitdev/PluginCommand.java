package fr.ribesg.blob.command.minecraft.bukkitdev;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Client;
import fr.ribesg.alix.api.Log;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.command.CommandManager;
import fr.ribesg.alix.api.bot.util.IrcUtil;
import fr.ribesg.alix.api.bot.util.WebUtil;
import fr.ribesg.alix.api.enums.Codes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

public class PluginCommand extends Command {

	private static final String BUKKITDEV_URL         = "http://dev.bukkit.org";
	private static final String BUKKITDEV_PLUGINS_URL = BUKKITDEV_URL + "/bukkit-plugins/";
	private static final String CURSE_URL             = "http://www.curse.com/bukkit-plugins/minecraft/";

	private SimpleDateFormat dateFormat;

	public PluginCommand(final CommandManager manager) {
		super(manager, "plugin", new String[] {
				"Look up a BukkitDev Plugin",
				"Usage: ## <name>"
		});
		this.dateFormat = new SimpleDateFormat("YYYY-MM-dd");
	}

	@Override
	public void exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
		final Receiver receiver = channel == null ? user : channel;

		if (args.length != 1) {
			sendUsage(receiver);
			return;
		}

		final String pluginUrl = CURSE_URL + args[0].toLowerCase();

		// Get BukkitDev Files page for later use
		final Future<Document> futureDBODoc = Client.getThreadPool().submit(() -> WebUtil.getPage(BUKKITDEV_PLUGINS_URL + args[0] + "/files/"));

		// Get Curse page now
		final Document doc;
		try {
			doc = WebUtil.getPage(pluginUrl);
		} catch (final IOException e) {
			receiver.sendMessage("Failed to get information about '" + args[0] + "'");
			Log.error("Failed to get page " + pluginUrl, e);
			return;
		}

		// Plugin name
		final String pluginName = IrcUtil.preventPing(doc.select("#project-overview span.right").get(0).ownText());

		// Authors list
		final Elements authorsLinks = doc.select("ul.authors a");
		final List<String> authorsList = new ArrayList<>();
		for (final Element e : authorsLinks) {
			final String author = e.ownText();
			if (!authorsList.contains(author)) {
				authorsList.add(author);
			}
		}
		final StringBuilder builder = new StringBuilder(Codes.BOLD + Codes.LIGHT_GREEN + IrcUtil.preventPing(authorsList.get(0)));
		if (authorsList.size() < 6) {
			for (int i = 1; i < authorsList.size(); i++) {
				builder.append(Codes.RESET).append(", ").append(Codes.BOLD).append(Codes.LIGHT_GREEN).append(IrcUtil.preventPing(authorsList.get(i)));
			}
		} else {
			for (int i = 1; i < 4; i++) {
				builder.append(Codes.RESET).append(", ").append(Codes.BOLD).append(Codes.LIGHT_GREEN).append(IrcUtil.preventPing(authorsList.get(i)));
			}
			builder.append(Codes.RESET).append(" and ").append(Codes.BOLD).append(Codes.LIGHT_GREEN).append(authorsList.size() - 4).append(Codes.RESET).append(" others");
		}
		final String authors = builder.toString();

		// Total downloads
		final String totalDownloads = getNbDownloads(doc.select("li.downloads").get(0).ownText());

		// Monthly downloads
		final String monthlyDownloads = getNbDownloads(doc.select("li.average-downloads").get(0).ownText());

		// Last Update date & Creation date
		final Elements updated = doc.select("li.updated");
		final String lastUpdated = getDate(updated.get(0).getElementsByTag("abbr").get(0).attr("data-epoch"));
		final String created = getDate(updated.get(1).getElementsByTag("abbr").get(0).attr("data-epoch"));

		// Get latest download
		final Element latestFileTr = doc.select("#tab-other-downloads div.listing-body > table > tbody > tr.even").get(0);
		final String latestFileName = latestFileTr.getElementsByTag("a").get(0).ownText();
		final String latestFileType = latestFileTr.child(1).ownText();
		final String latestFileCBVersion = latestFileTr.child(2).ownText();
		final String latestFileDate = getDate(latestFileTr.child(4).select("abbr.standard-date").get(0).attr("data-epoch"));

		// Get latest download URL from BukkitDev Files page
		String latestFileUrl;
		try {
			final Document bukkitdevDoc = futureDBODoc.get();
			latestFileUrl = BUKKITDEV_URL + bukkitdevDoc.select("table.listing > tbody tr.odd td.col-file a").get(0).attr("href");
		} catch (final Exception e) {
			latestFileUrl = null;
		}

		// Shorten the latest File url
		try {
			latestFileUrl = WebUtil.shortenUrl(latestFileUrl);
		} catch (final IOException ignored) {}

		// Shorten the BukkitDev Page url
		String url;
		try {
			url = WebUtil.shortenUrl(BUKKITDEV_PLUGINS_URL + args[0]);
		} catch (final IOException e) {
			url = BUKKITDEV_PLUGINS_URL + args[0];
		}

		// Send
		final String[] messages = {
				Codes.BOLD + pluginName + Codes.RESET + " - " + Codes.LIGHT_GREEN + url + (latestFileUrl == null ? "" : (Codes.RESET + " - Latest: " + Codes.LIGHT_BLUE + latestFileUrl)),
				"- Made by " + authors,
				"- " + Codes.BOLD + Codes.LIGHT_GREEN + monthlyDownloads + Codes.RESET + " monthly downloads, " +
				Codes.BOLD + Codes.LIGHT_GREEN + totalDownloads + Codes.RESET + " total downloads",
				"- Birth: " + Codes.BOLD + Codes.LIGHT_GREEN + created + Codes.RESET + " - Last Update: " + Codes.BOLD + Codes.LIGHT_GREEN + lastUpdated,
				"- Latest file: " + Codes.BOLD + Codes.LIGHT_GREEN + latestFileName + Codes.RESET + ", a " + Codes.BOLD + Codes.LIGHT_GREEN +
				latestFileType + Codes.RESET + " for " + Codes.BOLD + Codes.LIGHT_GREEN + latestFileCBVersion +
				Codes.RESET + " (" + Codes.BOLD + Codes.LIGHT_GREEN + latestFileDate + Codes.RESET + ')'
		};
		receiver.sendMessage(messages);
	}

	private String getNbDownloads(final String downloadsString) {
		return downloadsString.substring(0, downloadsString.indexOf(' '));
	}

	private String getDate(final String dataEpoch) {
		final long dateLong = Long.parseLong(dataEpoch);
		final Date date = new Date(1_000 * dateLong);
		return dateFormat.format(date);
	}

}
