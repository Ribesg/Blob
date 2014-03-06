package fr.ribesg.blob.command.minecraft.bukkitdev;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.util.IrcUtil;
import fr.ribesg.alix.api.bot.util.WebUtil;
import fr.ribesg.alix.api.enums.Codes;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class AuthorCommand extends Command {

	private static final Logger LOG = Logger.getLogger(AuthorCommand.class.getName());
	private SimpleDateFormat dateFormat;

	public AuthorCommand() {
		super("author");
		this.dateFormat = new SimpleDateFormat("YYYY-MM-dd");
	}

	@Override
	public void exec(final Server server, final Channel channel, final Source user, final String[] args) {
		final Receiver receiver = channel == null ? user : channel;

		if (args.length != 1 && args.length != 2) {
			nope(receiver);
			return;
		}

		try {
			int amount = 3;
			if (args.length == 2) {
				try {
					amount = Integer.parseInt(args[1]);
				} catch (final NumberFormatException e) {
					nope(receiver);
					return;
				}

				if (amount < 0) {
					nope(receiver);
					return;
				}
			}
			final UserInfo userInfo = getRealUserName(args[0]);

			if (userInfo == null) {
				receiver.sendMessage(Codes.RED + "Unable to find that user!");
				return;
			}

			final SortedSet<Plugin> plugins = new TreeSet<>();
			boolean hasNextPage;
			final String devBukkitLink = "http://dev.bukkit.org";
			final String profilePageLink = devBukkitLink + "/profiles/" + userInfo.name;
			String nextPageLink = profilePageLink + "/bukkit-plugins/";
			do {
				// Get the page
				final Document document = WebUtil.getPage(nextPageLink);

				// Check if there is at least one plugin
				if (document.getElementsByClass("listing-none-found").size() > 0) {
					receiver.sendMessage(IrcUtil.preventPing(userInfo.name) + " - " + userInfo.state + " (" + WebUtil.shortenUrl(profilePageLink) + ") | Reputation: " + userInfo.reputation + " | No Project");
					receiver.sendMessage("Join date: " + userInfo.joined + " | Status: " + userInfo.lastLogin);
					if (userInfo.state.contains("Banned")) {
						receiver.sendMessage("Ban reason: " + userInfo.banReason);
					}
					return;
				}

				// Check if we will have to look at another page
				final Elements pages = document.getElementsByClass("listing-pagination-pages").get(0).children();
				if (pages.size() > 1) {
					final Element lastLink = pages.get(pages.size() - 1);
					if (lastLink.children().size() > 0 && lastLink.child(0).ownText().trim().startsWith("Next")) {
						hasNextPage = true;
						nextPageLink = devBukkitLink + lastLink.child(0).attr("href");
					} else {
						hasNextPage = false;
						nextPageLink = null;
					}
				} else {
					hasNextPage = false;
					nextPageLink = null;
				}

				// List stuff on this page
				final Elements pluginsTd = document.getElementsByClass("col-project");
				for (final Element e : pluginsTd) {
					if ("td".equalsIgnoreCase(e.tagName())) {
						final Plugin plugin = new Plugin();
						final Element link = e.getElementsByTag("h2").get(0).getElementsByTag("a").get(0);
						plugin.name = link.ownText().trim();
						final String pluginUrl = devBukkitLink + link.attr("href");

						final Document pluginDocument = WebUtil.getPage(pluginUrl);
						plugin.downloadCount = Integer.parseInt(pluginDocument.getElementsByAttribute("data-value").first().attr("data-value"));
						final String date = e.nextElementSibling().child(0).attr("data-epoch");
						try {
							plugin.lastUpdate = Long.parseLong(date);
						} catch (final NumberFormatException ex) {
							receiver.sendMessage(Codes.RED + "An error occured: Cannot parse \"" + date + "\" as a long.");
							return;
						}
						plugins.add(plugin);
					}
				}
			} while (hasNextPage);

			final Iterator<Plugin> it = plugins.iterator();

			receiver.sendMessage(IrcUtil.preventPing(userInfo.name) + " - " + userInfo.state + " (" + WebUtil.shortenUrl(profilePageLink) + ") | Reputation: " + userInfo.reputation + " | Projects: " + plugins.size());
			receiver.sendMessage("Join date: " + userInfo.joined + " | Status: " + userInfo.lastLogin);
			if (userInfo.state.contains("Banned")) {
				receiver.sendMessage("Ban reason: " + userInfo.banReason);
			}
			if (plugins.isEmpty()) { // Should not happen
				receiver.sendMessage(Codes.RED + "Unknown user or user without plugins");
			} else {
				if (amount == 1) {
					final Plugin plugin = it.next();
					receiver.sendMessage("Last updated plugin: " + IrcUtil.preventPing(plugin.name) + " (" + formatDate(plugin.lastUpdate) + ") | " + plugin.downloadCount + " DLs");
				} else if (amount > 1) {
					receiver.sendMessage((amount < plugins.size() ? amount : plugins.size()) + " last updated plugins:");
					int i = 0;
					while (it.hasNext() && i < amount) {
						final Plugin plugin = it.next();
						receiver.sendMessage("- " + IrcUtil.preventPing(plugin.name) + " (" + formatDate(plugin.lastUpdate) + ") | " + plugin.downloadCount + " DLs");
						i++;
					}
				}
				int total = 0;
				for (final Plugin plugin : plugins) {
					total += plugin.downloadCount;
				}
				receiver.sendMessage("Total downloads: " + Codes.BOLD + total);
			}
		} catch (final FileNotFoundException | MalformedURLException e) {
			receiver.sendMessage(Codes.RED + "Unable to find that user!");
		} catch (final IOException e) {
			receiver.sendMessage(Codes.RED + "Failed: " + e.getMessage());
		}
	}

	private final class Plugin implements Comparable<Plugin> {

		public String name;
		public long   lastUpdate;
		public int    downloadCount;

		@Override
		public int compareTo(final Plugin o) {
			return lastUpdate > o.lastUpdate ? -1 : 1;
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

	private UserInfo getRealUserName(String bukkitDevUser) throws IOException {
		final Document doc = WebUtil.getPage("http://dev.bukkit.org/profiles/" + bukkitDevUser);
		final UserInfo info = new UserInfo();

		// Username
		info.name = doc.getElementsByTag("h1").get(1).ownText().trim();

		// User state
		if (doc.getElementsByClass("avatar-author").size() > 0) {
			info.state = Codes.BLUE + "Author";
		} else if (doc.getElementsByClass("avatar-normal").size() > 0) {
			info.state = Codes.GRAY + "Normal";
		} else if (doc.getElementsByClass("avatar-moderator").size() > 0) {
			info.state = Codes.GREEN + "Staff";
		} else if (doc.getElementsByClass("avatar-banned").size() > 0) {
			info.state = Codes.RED + "Banned";
		} else {
			info.state = Codes.PURPLE + "Unknown";
		}
		info.state += Codes.RESET;

		final Elements elems = doc.getElementsByClass("content-box-inner");
		final Element contentDiv = elems.get(elems.size() - 1);

		// User joined date
		String date = contentDiv.getElementsByClass("standard-date").get(0).attr("data-epoch");
		long dateLong = Long.parseLong(date);
		info.joined = formatDate(dateLong);

		// Last login
		if (contentDiv.getElementsByClass("user-online").size() > 0) {
			info.lastLogin = Codes.LIGHT_GREEN + "Online" + Codes.RESET;
		} else {
			final Elements elements = contentDiv.getElementsByClass("user-offline");
			if (elements.size() == 0) {
				// Unknown user
				return null;
			}
			date = elements.get(0).getElementsByClass("standard-date").get(0).attr("data-epoch");
			dateLong = Long.parseLong(date);
			info.lastLogin = Codes.GRAY + "Offline, last login on " + formatDate(dateLong) + Codes.RESET;
		}

		// Reputation
		info.reputation = contentDiv.getElementsByAttribute("data-value").get(0).ownText().trim();

		// Ban reason
		if (info.state.contains("Banned")) {
			info.banReason = doc.getElementsByClass("warning-message-inner").get(0).child(0).ownText().trim().substring(27);
		}
		return info;
	}

	private void nope(final Receiver receiver) {
		receiver.sendMessage(Codes.RED + "Look up an author with !author <name> [amount]");
	}

	private String formatDate(final long date) {
		return this.dateFormat.format(new Date(date * 1000));
	}

}
