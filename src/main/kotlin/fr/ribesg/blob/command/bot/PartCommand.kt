package fr.ribesg.blob.command.bot

import fr.ribesg.blob.command.Command
import fr.ribesg.blob.extensions.sendRedNotice
import org.kitteh.irc.client.library.Client
import org.kitteh.irc.client.library.element.Channel
import org.kitteh.irc.client.library.element.User

/**
 * @author Ribesg
 */

public class PartCommand(prefix: Char) : Command(prefix) {

    init {
        this.usage = this.prefix + "part <channel[,...]>"
    }

    override fun exec(client: Client, from: User, to: Channel?, primArg: String?, args: Array<String>) {
        if (args.size() == 0) {
            this.sendUsage(from)
        } else {
            args.forEach { arg ->
                if (client.getChannels().any { it.getName().equals(arg) }) {
                    client.getChannel(arg)?.part("Master asked...")
                } else {
                    from.sendRedNotice("I'm not in $arg!")
                }
            }
        }
    }
}
