package fr.ribesg.blob.command.minecraft.bukkitdev;
import fr.ribesg.alix.api.Channel;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

public class AuthorCommand extends Command {

	private static final String CURSE_PLUGIN_URL      = "http://www.curse.com/bukkit-plugins/minecraft";
	private static final String BUKKITDEV_URL         = "http://dev.bukkit.org";
	private static final String BUKKITDEV_PROFILE_URL = BUKKITDEV_URL + "/profiles/";

	private SimpleDateFormat dateFormat;
	private NumberFormat     numberFormat;

	public AuthorCommand(final CommandManager manager) {
		super(manager, "author");
		this.dateFormat = new SimpleDateFormat("YYYY-MM-dd");
		this.numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
	}

	@Override
	public void exec(final Server server, final Channel channel, final Source user, final String[] args) {
		final Receiver receiver = channel == null ? user : channel;

		if (args.length != 1 && args.length != 2) {
			sendUsage(receiver);
			return;
		}

		int amount = 0;
		if (args.length == 2) {
			try {
				amount = Integer.parseInt(args[1]);
			} catch (final NumberFormatException e) {
				sendUsage(receiver);
				return;
			}

			if (amount < 0) {
				sendUsage(receiver);
				return;
			}
		}

		final List<String> messages = parsePages(args[0], amount);
		receiver.sendMessage(messages.toArray(new String[messages.size()]));
	}

	private List<String> parsePages(final String name, int amount) {
		final List<String> messages = new ArrayList<>();
		try {
			final UserInfo userInfo = getUserInfo(name);

			if (userInfo == null) {
				messages.add(Codes.RED + "Unable to find that user!");
				return messages;
			}

			final SortedSet<Plugin> plugins = new TreeSet<>();
			boolean hasNextPage;
			final String profilePageLink = BUKKITDEV_PROFILE_URL + userInfo.name;
			String nextPageLink = profilePageLink + "/bukkit-plugins/";
			do {
				// Get the page
				final Document doc = WebUtil.getPage(nextPageLink);

				// Check if there is at least one plugin
				if (doc.select(".listing-none-found").size() > 0) {
					messages.add(IrcUtil.preventPing(userInfo.name) + " - " + userInfo.state + " (" + WebUtil.shortenUrl(profilePageLink) + ") | Reputation: " + userInfo.reputation + " | No Project");
					messages.add("Join date: " + userInfo.joined + " | Status: " + userInfo.lastLogin);
					if (userInfo.state.contains("Banned")) {
						messages.add("Ban reason: " + userInfo.banReason);
					}
					return messages;
				}

				// Check if we will have to look at another page
				final Elements pages = doc.select(".listing-pagination-pages").get(0).children();
				if (pages.size() > 1) {
					final Element lastLink = pages.get(pages.size() - 1);
					if (lastLink.children().size() > 0 && lastLink.child(0).ownText().trim().startsWith("Next")) {
						hasNextPage = true;
						nextPageLink = BUKKITDEV_URL + lastLink.child(0).attr("href");
					} else {
						hasNextPage = false;
						nextPageLink = null;
					}
				} else {
					hasNextPage = false;
					nextPageLink = null;
				}

				// List stuff on this page
				final Elements pluginsTd = doc.select(".col-project");
				for (final Element e : pluginsTd) {
					if ("td".equalsIgnoreCase(e.tagName())) {
						final Plugin plugin = new Plugin();
						final Element link = e.select("h2").get(0).select("a").get(0);
						plugin.name = link.ownText().trim();
						final String pluginUrl = CURSE_PLUGIN_URL + link.attr("href").substring(15);

						try {
							final Document pluginDocument = WebUtil.getPage(pluginUrl);
							plugin.lastUpdate = getDate(pluginDocument.select("li.updated").get(0).getElementsByTag("abbr").get(0).attr("data-epoch"));
							plugin.monthlyDownloadCount = getNbDownloads(pluginDocument.select("li.average-downloads").get(0).ownText());
							plugin.totalDownloadCount = getNbDownloads(pluginDocument.select("li.downloads").get(0).ownText());
						} catch (final IOException ex) {
							plugin.lastUpdate = Codes.RED + "Not found on Curse!" + Codes.RESET;
							plugin.monthlyDownloadCount = "0";
							plugin.totalDownloadCount = "0";
						}
						plugins.add(plugin);
					}
				}
			} while (hasNextPage);

			final Iterator<Plugin> it = plugins.iterator();

			messages.add(Codes.BOLD + IrcUtil.preventPing(userInfo.name) + Codes.RESET + " - " + Codes.BOLD + userInfo.state + Codes.RESET + " (" + Codes.LIGHT_GREEN + WebUtil.shortenUrl(profilePageLink) + Codes.RESET + ")");
			messages.add("Reputation: " + Codes.BOLD + userInfo.reputation + Codes.RESET + " | Projects: " + Codes.BOLD + plugins.size() + Codes.RESET + " | Join date: " + Codes.BOLD + userInfo.joined);
			messages.add("Status: " + Codes.BOLD + userInfo.lastLogin);
			if (userInfo.state.contains("Banned")) {
				messages.add("Ban reason: " + Codes.BOLD + userInfo.banReason);
			}
			if (plugins.isEmpty()) { // Should not happen
				messages.add(Codes.RED + "Unknown user or user without plugins");
			} else {
				if (amount == 1) {
					final Plugin plugin = it.next();
					messages.add("Last updated plugin: " + Codes.BOLD + IrcUtil.preventPing(plugin.name) + Codes.RESET + " (" + plugin.lastUpdate + ")");
					messages.add(IrcUtil.preventPing(plugin.name) + " downloads: " + Codes.LIGHT_GREEN + plugin.monthlyDownloadCount + Codes.RESET + " monthly, " + Codes.LIGHT_GREEN + plugin.totalDownloadCount + Codes.RESET + " total");
				} else if (amount > 1) {
					messages.add((amount < plugins.size() ? amount : plugins.size()) + " last updated plugins:");
					int i = 0;
					while (it.hasNext() && i < amount) {
						final Plugin plugin = it.next();
						messages.add("- " + Codes.BOLD + IrcUtil.preventPing(plugin.name) + Codes.RESET + " (" + plugin.lastUpdate + ")");
						messages.add("  | Downloads: " + Codes.LIGHT_GREEN + plugin.monthlyDownloadCount + Codes.RESET + " monthly, " + Codes.LIGHT_GREEN + plugin.totalDownloadCount + Codes.RESET + " total");
						i++;
					}
				}
				long totalMonthly = 0;
				long totalTotal = 0;
				for (final Plugin plugin : plugins) {
					totalMonthly += (long) numberFormat.parse(plugin.monthlyDownloadCount);
					totalTotal += (long) numberFormat.parse(plugin.totalDownloadCount);
				}
				messages.add("Total downloads: " + Codes.BOLD + Codes.LIGHT_GREEN + numberFormat.format(totalMonthly) + Codes.RESET + " monthly, " + Codes.BOLD + Codes.LIGHT_GREEN + numberFormat.format(totalTotal) + Codes.RESET + " total");
			}
		} catch (final FileNotFoundException | MalformedURLException e) {
			messages.add(Codes.RED + "Unable to find that user or failed to get a plugin page!");
		} catch (final IOException | ParseException e) {
			messages.add(Codes.RED + "Failed: " + e.getMessage());
		}
		return messages;
	}

