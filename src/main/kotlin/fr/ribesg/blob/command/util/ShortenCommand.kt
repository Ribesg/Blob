package fr.ribesg.blob.command.util

import fr.ribesg.blob.command.Command
import fr.ribesg.blob.extensions.sendRedNotice
import fr.ribesg.blob.util.Codes
import fr.ribesg.blob.util.Log
import fr.ribesg.blob.util.WebUtil
import org.kitteh.irc.client.library.Client
import org.kitteh.irc.client.library.element.Channel
import org.kitteh.irc.client.library.element.User
import java.io.IOException

/**
 * @author Ribesg
 */

public class ShortenCommand(prefix: Char) : Command(prefix) {

    init {
        this.usage = this.prefix + "s <url>"
    }

    override fun exec(client: Client, from: User, to: Channel?, primArg: String?, args: Array<String>) {
        if (args.size() != 1) {
            this.sendUsage(from)
        } else {
            val url = args[0];
            try {
                val shortUrl = WebUtil.shortenUrl(url);
                if (to == null) {
                    from.sendMessage("Result: " + shortUrl)
                } else {
                    to.sendMessage(from.getName() + ", " + shortUrl)
                }
            } catch (e: IOException) {
                from.sendRedNotice(Codes.RED + "Failed!")
                Log.error("Failed to shorten URL '" + url + "'", e);
            }
        }
    }
}
