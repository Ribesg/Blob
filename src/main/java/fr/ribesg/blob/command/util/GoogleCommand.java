package fr.ribesg.blob.command.util;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.command.CommandManager;
import fr.ribesg.alix.api.bot.util.WebUtil;
import fr.ribesg.alix.api.enums.Codes;
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
	public void exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
		final Receiver receiver = channel == null ? user : channel;

		if (args.length == 0) {
			sendUsage(receiver);
			return;
		}

		final String site = primaryArgument == null ? "com" : primaryArgument;
		final StringBuilder request = new StringBuilder(args[0]);
		for (int i = 1; i < args.length; i++) {
			request.append(' ').append(args[i]);
		}

		try {
			WebUtil.getString("http://www.google." + site);
		} catch (IOException e) {
			receiver.sendMessage(Codes.RED + user.getName() + ", " + Codes.LIGHT_BLUE + "http://www.google." + site + "/" + Codes.RED + " doesn't seem to be a thing");
			return;
		}

		try {
			final String url = URLEncoder.encode(String.format(GOOGLE_URL, site) + request, "UTF-8");
			final String message = Codes.LIGHT_GRAY + '\'' + request + "' on google." + Codes.BOLD + site + Codes.RESET + Codes.LIGHT_GRAY + ": " + Codes.LIGHT_GREEN;
			String shortUrl = url;
			try {
				shortUrl = WebUtil.shortenUrl(url);
			} catch (final IOException ignored) {}
			receiver.sendMessage(message + shortUrl);
		} catch (final UnsupportedEncodingException e) {
			LOGGER.error(e);
		}
	}
}
