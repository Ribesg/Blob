package fr.ribesg.blob.command

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
        this.commands.put("ping", PingCommand())
    }

    [Handler]
    fun onChannelMessageEvent(event: ChannelMessageEvent) {
        val message = event.getMessage()
        if (event.getMessage().startsWith(this.prefix)) {
            val firstSpace = message.indexOf(' ')
            val commandName = message.substring(1, if (firstSpace > 0) firstSpace else message.length())
            this.commands[commandName]?.exec(
                    event.getClient(),
                    event.getChannel(),
                    event.getActor(),
                    message.substring(commandName.length() + 1).split("\\s")
            )
        }
    }

    [Handler]
    fun onPrivateMessageEvent(event: PrivateMessageEvent) {
        val message = event.getMessage()
        val commandName: String
        val args: Array<String>
        val firstSpace = message.indexOf(' ')
        if (event.getMessage().startsWith(this.prefix)) {
            commandName = message.substring(1, if (firstSpace > 0) firstSpace else message.length())
            args = message.substring(commandName.length() + 1).split("\\s")
        } else {
            commandName = message.substring(0, if (firstSpace > 0) firstSpace else message.length())
            args = message.substring(commandName.length()).split("\\s")
        }
        this.commands[commandName]?.exec(event.getClient(), null, event.getActor(), args)
    }
}
