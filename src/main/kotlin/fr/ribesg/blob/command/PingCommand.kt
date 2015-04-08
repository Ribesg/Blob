package fr.ribesg.blob.command

import org.kitteh.irc.client.library.Client
import org.kitteh.irc.client.library.element.Channel
import org.kitteh.irc.client.library.element.User

/**
 * @author Ribesg
 */

public class PingCommand : Command {

    override fun exec(client: Client, to: Channel?, from: User, args: Array<String>) {
        if (to == null) {
            from.sendMessage("Pong!")
        } else {
            to.sendMessage(from.getNick() + ", pong!")
        }
    }

}
