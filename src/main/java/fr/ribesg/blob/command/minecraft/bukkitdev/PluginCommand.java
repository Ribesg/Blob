package fr.ribesg.blob.command.minecraft.bukkitdev;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.util.IrcUtil;
import fr.ribesg.alix.api.bot.util.WebUtil;
import fr.ribesg.alix.api.enums.Codes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PluginCommand extends Command {

	private SimpleDateFormat dateFormat;

	public PluginCommand() {
		super("plugin");
		this.dateFormat = new SimpleDateFormat("YYYY-MM-dd");
	}

	@Override
	public void exec(final Server server, final Channel channel, final Source user, final String[] args) {
		final Receiver receiver = channel == null ? user : channel;

		if (args.length != 1) {
			receiver.sendMessage(Codes.RED + "Look up a plugin with !plugin <name>");
			return;
		}

		try {
			final String urlSring = "http://dev.bukkit.org/bukkit-plugins/" + args[0] + "/";

			final Document document = WebUtil.getPage(urlSring);

			final String name = document.getElementsByTag("h1").get(1).ownText().trim();
			final StringBuilder authors = new StringBuilder();
			final int downloads = Integer.parseInt(document.getElementsByAttribute("data-value").first().attr("data-value"));

			final Elements containers = document.getElementsByClass("user-container");

			if (!containers.isEmpty()) {
				authors.append(IrcUtil.preventPing(containers.get(0).text().trim()));
			}

			for (int i = 1; i < containers.size(); ++i) {
				authors.append(", ");
				String author = containers.get(i).text().trim();
				authors.append(IrcUtil.preventPing(author));
			}

			String files;
			final Elements filesList = document.getElementsByClass("file-type");
			if (filesList.size() > 0) {
				final Element latest = filesList.get(0);
				final String version = latest.nextElementSibling().ownText();
				final String bukkitVersion = latest.parent().ownText().split("for")[1].trim();
				final long fileDate = Long.parseLong(latest.nextElementSibling().nextElementSibling().attr("data-epoch"));
				final String date = this.dateFormat.format(new Date(fileDate * 1000));
				files = "Latest File: " + version + " for " + bukkitVersion + " (" + date + ")";
			} else {
				files = "No files (yet!)";
			}

			receiver.sendMessage(name + " (" + WebUtil.shortenUrl(urlSring) + ")");
			receiver.sendMessage("Authors: " + authors.toString());
			receiver.sendMessage("Downloads: " + downloads);
			receiver.sendMessage(files);
		} catch (final FileNotFoundException e) {
			receiver.sendMessage(Codes.RED + "Project not found");
		} catch (final MalformedURLException e) {
			receiver.sendMessage(Codes.RED + "Unable to find that plugin!");
		} catch (final IOException e) {
			receiver.sendMessage(Codes.RED + "Failed: " + e.getMessage());
		}
	}

}
