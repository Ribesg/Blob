package fr.ribesg.blob.command.bot;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.blob.BlobClient;

public class QuitCommand extends Command {

	private final BlobClient client;

	public QuitCommand(final BlobClient client) {
		super("quit", true, null);
		this.client = client;
	}

	@Override
	public void exec(final Server server, final Channel channel, final Source user, final String[] strings) {
		final Receiver receiver = channel == null ? user : channel;

		receiver.sendMessage("Bye!");
		for (final Server s : this.client.getServers()) {
			if (s.hasJoined()) {
				s.disconnect("Bye!");
			}
		}
	}
}
