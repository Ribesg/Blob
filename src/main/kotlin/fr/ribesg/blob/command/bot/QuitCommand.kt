package fr.ribesg.blob.command.bot

import fr.ribesg.blob.command.Command
import org.kitteh.irc.client.library.Client
import org.kitteh.irc.client.library.element.Channel
import org.kitteh.irc.client.library.element.User

/**
 * @author Ribesg
 */

public class QuitCommand(prefix: Char) : Command(prefix) {

    init {
        this.usage = this.prefix + "quit [reason]"
    }

    override fun exec(client: Client, from: User, to: Channel?, primArg: String?, args: Array<String>) {
        if (args.size() == 0) {
            client.shutdown("Master asked...")
        } else {
            client.shutdown(args.join(" "))
        }
    }
}
