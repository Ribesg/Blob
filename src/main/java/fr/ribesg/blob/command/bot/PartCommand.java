package fr.ribesg.blob.command.bot;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.command.CommandManager;
import fr.ribesg.alix.api.message.IrcPacket;

// TODO Rework this once Alix has appropriate stuff
public class PartCommand extends Command {

	public PartCommand(final CommandManager manager) {
		super(manager, "part", true, null);
	}

	@Override
	public void exec(final Server server, final Channel channel, final Source user, final String[] args) {
		if (args.length == 0) {
			channel.sendMessage("Bye!");
			server.send(new IrcPacket(null, fr.ribesg.alix.api.enums.Command.PART.name(), null, channel.getName()));
		} else {
			for (final String arg : args) {
				final Channel otherChannel = server.getChannel(arg);
				if (otherChannel != null) {
					otherChannel.sendMessage(user.getName() + " told me I should leave this channel, bye!");
					server.send(new IrcPacket(null, fr.ribesg.alix.api.enums.Command.PART.name(), null, otherChannel.getName()));
				}
			}
		}
	}
}
