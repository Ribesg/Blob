package fr.ribesg.blob.command.bot;
import fr.ribesg.alix.Tools;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.command.CommandManager;
import fr.ribesg.alix.api.enums.Codes;
import fr.ribesg.alix.api.message.IrcPacket;

public class JoinCommand extends Command {

	public JoinCommand(final CommandManager manager) {
		super(manager, "join", true, null);
	}

	@Override
	public void exec(final Server server, final Channel channel, final Source user, final String[] args) {
		final Receiver receiver = channel == null ? user : channel;

		if (args.length < 1) {
			receiver.sendMessage(Codes.RED + "Ask " + server.getClient().getName() + " to join a channel with " + this + " <channel[,...]>");
		} else {
			boolean silent = false;
			for (final String arg : args) {
				switch (arg.toLowerCase()) {
					case "-s":
					case "--silent":
						silent = true;
						break;
					default:
						break;
				}
			}
			for (final String arg : args) {
				Channel otherChannel = server.getChannel(arg);
				if (otherChannel == null) {
					server.send(new IrcPacket(null, fr.ribesg.alix.api.enums.Command.JOIN.name(), null, arg));
					if (!silent) {
						Tools.pause(1_000);
						otherChannel = server.getChannel(arg);
						if (otherChannel != null) {
							otherChannel.sendMessage(user.getName() + " told me I should join this channel, hi!");
						}
					}
				} else {
					receiver.sendMessage(Codes.RED + "I'm already in " + otherChannel.getName() + "!");
				}
			}
		}
	}
}
