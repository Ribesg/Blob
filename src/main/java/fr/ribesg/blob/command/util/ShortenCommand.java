/*
 * Copyright (c) 2012-2014 Ribesg - www.ribesg.fr
 * This file is under GPLv3 -> http://www.gnu.org/licenses/gpl-3.0.txt
 * Please contact me at ribesg[at]yahoo.fr if you improve this file!
 */

package fr.ribesg.blob.command.util;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Log;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.command.CommandManager;
import fr.ribesg.alix.api.bot.util.WebUtil;
import fr.ribesg.alix.api.enums.Codes;

import java.io.IOException;

public class ShortenCommand extends Command {

   public ShortenCommand() {
      super("shorten", new String[] {
         "Shorten a url with the http://is.gd/ api",
         "Usage: ## <url>"
      }, "s");
   }

   @Override
   public boolean exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
      final Receiver receiver = channel == null ? user : channel;

      if (args.length != 1) {
         return false;
      }

      final String url = args[0];
      try {
         final String shortUrl = WebUtil.shortenUrl(url);
         receiver.sendMessage("Done: " + Codes.LIGHT_GREEN + shortUrl);
      } catch (final IOException e) {
         receiver.sendMessage(Codes.RED + "Failed to shorten URL");
         Log.error("Failed to shorten URL", e);
      }
      return true;
   }
}
