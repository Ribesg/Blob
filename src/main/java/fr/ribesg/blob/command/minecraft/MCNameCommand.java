/*
 * Copyright (c) 2012-2014 Ribesg - www.ribesg.fr
 * This file is under GPLv3 -> http://www.gnu.org/licenses/gpl-3.0.txt
 * Please contact me at ribesg[at]yahoo.fr if you improve this file!
 */

package fr.ribesg.blob.command.minecraft;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Log;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.util.IrcUtil;
import fr.ribesg.alix.api.bot.util.WebUtil;
import fr.ribesg.alix.api.enums.Codes;

import java.util.regex.Pattern;

public class MCNameCommand extends Command {

   private static final Pattern MC_USER_REGEX = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");

   public MCNameCommand() {
      super("minecraftname", new String[] {
         "Look up a Minecraft username",
         "Usage: ## <name>"
      }, "mcname", "mcn");
   }

   @Override
   public boolean exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
      final Receiver receiver = channel == null ? user : channel;

      if (args.length != 1) {
         return false;
      }

      final String userName = args[0];
      String escapedUserName = IrcUtil.preventPing(userName);

      if (!MC_USER_REGEX.matcher(userName).matches()) {
         receiver.sendMessage(Codes.RED + '"' + userName + "\" is not a valid Minecraft username");
         return true;
      }

      String realName;
      String uuid = "???";
      try {
         final String resultString = WebUtil.post("https://api.mojang.com/profiles/page/1", "application/json", String.format("{\"name\":\"%s\",\"agent\":\"minecraft\"}", userName));
         final JsonObject result = new JsonParser().parse(resultString).getAsJsonObject();
         final JsonArray profiles = result.getAsJsonArray("profiles");
         if (profiles.size() < 1) {
            receiver.sendMessage("The username " + Codes.BOLD + escapedUserName + Codes.RESET + " is " +
                                 Codes.BOLD + Codes.LIGHT_GREEN + "available");
            return true;
         } else if (profiles.size() > 1) {
            receiver.sendMessage(Codes.RED + "Name '" + escapedUserName + "' matches multiple account, not supported yet");
            Log.error("Name '" + userName + "' matches multiple account, not supported yet");
            return true;
         } else {
            final JsonObject profile = profiles.get(0).getAsJsonObject();
            realName = profile.getAsJsonPrimitive("name").getAsString();
            escapedUserName = IrcUtil.preventPing(realName);
            uuid = profile.getAsJsonPrimitive("id").getAsString();
            if (uuid.length() != 32) {
               receiver.sendMessage(Codes.RED + "Incorrect UUID");
               Log.error("Incorrect UUID: " + uuid);
            } else {
               uuid = uuid.substring(0, 8) + '-' +
                      uuid.substring(8, 12) + '-' +
                      uuid.substring(12, 16) + '-' +
                      uuid.substring(16, 20) + '-' +
                      uuid.substring(20, 32);
            }
         }
      } catch (final Exception e) {
         receiver.sendMessage(Codes.RED + "Failed to get realName");
         Log.error("Failed to get realName", e);
      }

      final boolean hasPaid;
      try {
         final String result = WebUtil.get("https://minecraft.net/haspaid.jsp?user=" + userName).trim();
         switch (result) {
            case "true":
               hasPaid = true;
               break;
            case "false":
               hasPaid = false;
               break;
            default:
               throw new Exception("Unknown result: " + result);
         }
      } catch (final Exception e) {
         receiver.sendMessage(Codes.RED + "Failed to get hasPaid state");
         Log.error("Failed to get hasPaid state", e);
         return true;
      }

      if (hasPaid) {
         receiver.sendMessage("The username " + Codes.BOLD + escapedUserName + Codes.RESET + " (" + Codes.BOLD + uuid + Codes.RESET + ") is " +
                              Codes.BOLD + Codes.RED + "taken" + Codes.RESET + " and " + Codes.BOLD + Codes.RED + "premium");
      } else {
         receiver.sendMessage("The username " + Codes.BOLD + escapedUserName + Codes.RESET + " is " +
                              Codes.BOLD + Codes.RED + "taken" + Codes.RESET + " but " + Codes.BOLD + Codes.YELLOW + "not premium");
      }
      return true;
   }
}
