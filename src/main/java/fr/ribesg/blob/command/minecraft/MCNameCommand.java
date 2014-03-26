package fr.ribesg.blob.command.minecraft;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.command.CommandManager;
import fr.ribesg.alix.api.bot.util.IrcUtil;
import fr.ribesg.alix.api.bot.util.WebUtil;
import fr.ribesg.alix.api.enums.Codes;
import org.apache.log4j.Logger;

import java.util.regex.Pattern;

public class MCNameCommand extends Command {

	private static final Logger LOGGER = Logger.getLogger(MCNameCommand.class.getName());

	private static final Pattern MC_USER_REGEX = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");

	public MCNameCommand(final CommandManager manager) {
		super(manager, "minecraftname", new String[] {" <name> - Look up a Minecraft username"}, "mcname", "mcn");
	}

	@Override
	public void exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
		final Receiver receiver = channel == null ? user : channel;

		if (args.length != 1) {
			sendUsage(receiver);
			return;
		}

		final String userName = args[0];
		final String escapedUserName = IrcUtil.preventPing(userName);

		if (!MC_USER_REGEX.matcher(userName).matches()) {
			receiver.sendMessage(Codes.RED + '"' + userName + "\" is not a valid Minecraft username");
			return;
		}

		final boolean taken;
		try {
			final String result = WebUtil.getString("https://account.minecraft.net/buy/frame/checkName/" + userName).trim();
			switch (result) {
				case "TAKEN":
					taken = true;
					break;
				case "OK":
					taken = false;
					break;
				default:
					throw new Exception("Unknown result: " + result);
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to get availability", e);
			return;
		}

		if (taken) {
			final boolean hasPaid;
			try {
				final String result = WebUtil.getString("https://minecraft.net/haspaid.jsp?user=" + userName).trim();
				switch (result) {
					case "true":
						hasPaid = true;
						break;
					case "false":
						hasPaid = false;
						break;
					default:
						throw new Exception("Unknown result: " + result);
				}
			} catch (final Exception e) {
				LOGGER.error("Failed to get hasPaid state", e);
				return;
			}
			if (hasPaid) {
				receiver.sendMessage("The username " + Codes.BOLD + escapedUserName + Codes.RESET + " is " +
				                     Codes.BOLD + Codes.RED + "taken" + Codes.RESET + " and " + Codes.BOLD + Codes.RED + "premium");
			} else {
				receiver.sendMessage("The username " + Codes.BOLD + escapedUserName + Codes.RESET + " is " +
				                     Codes.BOLD + Codes.RED + "taken" + Codes.RESET + " but " + Codes.BOLD + Codes.YELLOW + "not premium");
			}
		} else {
			receiver.sendMessage("The username " + Codes.BOLD + escapedUserName + Codes.RESET + " is " +
			                     Codes.BOLD + Codes.LIGHT_GREEN + "available");
		}
	}
}
