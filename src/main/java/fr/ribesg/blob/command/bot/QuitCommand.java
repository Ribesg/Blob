package fr.ribesg.blob.command.bot;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.command.CommandManager;
import fr.ribesg.blob.BlobClient;

public class QuitCommand extends Command {

	private final BlobClient client;

	public QuitCommand(final CommandManager manager, final BlobClient client) {
		super(manager, "quit", new String[] {
				"## - Ask me to disconnect from this server"
		}, true, null);
		this.client = client;
	}

	@Override
	public void exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] strings) {
		for (final Channel c : server.getChannels()) {
			c.sendMessage("Bye!");
		}
		server.disconnect("Bye!");
	}
}
