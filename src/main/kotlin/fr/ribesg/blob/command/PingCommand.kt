package fr.ribesg.blob.command

import org.kitteh.irc.client.library.Client
import org.kitteh.irc.client.library.element.Channel
import org.kitteh.irc.client.library.element.User

/**
 * @author Ribesg
 */

public class PingCommand(prefix: Char) : Command(prefix) {

    init {
        this.usage = this.prefix + "ping"
    }

    override fun exec(client: Client, from: User, to: Channel?, primArg: String?, args: Array<String>) {
        if (to == null) {
            from.sendMessage("Pong!")
        } else {
            to.sendMessage(from.getNick() + ", pong!")
        }
    }
}
