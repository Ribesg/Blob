package fr.ribesg.blob.mtxserv;

import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.enums.Codes;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class MtxCommand extends Command {

   private final MtxservChannelHandler mtxserv;

   public MtxCommand(final MtxservChannelHandler mtxserv) {
      super("mtx", new String[]{
         "[FR] Commande mTx pour QuakeNet#mtxserv",
         "Utilisation: ## welcomemessage [\"get\"]",
         "             ## welcomemessage set [index] <value>",
         "             ## ping <\"add\"|\"del\"> [name]",
         "             ## admins <\"add\"|\"del\"> <name>",
         "             ## enable|disable",
         " Utilisez ### dans un message pour utiliser le pseudo de l'utilisateur"
      });
      this.mtxserv = mtxserv;
   }

   @Override
   public boolean exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
      final Receiver receiver = channel == null ? user : channel;

      if (!server.getUrl().contains("quakenet")) {
         receiver.sendMessage(Codes.RED + (channel == null ? "T" : user.getName() + ", t") + "his should be used on QuakeNet.");
         return true;
      }

      if (args.length < 1) {
         return false;
      }

      switch (args[0].toLowerCase()) {
         case "welcomemessage":
         case "wm":
            return this.wmSubCommand(receiver, user, channel != null, Arrays.copyOfRange(args, 1, args.length));
         case "ping":
            return this.pingSubCommand(receiver, user, channel != null, Arrays.copyOfRange(args, 1, args.length));
         case "admins":
            return this.adminsSubCommand(receiver, user, channel != null, Arrays.copyOfRange(args, 1, args.length));
         case "enable":
         case "on":
            if (this.mtxserv.hasAdmin(user)) {
               this.mtxserv.enabled = true;
               receiver.sendMessage((channel != null ? user.getName() + ", a" : "A") + "ctivé!");
            } else {
               receiver.sendMessage(Codes.RED + "Nope.");
            }
            return true;
         case "disable":
         case "off":
            if (this.mtxserv.hasAdmin(user)) {
               this.mtxserv.enabled = false;
               receiver.sendMessage((channel != null ? user.getName() + ", d" : "D") + "ésactivé!");
            } else {
               receiver.sendMessage(Codes.RED + "Nope.");
            }
            return true;
         default:
            return false;
      }
   }

   private boolean wmSubCommand(final Receiver receiver, final Source user, final boolean isChannel, final String[] args) {
      if (args.length == 0 || "get".equalsIgnoreCase(args[0])) {
         for (int i = 0; i < this.mtxserv.welcomeMessage.size(); i++) {
            receiver.sendMessage((isChannel ? user.getName() + ", " : "") + Codes.BOLD + i + " - " + Codes.RESET + this.mtxserv.welcomeMessage.get(i));
         }
         return true;
      } else if (args.length > 1 && "set".equalsIgnoreCase(args[0])) {
         if (this.mtxserv.hasAdmin(user)) {
            receiver.sendMessage(Codes.RED + "Nope.");
            return true;
         }

         int index;
         try {
            index = Integer.parseInt(args[1]);
            if (index < 0) {
               index = -1;
            }
         } catch (final NumberFormatException e) {
            index = -1;
         }

         if (index != -1 && args.length < 3) {
            return false;
         } else {
            final String message = StringUtils.join(args, ' ', index == -1 ? 1 : 2, args.length);
            if (index == -1) {
               this.mtxserv.welcomeMessage.clear();
               this.mtxserv.welcomeMessage.add(message);
            } else if (index >= this.mtxserv.welcomeMessage.size()) {
               this.mtxserv.welcomeMessage.add(message);
            } else {
               this.mtxserv.welcomeMessage.set(index, message);
            }
            receiver.sendMessage((isChannel ? user.getName() + ", " : "") + "Nouveau message de bienvenue:");
            for (int i = 0; i < this.mtxserv.welcomeMessage.size(); i++) {
               receiver.sendMessage((isChannel ? user.getName() + ", " : "") + Codes.BOLD + i + " - " + Codes.RESET + this.mtxserv.welcomeMessage.get(i));
            }
            this.mtxserv.save();
            return true;
         }
      } else {
         return false;
      }
   }

   private boolean pingSubCommand(final Receiver receiver, final Source user, final boolean isChannel, final String[] args) {
      if (this.mtxserv.hasAdmin(user)) {
         receiver.sendMessage(Codes.RED + "Nope.");
         return true;
      }

      boolean add;
      if (args.length < 1) {
         return false;
      } else if ("add".equalsIgnoreCase(args[0])) {
         add = true;
      } else if ("del".equalsIgnoreCase(args[0])) {
         add = false;
      } else {
         return false;
      }

      if (args.length == 1) {
         if (this.mtxserv.pingables.contains(user.getName())) {
            if (add) {
               receiver.sendMessage(Codes.RED + (isChannel ? user.getName() + ", v" : "V") + "ous êtes dans la liste.");
            } else {
               this.mtxserv.pingables.remove(user.getName());
               receiver.sendMessage((isChannel ? user.getName() + ", v" : "V") + "ous avez été retiré de la liste.");
            }
         } else {
            if (add) {
               this.mtxserv.pingables.add(user.getName());
               receiver.sendMessage((isChannel ? user.getName() + ", v" : "V") + "ous avez été ajouté à la liste.");
            } else {
               receiver.sendMessage(Codes.RED + (isChannel ? user.getName() + ", v" : "V") + "ous n'êtes pas dans la liste.");
            }
         }
      } else {
         for (int i = 1; i < args.length; i++) {
            final String userName = args[i];
            if (this.mtxserv.pingables.contains(userName)) {
               if (add) {
                  receiver.sendMessage(Codes.RED + (isChannel ? user.getName() + ", " : "") + userName + " est déjà dans la liste.");
               } else {
                  this.mtxserv.pingables.remove(userName);
                  receiver.sendMessage((isChannel ? user.getName() + ", " : "") + userName + " retiré de la liste.");
               }
            } else {
               if (add) {
                  this.mtxserv.pingables.add(userName);
                  receiver.sendMessage((isChannel ? user.getName() + ", " : "") + userName + " ajouté à la liste.");
               } else {
                  receiver.sendMessage(Codes.RED + (isChannel ? user.getName() + ", " : "") + userName + " n'est pas dans la liste.");
               }
            }
         }
      }
      this.mtxserv.save();
      return true;
   }

   private boolean adminsSubCommand(final Receiver receiver, final Source user, final boolean isChannel, final String[] args) {
      if (this.mtxserv.hasAdmin(user)) {
         receiver.sendMessage(Codes.RED + "Nope.");
         return true;
      }

      boolean add;
      if (args.length < 2) {
         return false;
      } else if ("add".equalsIgnoreCase(args[0])) {
         add = true;
      } else if ("del".equalsIgnoreCase(args[0])) {
         add = false;
      } else {
         return false;
      }

      for (int i = 1; i < args.length; i++) {
         final String userName = args[i];
         if (this.mtxserv.admins.contains(userName)) {
            if (add) {
               receiver.sendMessage(Codes.RED + (isChannel ? user.getName() + ", " : "") + userName + " est déjà admin.");
            } else {
               this.mtxserv.admins.remove(userName);
               receiver.sendMessage((isChannel ? user.getName() + ", " : "") + userName + " n'est plus admin.");
            }
         } else {
            if (add) {
               this.mtxserv.admins.add(userName);
               receiver.sendMessage((isChannel ? user.getName() + ", " : "") + userName + " est maintenant admin.");
            } else {
               receiver.sendMessage(Codes.RED + (isChannel ? user.getName() + ", " : "") + userName + " n'est pas admin.");
            }
         }
      }
      this.mtxserv.save();
      return true;
   }
}
