package fr.ribesg.blob;
import fr.ribesg.alix.api.Client;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.bot.command.CommandManager;
import fr.ribesg.alix.network.ssl.SSLType;
import fr.ribesg.blob.command.bot.QuitCommand;
import fr.ribesg.blob.command.minecraft.bukkitdev.AuthorCommand;
import fr.ribesg.blob.command.minecraft.bukkitdev.PluginCommand;
import fr.ribesg.blob.command.minecraft.mcstats.GlobalMCStatsCommand;
import fr.ribesg.blob.command.minecraft.mcstats.MCStatsCommand;

import java.util.HashSet;
import java.util.Set;

public class BlobClient extends Client {

	public BlobClient(final String name) {
		super(name);
	}

	@Override
	protected void load() {
		// EsperNet
		final Server esperNet = new Server(this, "irc.esper.net", 6697, SSLType.TRUSTING);
		esperNet.addChannel("#alix");
		esperNet.addChannel("#blob");
		esperNet.addChannel("#ribesg");
		this.getServers().add(esperNet);

		// QuakeNet
		final Server quakeNet = new Server(this, "irc.quakenet.org", 6667);
		quakeNet.addChannel("#mtxserv");
		this.getServers().add(quakeNet);

		final Set<String> admins = new HashSet<>();
		admins.add("Ribesg");

		this.createCommandManager("!", admins);

		final CommandManager cmdManager = getCommandManager();

		// Minecraft
		cmdManager.registerCommand(new MCStatsCommand());
		cmdManager.registerCommand(new GlobalMCStatsCommand());
		cmdManager.registerCommand(new PluginCommand());
		cmdManager.registerCommand(new AuthorCommand());

		// Bot
		cmdManager.registerCommand(new QuitCommand(this));
	}
}