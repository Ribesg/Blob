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
import fr.ribesg.alix.api.bot.util.WebUtil;
import fr.ribesg.alix.api.enums.Codes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GoogleCommand extends Command {

   private static final String GOOGLE_URL = "http://www.google.%s/search?q=";

   public GoogleCommand() {
      super("google", new String[] {
         "Link to a Google search of something",
         "Ok this one is tricky: ##<.<lang>| ><query>"
      }, "g");
   }

   @Override
   public boolean exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
      final Receiver receiver = channel == null ? user : channel;

      if (args.length == 0) {
         return false;
      }

      final String site = primaryArgument == null ? "com" : primaryArgument;
      final StringBuilder request = new StringBuilder(args[0]);
      for (int i = 1; i < args.length; i++) {
         request.append(' ').append(args[i]);
      }
      try {
         WebUtil.get("http://www.google." + site, 1_000);
      } catch (IOException e) {
         receiver.sendMessage(Codes.RED + (channel == null ? "" : user.getName() + ", ") + Codes.LIGHT_BLUE + "http://www.google." + site + "/" + Codes.RED + " doesn't seem to be a thing");
         return true;
      }

      try {
         final String url = URLEncoder.encode(String.format(GOOGLE_URL, site) + request, "UTF-8");
         final String response;
         try {
            response = WebUtil.get(url);
         } catch (IOException e) {
            receiver.sendMessage(Codes.RED + (channel == null ? "" : user.getName() + ", ") + "failed to get response from Google!");
            Log.error(e.getMessage(), e);
            return true;
         }

         final String link;
         final String desc;
         try {
            final Document page = WebUtil.parseHtml(response);
            final Elements results = page.select("li.g");
            final Element result = results.get(0);
            link = result.select("h3.r a").get(0).attr("href");
            final StringBuilder descBuilder = new StringBuilder();
            result.select("div.s span.st").get(0).childNodes().forEach((node) -> {
               if (node instanceof Element) {
                  if (!((Element) node).tagName().equals("span")) {
                     descBuilder.append(((Element) node).text());
                  }
               } else {
                  descBuilder.append(node.toString());
               }
            });
            desc = descBuilder.toString();
         } catch (final Throwable t) {
            receiver.sendMessage(Codes.RED + (channel == null ? "" : user.getName() + ", ") + "failed to parse response from Google!");
            Log.error(t.getMessage(), t);
            return true;
         }
         String shortUrl = url;
         try {
            shortUrl = WebUtil.shortenUrl(link);
         } catch (final IOException ignored) {}
         receiver.sendMessage((channel == null ? "" : user.getName() + ", ") + shortUrl + " - " + desc);
      } catch (final UnsupportedEncodingException e) {
         receiver.sendMessage(Codes.RED + (channel == null ? "" : user.getName() + ", ") + "failed to encode URL!");
         Log.error(e.getMessage(), e);
      }
      return true;
   }
}
