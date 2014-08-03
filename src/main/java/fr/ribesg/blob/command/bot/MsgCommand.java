package fr.ribesg.blob.command.bot;

import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.message.PrivMsgIrcPacket;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Ribesg
 */
public class MsgCommand extends Command {

   public MsgCommand() {
      super("msg", new String[]{
         "Makes Blob use the /msg command",
         "Usage: ## <query>"
      }, true, null, "pm");
   }

   @Override
   public boolean exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
      final Receiver receiver = channel == null ? user : channel;
      if (args.length < 2) {
         return false;
      }

      final String query = StringUtils.join(args, ' ', 1, args.length);
      server.send(new PrivMsgIrcPacket(args[0], query));
      receiver.sendMessage((channel == null ? "D" : user.getName() + ", d") + "one.");
      return true;
   }
}
