package fr.ribesg.blob.command

import org.kitteh.irc.client.library.Client
import org.kitteh.irc.client.library.element.Channel
import org.kitteh.irc.client.library.element.User

/**
 * @author Ribesg
 */

public trait Command {

    public fun exec(client: Client, to: Channel?, from: User, args: Array<String>)

}
