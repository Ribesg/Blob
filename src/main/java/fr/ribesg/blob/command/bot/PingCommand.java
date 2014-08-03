package fr.ribesg.blob.command.bot;

import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;

public class PingCommand extends Command {

   public PingCommand() {
      super("ping", new String[]{
         "Just responds to ping. Usage: ##"
      });
   }

   @Override
   public boolean exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
      (channel == null ? user : channel).sendMessage((channel == null ? "P" : (user.getName() + ", p")) + "ong!");
      return true;
   }
}
