package fr.ribesg.blob;
import fr.ribesg.alix.api.Client;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.bot.command.CommandManager;
import fr.ribesg.blob.command.minecraft.mcstats.GlobalMCStatsCommand;
import fr.ribesg.blob.command.minecraft.mcstats.MCStatsCommand;

public class BlobClient extends Client {

	public BlobClient(final String name) {
		super(name);
	}

	@Override
	protected void load() {
		final Server server = new Server(this, "irc.esper.net", 6667);
		server.addChannel("#drtshock");
		this.getServers().add(server);

		this.createCommandManager("!", null);

		final CommandManager cmdManager = getCommandManager();
		cmdManager.registerCommand(new MCStatsCommand());
		cmdManager.registerCommand(new GlobalMCStatsCommand());
	}
}
