/*
 * Copyright (c) 2012-2014 Ribesg - www.ribesg.fr
 * This file is under GPLv3 -> http://www.gnu.org/licenses/gpl-3.0.txt
 * Please contact me at ribesg[at]yahoo.fr if you improve this file!
 */

package fr.ribesg.blob;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Client;
import fr.ribesg.alix.api.Log;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.bot.command.CommandManager;
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
import fr.ribesg.blob.command.util.UrbanCommand;

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
      esperNet.addChannel("#alix");
      esperNet.addChannel("#bendemPlugins");
      esperNet.addChannel("#drtshock");
      esperNet.addChannel("#fmdev");
      esperNet.addChannel("#ncube");
      esperNet.addChannel("#nukkit");
      esperNet.addChannel("#ribesg");
      esperNet.addChannel("#statik");
      esperNet.addChannel("#ten.java");
      this.getServers().add(esperNet);

      // QuakeNet
      final Server quakenet = new Server(this, getName(), "euroserv.fr.quakenet.org", 6667, SSLType.NONE);
      quakenet.addChannel("#mtxserv");
      this.getServers().add(quakenet);

      final Set<String> admins = new HashSet<>();
      admins.add("Ribesg");

      this.createCommandManager("+", admins);

      final CommandManager manager = getCommandManager();

      manager.setUnknownCommandMessage(null);

      // Minecraft
      manager.registerCommand(new MCStatsCommand());
      manager.registerCommand(new GlobalMCStatsCommand());
      manager.registerCommand(new PluginCommand());
      manager.registerCommand(new AuthorCommand());
      manager.registerCommand(new MCNameCommand());
      manager.registerCommand(new MCStatusCommand());

      // Bot
      manager.registerCommand(new JoinCommand());
      manager.registerCommand(new PartCommand());
      manager.registerCommand(new QuitCommand(this));

      // Util
      manager.registerCommand(new ShortenCommand());
      manager.registerCommand(new GoogleCommand());
      manager.registerCommand(new UrbanCommand());
   }

   @Override
   public void onClientJoinChannel(final Channel channel) {
      // Anti-shitty Willie
      Log.debug("DEBUG: Updating users...");
      channel.updateUsers(() -> {
         Log.debug("DEBUG: Users updated!");
         if (channel.getUserNicknames().contains("Willie")) {
            channel.sendMessage("Plop");
         }
      });
   }
}
