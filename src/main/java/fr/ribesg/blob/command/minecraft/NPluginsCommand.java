package fr.ribesg.blob.command.minecraft;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;

public class NPluginsCommand extends Command {

	public NPluginsCommand() {
		super("nplugins");
	}

	@Override
	public void exec(final Server server, final Channel channel, final Source user, final String[] strings) {
		final Receiver receiver = channel == null ? user : channel;


	}
}
