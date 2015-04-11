package fr.ribesg.blob.command

import org.kitteh.irc.client.library.Client
import org.kitteh.irc.client.library.element.Channel
import org.kitteh.irc.client.library.element.MessageReceiver
import org.kitteh.irc.client.library.element.User

/**
 * @author Ribesg
 */

public abstract class Command(protected val prefix: Char) {

    protected var usage: String? = null

    public abstract fun exec(client: Client, from: User, to: Channel?, primArg: String?, args: Array<String>)

    protected fun sendUsage(to: MessageReceiver) {
        if (this.usage != null) {
            to.sendNotice("Usage: ${this.usage}")
        }
    }
}
