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
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.CommandManager;
import fr.ribesg.blob.command.bot.JoinCommand;
import fr.ribesg.blob.command.bot.NickServCommand;
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
import fr.ribesg.blob.command.util.WolframAlphaCommand;
import fr.ribesg.blob.config.BlobConfiguration;

import java.io.IOException;
import java.util.regex.Pattern;

public class BlobClient extends Client {

   private static BlobClient instance;

   public static BlobConfiguration getConfig() {
      return BlobClient.instance.config;
   }

   private BlobConfiguration config;

   public BlobClient() {
      super("Blob");
   }

   @Override
   protected boolean load() {
      BlobClient.instance = this;
      Log.info("Loading Configuration file...");
      this.config = new BlobConfiguration();
      try {
         if (!this.config.load(this)) {
            Log.info("New Configuration file created, please edit it and restart Blob.");
            return false;
         }
      } catch (final IOException e) {
         Log.error(e.getMessage(), e);
         return false;
      }

      if ("CHANGEME".equals(this.config.getWolframAlphaAppId()) || "CHANGEME".equals(this.config.getEsperNetNickServPass())) {
         Log.info("Please edit configuration!");
         return false;
      }

      Log.info("Loading Configuration...");
      this.name = this.config.getMainNick();
      this.getServers().addAll(this.config.getServers());

      Log.info("Configuration loaded! Registered servers:");
      for (final Server server : this.getServers()) {
         Log.info("- Server @ " + server.getUrl() + ':' + server.getPort() + ", SSL=" + server.getSslType());
         Log.info("  With channels:");
         for (final Channel channel : server.getChannels()) {
            Log.info("  - " + channel.getName());
         }
      }

      Log.info("Creating Command Manager...");
      this.createCommandManager("+", this.config.getAdmins());
      final CommandManager manager = getCommandManager();
      manager.setUnknownCommandMessage(null);

      Log.info("Registering Commands...");
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
      manager.registerCommand(new NickServCommand());

      // Util
      manager.registerCommand(new ShortenCommand());
      manager.registerCommand(new GoogleCommand());
      manager.registerCommand(new UrbanCommand());
      manager.registerCommand(new WolframAlphaCommand());

      // Logger filter
      Log.info("Adding Logger filters...");
      Log.addFilter(Pattern.quote(this.config.getWolframAlphaAppId()), "**********");
      Log.addFilter(Pattern.quote(this.config.getEsperNetNickServPass()), "**********");

      return true;
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

   @Override
   public void onServerJoined(final Server server) {
      if (this.config.hasEsperNetNickServAutoIdentify() && server.getUrl().contains("esper")) {
         final Source source = new Source(server, "NickServ", "NickServ", "NickServ@services.esper.net");
         source.sendMessage("IDENTIFY " + this.config.getEsperNetNickServPass());
      }
   }
}
