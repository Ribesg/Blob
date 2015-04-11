package fr.ribesg.blob.command.bot

import fr.ribesg.blob.command.Command
import org.kitteh.irc.client.library.Client
import org.kitteh.irc.client.library.element.Channel
import org.kitteh.irc.client.library.element.User

/**
 * @author Ribesg
 */

public class MsgCommand(prefix: Char) : Command(prefix) {

    init {
        this.usage = this.prefix + "msg <target> <message>"
    }

    override fun exec(client: Client, from: User, to: Channel?, primArg: String?, args: Array<String>) {
        // TODO
    }
}
