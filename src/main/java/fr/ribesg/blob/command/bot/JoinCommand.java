/*
 * Copyright (c) 2012-2014 Ribesg - www.ribesg.fr
 * This file is under GPLv3 -> http://www.gnu.org/licenses/gpl-3.0.txt
 * Please contact me at ribesg[at]yahoo.fr if you improve this file!
 */

package fr.ribesg.blob.command.bot;

import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.callback.Callback;
import fr.ribesg.alix.api.enums.Codes;
import fr.ribesg.alix.api.event.ReceivedPacketEvent;
import fr.ribesg.alix.api.message.IrcPacket;
import fr.ribesg.alix.api.message.JoinIrcPacket;

public class JoinCommand extends Command {

   public JoinCommand() {
      super("join", new String[] {
         "Ask me to join a/some channel(s)",
         "Usage: ## <channel[,...]>"
      }, true, null);
   }

   @Override
   public boolean exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
      final Receiver receiver = channel == null ? user : channel;

      if (args.length < 1) {
         return false;
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
         final boolean finalSilent = silent;
         for (final String arg : args) {
            if ("-s".equalsIgnoreCase(arg) || "--silent".equals(arg)) {
               continue;
            }
            final Channel otherChannel = server.getChannel(arg);
            if (otherChannel == null) {
               server.send(new JoinIrcPacket(arg), new Callback(5_000, "JOIN") {

                  @Override
                  public boolean onReceivedPacket(final ReceivedPacketEvent event) {
                     final IrcPacket packet = event.getPacket();
                     final String channelName = packet.getParameters().length > 0 ? packet.getParameters()[0] : packet.getTrail();
                     if (channelName.equals(this.originalIrcPacket.getParameters()[0])) {
                        final Channel channel = this.server.getChannel(channelName);
                        if (!finalSilent) {
                           channel.sendMessage(user.getName() + " told me I should join this channel, hi!");
                        }
                        this.runAllCallbacks();
                        event.consume();
                        return true;
                     } else {
                        return false;
                     }
                  }
               }.addCallback(() -> receiver.sendMessage(Codes.RED + "I'm in!")));
            } else {
               receiver.sendMessage(Codes.RED + "I'm already in " + otherChannel.getName() + "!");
            }
         }
         return true;
      }
   }
}
