package fr.ribesg.blob;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Client;
import fr.ribesg.alix.api.Log;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.bot.command.CommandManager;
import fr.ribesg.alix.api.enums.Codes;
import fr.ribesg.alix.api.network.ssl.SSLType;
import fr.ribesg.blob.command.bot.JoinCommand;
import fr.ribesg.blob.command.bot.PartCommand;
import fr.ribesg.blob.command.bot.QuitCommand;
import fr.ribesg.blob.command.minecraft.MCNameCommand;
import fr.ribesg.blob.command.minecraft.MCStatusCommand;
import fr.ribesg.blob.command.minecraft.bukkitdev.AuthorCommand;
import fr.ribesg.blob.command.minecraft.bukkitdev.PluginCommand;
import fr.ribesg.blob.command.minecraft.mcstats.GlobalMCStatsCommand;
import fr.ribesg.blob.command.minecraft.mcstats.MCStatsCommand;
import fr.ribesg.blob.command.util.GoogleCommand;
import fr.ribesg.blob.command.util.ShortenCommand;

import java.util.HashSet;
import java.util.Set;

public class BlobClient extends Client {

	public BlobClient() {
		super("Blob");
	}

	@Override
	protected void load() {
		// EsperNet
		final Server esperNet = new Server(this, getName(), "irc.esper.net", 6697, SSLType.TRUSTING);
//		esperNet.addChannel("#alix");
//		esperNet.addChannel("#bendemPlugins");
		esperNet.addChannel("#blob");
//		esperNet.addChannel("#drtshock");
//		esperNet.addChannel("#ncube");
//		esperNet.addChannel("#ribesg");
		this.getServers().add(esperNet);

//		final Server freenode = new Server(this, getName(), "chat.freenode.net", 6697, SSLType.TRUSTING);
//		freenode.addChannel("#brainjar");
//		this.getServers().add(freenode);

		final Set<String> admins = new HashSet<>();
		admins.add("Ribesg");

		this.createCommandManager("+", admins);

		final CommandManager manager = getCommandManager();

		manager.setUnknownCommandMessage(null);

		// Minecraft
		manager.registerCommand(new MCStatsCommand(manager));
		manager.registerCommand(new GlobalMCStatsCommand(manager));
		manager.registerCommand(new PluginCommand(manager));
		manager.registerCommand(new AuthorCommand(manager));
		manager.registerCommand(new MCNameCommand(manager));
		manager.registerCommand(new MCStatusCommand(manager));

		// Bot
		manager.registerCommand(new JoinCommand(manager));
		manager.registerCommand(new PartCommand(manager));
		manager.registerCommand(new QuitCommand(manager, this));

		// Util
		manager.registerCommand(new ShortenCommand(manager));
		manager.registerCommand(new GoogleCommand(manager));
	}

	@Override
	public void onClientJoinChannel(final Channel channel) {
		// Anti-shitty Willie
		Log.debug("DEBUG: Updating users...");
		channel.updateUsers(true);
		Log.debug("DEBUG: Users updated!");
		if (channel.getUserNames().contains("Willie")) {
			channel.sendMessage("Hey " + Codes.RED + "Willie" + Codes.RESET + ", don't kick me, stupid bot, thanks!");
		}
	}
}
