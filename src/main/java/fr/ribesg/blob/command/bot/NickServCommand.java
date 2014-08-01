package fr.ribesg.blob.command.bot;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.message.PrivMsgIrcPacket;
import fr.ribesg.blob.BlobClient;

/** @author Ribesg */
public class NickServCommand extends Command {

   public NickServCommand() {
      super("nickserv", new String[] {
         "NickServ command",
         "Usage: ## <identify [password]>|<register <password> <email>>|<auth <authCode>>"
      }, true, null, "ns");
   }

   @Override
   public boolean exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
      final Receiver receiver = channel == null ? user : channel;
      if (args.length < 1) {
         return false;
      }

      switch (args[0].toLowerCase()) {
         case "identify":
         case "id":
            final String pass = args.length > 1 ? args[1] : BlobClient.getConfig().getEsperNetNickServPass();
            server.send(new PrivMsgIrcPacket("NickServ", "IDENTIFY " + pass), true);
            receiver.sendMessage("Done.");
            return true;
         case "register":
         case "reg":
            if (args.length != 3) {
               return false;
            }
            server.send(new PrivMsgIrcPacket("NickServ", "REGISTER " + args[1] + " " + args[2]), true);
            receiver.sendMessage("Done.");
            return true;
         case "auth":
            if (args.length != 2) {
               return false;
            }
            server.send(new PrivMsgIrcPacket("NickServ", "AUTH " + args[1]), true);
            receiver.sendMessage("Done.");
            return true;
         default:
            return false;
      }
   }
}
