package fr.ribesg.blob.command.bot;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.CommandManager;
import fr.ribesg.alix.api.callback.Callback;
import fr.ribesg.alix.api.enums.Codes;
import fr.ribesg.alix.api.enums.Command;
import fr.ribesg.alix.api.message.IrcPacket;
import fr.ribesg.alix.api.message.JoinIrcPacket;

public class JoinCommand extends fr.ribesg.alix.api.bot.command.Command {

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
			final boolean finalSilent = silent;
			for (final String arg : args) {
				Channel otherChannel = server.getChannel(arg);
				if (otherChannel == null) {
					server.send(new JoinIrcPacket(arg), new Callback(5_000, Command.JOIN.name()) {

						@Override
						public boolean onIrcPacket(final IrcPacket packet) {
							final String channelName = packet.getParameters().length > 0 ? packet.getParameters()[0] : packet.getTrail();
							if (channelName.equals(this.originalIrcPacket.getParameters()[0])) {
								this.server.addChannel(channelName);
								final Channel channel = this.server.getChannel(channelName);
								if (!finalSilent) {
									channel.sendMessage(user.getName() + " told me I should join this channel, hi!");
								}
								return true;
							} else {
								return false;
							}
						}
					});
				} else {
					receiver.sendMessage(Codes.RED + "I'm already in " + otherChannel.getName() + "!");
				}
			}
		}
	}
}
