package fr.ribesg.blob.command.util;

import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Log;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.util.WebUtil;
import fr.ribesg.alix.api.enums.Codes;
import fr.ribesg.blob.BlobClient;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URLEncoder;

/** @author Ribesg */
public class WolframAlphaCommand extends Command {

   public WolframAlphaCommand() {
      super("wolframalpha", new String[] {
         "Wolfram Alpha Command",
         "Usage: ## <query>"
      }, "wolfram", "wa");
   }

   @Override
   public boolean exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
      final Receiver receiver = channel == null ? user : channel;

      if (args.length < 1) {
         return false;
      }

      int timeout = 5_000;
      if (primaryArgument != null) {
         try {
            timeout = Integer.parseInt(primaryArgument);
         } catch (final NumberFormatException ignored) {}
      }

      final String query = StringUtils.join(args, " ");
      final String response;
      try {
         response = WebUtil.get("http://api.wolframalpha.com/v2/query?input=" + URLEncoder.encode(query, "UTF-8") + "&appid=" + URLEncoder.encode(BlobClient.getConfig().getWolframAlphaAppId(), "UTF-8"), timeout);
      } catch (final IOException e) {
         Log.error(e.getMessage(), e);
         receiver.sendMessage(Codes.RED + (channel == null ? "" : user.getName() + ", ") + "failed to contact WolframAlpha API!");
         return true;
      }

      final Document xmlDocument;
      try {
         xmlDocument = WebUtil.parseXml(response);
      } catch (final Throwable t) {
         Log.error("Failed to parse WolframAlpha API response", t);
         Log.debug("Response was: \n" + response);
         receiver.sendMessage(Codes.RED + (channel == null ? "" : user.getName() + ", ") + "failed to parse WolframAlpha API response!");
         return true;
      }

      try {
         final Element root = xmlDocument.getElementsByTag("queryresult").get(0);
         if ("false".equals(root.attr("success"))) {
            receiver.sendMessage(Codes.RED + (channel == null ? "" : user.getName() + ", ") + "No result.");
         } else {
            final Element primary = xmlDocument.getElementsByAttributeValue("primary", "true").get(0);
            final String text = primary.getElementsByTag("plaintext").get(0).text();
            receiver.sendMessage((channel == null ? "" : user.getName() + ", ") + text);
         }
      } catch (final Throwable t) {
         Log.error("Failed to parse WolframAlpha API response", t);
         Log.debug("Response was: \n" + response);
         receiver.sendMessage(Codes.RED + (channel == null ? "" : user.getName() + ", ") + "unsupported response kind, see logs to debug and implement!");
      }
      return true;
   }
}
