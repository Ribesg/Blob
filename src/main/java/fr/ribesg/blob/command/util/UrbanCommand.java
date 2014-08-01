package fr.ribesg.blob.command.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Log;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.util.WebUtil;
import fr.ribesg.alix.api.enums.Codes;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UrbanCommand extends Command {

   public UrbanCommand() {
      super("urban", new String[] {
         "Find something on UrbanDictionary",
         "Usage: ##[.number] <query>"
      }, "u");
   }

   @Override
   public boolean exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
      final Receiver receiver = channel == null ? user : channel;

      if (args.length == 0) {
         return false;
      }

      int definitionNumber;
      try {
         definitionNumber = Integer.parseInt(primaryArgument);
         if (definitionNumber < 1) {
            definitionNumber = 1;
         }
      } catch (final NumberFormatException e) {
         definitionNumber = 1;
      }
      final String query = StringUtils.join(args, " ");
      final String url;
      try {
         url = "http://urbanscraper.herokuapp.com/search/" + URLEncoder.encode(query, "UTF-8");
      } catch (final UnsupportedEncodingException e) {
         Log.error(e.getMessage(), e);
         receiver.sendMessage(Codes.RED + (channel == null ? "" : user.getName() + ", ") + "failed to encode that thing to a valid URL parameter!");
         return true;
      }

      final String response;
      try {
         response = WebUtil.get(url);
      } catch (final IOException e) {
         Log.error(e.getMessage(), e);
         receiver.sendMessage(Codes.RED + (channel == null ? "" : user.getName() + ", ") + "failed to get result from API!");
         return true;
      }

      final JsonElement jsonResponse;
      try {
         jsonResponse = new JsonParser().parse(response);
      } catch (final JsonParseException e) {
         Log.error(e.getMessage(), e);
         receiver.sendMessage(Codes.RED + (channel == null ? "" : user.getName() + ", ") + "failed to parse response from API!");
         return true;
      }

      if (jsonResponse.isJsonArray()) {
         final JsonArray array = jsonResponse.getAsJsonArray();
         final int size = array.size();
         if (size == 0) {
            receiver.sendMessage(Codes.RED + (channel == null ? "" : user.getName() + ", ") + "no result.");
         } else if (definitionNumber > size) {
            receiver.sendMessage(Codes.RED + (channel == null ? "" : user.getName() + ", ") + "no such definition number.");
         } else {
            final JsonElement definitionElement = array.get(definitionNumber - 1);
            if (definitionElement.isJsonObject()) {
               final JsonObject definitionObject = definitionElement.getAsJsonObject();
               if (definitionObject.has("definition")) {
                  final String definitionString = definitionObject.getAsJsonPrimitive("definition").getAsString();
                  final String definitionUrl;
                  if (definitionObject.has("url")) {
                     definitionUrl = definitionObject.getAsJsonPrimitive("url").getAsString();
                     String shortUrl;
                     try {
                        shortUrl = WebUtil.shortenUrl(url);
                     } catch (IOException e) {
                        Log.error("Failed to shorten URL '" + definitionUrl + "'", e);
                        shortUrl = url;
                     }
                     receiver.sendMessage((channel == null ? "" : user.getName() + ", ") + definitionString + '(' + shortUrl + ')');
                  } else {
                     receiver.sendMessage((channel == null ? "" : user.getName() + ", ") + ", " + definitionString);
                  }
               } else {
                  Log.error("Malformed response from API!");
                  receiver.sendMessage(Codes.RED + (channel == null ? "" : user.getName() + ", ") + "malformed response from API!");
               }
            }
         }
      }
      return true;
   }
}