	private final class Plugin implements Comparable<Plugin> {

		public String name;
		public String lastUpdate;
		public String totalDownloadCount;
		public String monthlyDownloadCount;

		@Override
		public int compareTo(final Plugin o) {
			return lastUpdate.compareTo(o.lastUpdate) > 0 ? -1 : 1;
		}
	}

	private class UserInfo {

		public String name;
		public String state;
		public String joined;
		public String lastLogin;
		public String reputation;
		public String banReason;
	}

	private UserInfo getUserInfo(final String bukkitDevUser) throws IOException {
		final Document doc = WebUtil.getPage(BUKKITDEV_PROFILE_URL + bukkitDevUser);
		final UserInfo info = new UserInfo();

		// Username
		info.name = doc.select("h1").get(1).ownText().trim();

		// User state
		if (doc.select(".avatar-author").size() > 0) {
			info.state = Codes.BLUE + "Author";
		} else if (doc.select(".avatar-normal").size() > 0) {
			info.state = Codes.GRAY + "Normal";
		} else if (doc.select(".avatar-moderator").size() > 0) {
			info.state = Codes.GREEN + "Staff";
		} else if (doc.select(".avatar-banned").size() > 0) {
			info.state = Codes.RED + "Banned";
		} else {
			info.state = Codes.PURPLE + "Unknown";
		}
		info.state += Codes.RESET;

		final Elements elems = doc.select(".content-box-inner");
		final Element contentDiv = elems.get(elems.size() - 1);

		// User joined date
		String date = contentDiv.select(".standard-date").get(0).attr("data-epoch");
		info.joined = getDate(date);

		// Last login
		if (contentDiv.select(".user-online").size() > 0) {
			info.lastLogin = Codes.LIGHT_GREEN + "Online" + Codes.RESET;
		} else {
			final Elements elements = contentDiv.select(".user-offline");
			if (elements.size() == 0) {
				// Unknown user
				return null;
			}
			date = elements.get(0).select(".standard-date").get(0).attr("data-epoch");
			info.lastLogin = Codes.GRAY + "Offline, last login on " + getDate(date) + Codes.RESET;
		}

		// Reputation
		info.reputation = contentDiv.getElementsByAttribute("data-value").get(0).ownText().trim();

		// Ban reason
		if (info.state.contains("Banned")) {
			info.banReason = doc.select(".warning-message-inner").get(0).child(0).ownText().trim().substring(27);
		}
		return info;
	}

	private void sendUsage(final Receiver receiver) {
		receiver.sendMessage(Codes.RED + "Look up an author with " + this + " <name> [amount]");
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
