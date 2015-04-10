package fr.ribesg.blob.command

import fr.ribesg.blob.command.util.ShortenCommand
import fr.ribesg.blob.command.util.UrbanCommand
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent
import org.kitteh.irc.lib.net.engio.mbassy.listener.Handler
import java.util.HashMap

/**
 * @author Ribesg
 */

public class CommandManager(val prefix: Char = '+') {

    private val commands: MutableMap<String, Command>

    init {
        this.commands = HashMap()
        this.commands.put("ping", PingCommand(this.prefix))
        this.commands.put("s", ShortenCommand(this.prefix))
        this.commands.put("urban", UrbanCommand(this.prefix))
    }

    [Handler]
    fun onChannelMessageEvent(event: ChannelMessageEvent) {
        val message = event.getMessage()
        if (event.getMessage().startsWith(this.prefix)) {
            val splitLoc = message.indexOfFirst { it == '.' || it == ' ' }
            val commandName: String
            val primArg: String?
            val args: Array<String>
            if (splitLoc > 0) {
                commandName = message.substring(1, splitLoc)
                primArg = if (message[splitLoc] == '.') message.substring(splitLoc + 1, message.indexOf(' ')) else null
                args = message.substring(message.indexOf(' ')).split("\\s")
            } else {
                commandName = message.substring(1)
                primArg = null
                args = array()
            }
            this.commands[commandName]?.exec(event.getClient(), event.getActor(), event.getChannel(), primArg, args)
        }
    }

    [Handler]
    fun onPrivateMessageEvent(event: PrivateMessageEvent) {
        val message = event.getMessage()
        val splitLoc = message.indexOfFirst { it == '.' || it == ' ' }
        val commandName: String
        val primArg: String?
        val args: Array<String>
        if (splitLoc > 0) {
            commandName = message.substring(if (message.startsWith(this.prefix)) 1 else 0, splitLoc)
            primArg = if (message[splitLoc] == '.') message.substring(splitLoc + 1, message.indexOf(' ')) else null
            args = message.substring(message.indexOf(' ')).split("\\s")
        } else {
            commandName = message.substring(1)
            primArg = null
            args = array()
        }
        this.commands[commandName]?.exec(event.getClient(), event.getActor(), null, primArg, args)
    }
}
