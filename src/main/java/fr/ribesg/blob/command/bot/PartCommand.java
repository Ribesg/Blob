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

public class PartCommand extends Command {

   public PartCommand() {
      super("part", new String[] {
         "Ask me to leave a/some channel(s)",
         "Usage: ## [channel[,...]]"
      }, true, null);
   }

   @Override
   public boolean exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
      if (args.length == 0) {
         channel.sendMessage("Bye!");
         channel.part();
      } else {
         boolean silent = false;
         for (final String arg : args) {
            switch (arg.toLowerCase()) {
               case "-s":
               case "--silent":
                  silent = true;
                  break;
               default:
                  break;
            }
         }
         for (final String arg : args) {
            final Channel otherChannel = server.getChannel(arg);
            if (otherChannel != null) {
               if (!silent) {
                  otherChannel.sendMessage(user.getName() + " told me I should leave this channel, bye!");
               }
               otherChannel.part();
            }
         }
      }
      return true;
   }
}
