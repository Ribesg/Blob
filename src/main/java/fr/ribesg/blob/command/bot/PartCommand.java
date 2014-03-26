package fr.ribesg.blob.command.bot;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.command.CommandManager;
import fr.ribesg.alix.api.callback.Callback;
import fr.ribesg.alix.api.message.IrcPacket;
import fr.ribesg.alix.api.message.PartIrcPacket;

public class PartCommand extends Command {

	public PartCommand(final CommandManager manager) {
		super(manager, "part", new String[] {" [channel[,...]] - Ask me to leave a/some channel(s)"}, true, null);
	}

	@Override
	public void exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
		if (args.length == 0) {
			channel.sendMessage("Bye!");
			server.send(new PartIrcPacket(channel.getName()));
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
				final Channel otherChannel = server.getChannel(arg);
				if (otherChannel != null) {
					if (!silent) {
						otherChannel.sendMessage(user.getName() + " told me I should leave this channel, bye!");
					}
					server.send(new PartIrcPacket(otherChannel.getName()), new Callback(5_000, "PART") {

						@Override
						public boolean onIrcPacket(final IrcPacket packet) {
							final String channelName = packet.getParameters().length > 0 ? packet.getParameters()[0] : packet.getTrail();
							if (channelName.equals(this.originalIrcPacket.getParameters()[0])) {
								this.server.removeChannel(channelName);
								return true;
							} else {
								return false;
							}
						}
					});
				}
			}
		}
	}
}
