package fr.ribesg.blob.command.util;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.command.CommandManager;
import fr.ribesg.alix.api.bot.util.WebUtil;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GoogleCommand extends Command {

	private static final Logger LOGGER = Logger.getLogger(GoogleCommand.class.getName());

	private static final String GOOGLE_URL = "http://www.google.%s/search?q=";

	public GoogleCommand(final CommandManager manager) {
		super(manager, "google", new String[] {"<query> - Link to a Google search of something"}, "g");
	}

	@Override
	public void exec(final Server server, final Channel channel, final Source user, final String[] args) {
		final Receiver receiver = channel == null ? user : channel;

		if (args.length == 0) {
			sendUsage(receiver);
			return;
		}

		final String site;
		final StringBuilder request = new StringBuilder(args[0]);
		if (args[args.length - 1].startsWith("--")) {
			if (args.length == 1) {
				sendUsage(receiver);
				return;
			}

			site = args[args.length - 1].substring(2);

			for (int i = 1; i < args.length - 1; i++) {
				request.append(' ').append(args[i]);
			}
		} else {
			site = "com";

			for (int i = 1; i < args.length; i++) {
				request.append(args[i]);
			}
		}

		try {
			final String url = URLEncoder.encode(String.format(GOOGLE_URL, site) + request, "UTF-8");
			try {
				receiver.sendMessage("Search: " + WebUtil.shortenUrl(url));
			} catch (final IOException e) {
				receiver.sendMessage("Search: " + url);
			}
		} catch (final UnsupportedEncodingException e) {
			LOGGER.error(e);
		}
	}
}
