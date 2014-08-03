/*
 * Copyright (c) 2012-2014 Ribesg - www.ribesg.fr
 * This file is under GPLv3 -> http://www.gnu.org/licenses/gpl-3.0.txt
 * Please contact me at ribesg[at]yahoo.fr if you improve this file!
 */

package fr.ribesg.blob.command.minecraft.bukkitdev;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Client;
import fr.ribesg.alix.api.Log;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.util.IrcUtil;
import fr.ribesg.alix.api.bot.util.WebUtil;
import fr.ribesg.alix.api.enums.Codes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class AuthorCommand extends Command {

   private static final String CURSE_PLUGIN_URL      = "http://www.curse.com/bukkit-plugins/minecraft";
   private static final String BUKKITDEV_URL         = "http://dev.bukkit.org";
   private static final String BUKKITDEV_PROFILE_URL = BUKKITDEV_URL + "/profiles/";

   private SimpleDateFormat dateFormat;
   private NumberFormat     numberFormat;

   public AuthorCommand() {
      super("author", new String[] {
         "Look up a BukkitDev plugin Author",
         "Usage: ## <name> [\"more\"|amount]"
      });
      this.dateFormat = new SimpleDateFormat("YYYY-MM-dd");
      this.numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
   }

   @Override
   public boolean exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
      final Receiver receiver = channel == null ? user : channel;

      if (args.length != 1 && args.length != 2) {
         return false;
      }

      int mode = -1;
      if (args.length == 2) {
         if ("more".equalsIgnoreCase(args[1])) {
            mode = 0;
         } else {
            try {
               mode = Integer.parseInt(args[1]);
               if (mode < 1) {
                  return false;
               }
            } catch (final NumberFormatException e) {
               return false;
            }
         }
      }

      final List<String> messages = parsePages(args[0], mode);
      receiver.sendMessage(messages.toArray(new String[messages.size()]));
      return true;
   }

   private List<String> parsePages(final String name, int mode) {
      final List<String> messages = new ArrayList<>();
      try {
         final UserInfo userInfo = getUserInfo(name);

         if (userInfo == null) {
            messages.add(Codes.RED + "Unable to find that user!");
            return messages;
         }

         final Map<String, Future<Plugin>> pluginPages = new HashMap<>();

         boolean hasNextPage;
         userInfo.profilePageLink = BUKKITDEV_PROFILE_URL + userInfo.name;
         String nextPageLink = userInfo.profilePageLink + "/bukkit-plugins/";
         do {
            // Get the page
            final Document doc = WebUtil.parseHtml(WebUtil.get(nextPageLink));

            // Check if there is at least one plugin
            if (doc.select(".listing-none-found").size() > 0) {
               printResult(messages, userInfo, mode);
               return messages;
            }

            // Check if we will have to look at another page
            final Elements pages = doc.select(".listing-pagination-pages").get(0).children();
            if (pages.size() > 1) {
               final Element lastLink = pages.get(pages.size() - 1);
               if (lastLink.children().size() > 0 && lastLink.child(0).ownText().trim().startsWith("Next")) {
                  hasNextPage = true;
                  nextPageLink = BUKKITDEV_URL + lastLink.child(0).attr("href");
               } else {
                  hasNextPage = false;
                  nextPageLink = null;
               }
            } else {
               hasNextPage = false;
               nextPageLink = null;
            }

            // List stuff on this page
            final Elements pluginsTd = doc.select(".col-project");
            pluginsTd.stream().filter(elem -> "td".equalsIgnoreCase(elem.tagName())).forEach(elem -> {
               final Element link = elem.select("h2").get(0).select("a").get(0);
               final String pluginName = link.ownText().trim();
               final String pluginUrl = CURSE_PLUGIN_URL + link.attr("href").substring(15);

               final Callable<Plugin> pluginCallable = () -> {
                  final Plugin plugin = new Plugin();
                  plugin.name = pluginName;
                  try {
                     final Document pluginDoc = WebUtil.parseHtml(WebUtil.get(pluginUrl));
                     try {
                        plugin.lastUpdate = getDate(pluginDoc.select("li.updated").get(0).getElementsByTag("abbr").get(0).attr("data-epoch"));
                        plugin.monthlyDownloadCount = getNbDownloads(pluginDoc.select("li.average-downloads").get(0).ownText());
                        plugin.totalDownloadCount = getNbDownloads(pluginDoc.select("li.downloads").get(0).ownText());
                     } catch (final Exception e) {
                        Log.error(e.getMessage(), e);
                        plugin.lastUpdate = Codes.RED + "Not found on Curse!" + Codes.RESET;
                        plugin.monthlyDownloadCount = "0";
                        plugin.totalDownloadCount = "0";
                     }
                     try {
                        plugin.url = WebUtil.shortenUrl(pluginUrl);
                     } catch (final Throwable t) {
                        plugin.url = pluginUrl;
                        Log.error("Failed to shorten URL '" + pluginUrl + "'", t);
                     }
                     return plugin;
                  } catch (final IOException ex) {
                     return null;
                  }
               };

               final Future<Plugin> pluginFuture = Client.getThreadPool().submit(pluginCallable);

               pluginPages.put(pluginName, pluginFuture);
            });
         } while (hasNextPage);

         for (final Map.Entry<String, Future<Plugin>> entry : pluginPages.entrySet()) {
            try {
               userInfo.plugins.add(entry.getValue().get());
            } catch (final Throwable t) {
               Log.error(t.getMessage(), t);
            }
         }

         printResult(messages, userInfo, mode);
      } catch (final FileNotFoundException | MalformedURLException e) {
         messages.add(Codes.RED + "Unable to find that user or failed to get a plugin page!");
      } catch (final IOException | ParseException e) {
         messages.add(Codes.RED + "Failed: " + e.getMessage());
      }
      return messages;
   }

   private void printResult(final List<String> messages, final UserInfo userInfo, final int mode) throws ParseException, IOException {
      userInfo.computeTotalDownloads();

      String shortUrl;
      try {
         shortUrl = WebUtil.shortenUrl(userInfo.profilePageLink);
      } catch (final IOException e) {
         shortUrl = userInfo.profilePageLink;
         Log.error("Failed to shorten URL '" + userInfo.profilePageLink + "'", e);
      }

      if (mode == -1) {
         // Oneline mode
         messages.add(Codes.BOLD + '[' + userInfo.state + Codes.RESET + Codes.BOLD + "] " + IrcUtil.preventPing(userInfo.name) + Codes.RESET + " (" + Codes.LIGHT_GREEN + shortUrl + Codes.RESET + ") - " + Codes.LIGHT_GREEN + userInfo.plugins.size() + Codes.RESET + " plugins - DLs (m/t): " + Codes.BOLD + Codes.LIGHT_GREEN + userInfo.totalMonthly + Codes.RESET + " / " + Codes.BOLD + Codes.LIGHT_GREEN + userInfo.totalTotal + Codes.RESET + " - Now: " + Codes.BOLD + userInfo.lastLogin);
      } else {
         if (userInfo.plugins.isEmpty()) {
            messages.add(Codes.BOLD + '[' + userInfo.state + Codes.RESET + Codes.BOLD + "] " + IrcUtil.preventPing(userInfo.name) + Codes.RESET + " (" + shortUrl + ") | Reputation: " + userInfo.reputation + " | No Projects");
            messages.add("Join date: " + userInfo.joined + " | Status: " + userInfo.lastLogin);
            if (userInfo.state.contains("Banned")) {
               messages.add("Ban reason: " + userInfo.banReason);
            }
         } else {
            final Iterator<Plugin> it = userInfo.plugins.iterator();

            messages.add(Codes.BOLD + '[' + userInfo.state + Codes.RESET + Codes.BOLD + "] " + IrcUtil.preventPing(userInfo.name) + Codes.RESET + " (" + Codes.LIGHT_GREEN + shortUrl + Codes.RESET + ") | Reputation: " + Codes.BOLD + userInfo.reputation + Codes.RESET + " | Projects: " + Codes.BOLD + userInfo.plugins.size());
            messages.add("Join date: " + Codes.BOLD + userInfo.joined + Codes.RESET + " | Status: " + Codes.BOLD + userInfo.lastLogin);
            if (userInfo.state.contains("Banned")) {
               messages.add("Ban reason: " + Codes.BOLD + userInfo.banReason);
            }
            if (mode == 0) {
               final Plugin plugin = it.next();
               messages.add("Last updated plugin: " + Codes.BOLD + IrcUtil.preventPing(plugin.name) + Codes.RESET + " on " + plugin.lastUpdate + " (" + Codes.LIGHT_GREEN + plugin.url + Codes.RESET + ")");
            } else {
               messages.add((mode < userInfo.plugins.size() ? mode : userInfo.plugins.size()) + " last updated plugins:");
               int i = 0;
               while (it.hasNext() && i < mode) {
                  final Plugin plugin = it.next();
                  messages.add("- " + Codes.BOLD + IrcUtil.preventPing(plugin.name) + Codes.RESET + " (" + plugin.lastUpdate + ")");
                  messages.add("  | Downloads: " + Codes.LIGHT_GREEN + plugin.monthlyDownloadCount + Codes.RESET + " monthly, " + Codes.LIGHT_GREEN + plugin.totalDownloadCount + Codes.RESET + " total");
                  i++;
               }
            }
            messages.add("Total downloads: " + Codes.BOLD + Codes.LIGHT_GREEN + numberFormat.format(userInfo.totalMonthly) + Codes.RESET + " monthly, " + Codes.BOLD + Codes.LIGHT_GREEN + numberFormat.format(userInfo.totalTotal) + Codes.RESET + " total");
         }
      }
   }

   private final class Plugin implements Comparable<Plugin> {

      public String name;
      public String lastUpdate;
      public String totalDownloadCount;
      public String monthlyDownloadCount;
      public String url;

      @Override
      public int compareTo(final Plugin o) {
         return lastUpdate.compareTo(o.lastUpdate) > 0 ? -1 : 1;
      }
   }

   private class UserInfo {

      public String            name;
      public String            state;
      public String            joined;
      public String            lastLogin;
      public String            reputation;
      public String            banReason;
      public SortedSet<Plugin> plugins;
      public int               totalMonthly;
      public int               totalTotal;

      public String profilePageLink;

      public void computeTotalDownloads() throws ParseException {
         totalMonthly = totalTotal = 0;
         for (final Plugin plugin : plugins) {
            totalMonthly += (long) numberFormat.parse(plugin.monthlyDownloadCount);
            totalTotal += (long) numberFormat.parse(plugin.totalDownloadCount);
         }
      }
   }

   private UserInfo getUserInfo(final String bukkitDevUser) throws IOException {
      final Document doc = WebUtil.parseHtml(WebUtil.get(BUKKITDEV_PROFILE_URL + bukkitDevUser));
      final UserInfo info = new UserInfo();

      // Username
      info.name = doc.select("h1").get(1).ownText().trim();

      // User state
      if (doc.select(".avatar-author").size() > 0) {
         info.state = Codes.BLUE + "Author";
      } else if (doc.select(".avatar-normal").size() > 0) {
         info.state = Codes.GRAY + "Normal";
      } else if (doc.select(".avatar-moderator").size() > 0) {
         info.state = Codes.GREEN + "Staff";
      } else if (doc.select(".avatar-banned").size() > 0) {
         info.state = Codes.RED + "Banned";
      } else {
         info.state = Codes.PURPLE + "Unknown";
      }
      info.state += Codes.RESET;

      final Elements elems = doc.select(".content-box-inner");
      final Element contentDiv = elems.get(elems.size() - 1);

      // User joined date
      String date = contentDiv.select(".standard-date").get(0).attr("data-epoch");
      info.joined = getDate(date);

      // Last login
      if (contentDiv.select(".user-online").size() > 0) {
         info.lastLogin = Codes.LIGHT_GREEN + "Online" + Codes.RESET;
      } else {
         final Elements elements = contentDiv.select(".user-offline");
         if (elements.size() == 0) {
            // Unknown user
            return null;
         }
         date = elements.get(0).select(".standard-date").get(0).attr("data-epoch");
         info.lastLogin = Codes.GRAY + "Offline, last login on " + getDate(date) + Codes.RESET;
      }

      // Reputation
      info.reputation = contentDiv.getElementsByAttribute("data-value").get(0).ownText().trim();

      // Ban reason
      if (info.state.contains("Banned")) {
         info.banReason = doc.select(".warning-message-inner").get(0).child(0).ownText().trim().substring(27);
      }

      info.plugins = new TreeSet<>();
      return info;
   }

   private String getNbDownloads(final String downloadsString) {
      return downloadsString.substring(0, downloadsString.indexOf(' '));
   }

   private String getDate(final String dataEpoch) {
      final long dateLong = Long.parseLong(dataEpoch);
      final Date date = new Date(1_000 * dateLong);
      return dateFormat.format(date);
   }

}
