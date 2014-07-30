/*
 * Copyright (c) 2012-2014 Ribesg - www.ribesg.fr
 * This file is under GPLv3 -> http://www.gnu.org/licenses/gpl-3.0.txt
 * Please contact me at ribesg[at]yahoo.fr if you improve this file!
 */

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
      super("quit", new String[] {
         "Ask me to disconnect from this server",
         "Super Complicated Usage: ##"
      }, true, null);
      this.client = client;
   }

   @Override
   public boolean exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] strings) {
      for (final Channel c : server.getChannels()) {
         c.sendMessage("Bye!");
      }
      server.disconnect("Bye!");
      return true;
   }
}
