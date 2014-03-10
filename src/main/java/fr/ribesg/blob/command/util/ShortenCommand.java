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

public class ShortenCommand extends Command {

	private static final Logger LOGGER = Logger.getLogger(ShortenCommand.class.getName());

	public ShortenCommand(final CommandManager manager) {
		super(manager, "shorten", "s");
	}

	@Override
	public void exec(final Server server, final Channel channel, final Source user, final String[] args) {
		final Receiver receiver = channel == null ? user : channel;

		if (args.length != 1) {
			receiver.sendMessage(Codes.RED + "Shorten an URL with " + this + " <url>");
			return;
		}

		final String url = args[0];
		try {
			final String shortUrl = WebUtil.shortenUrl(url);
			receiver.sendMessage("Done: " + Codes.LIGHT_GREEN + shortUrl);
		} catch (final IOException e) {
			receiver.sendMessage(Codes.RED + "Failed to shorten URL");
			LOGGER.error("Failed to shorten URL", e);
		}
	}
}
