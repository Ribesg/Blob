package fr.ribesg.blob.extensions

import fr.ribesg.blob.util.Codes
import org.kitteh.irc.client.library.element.User

/**
 * This package contains extensions used by Blob.
 *
 * @author Ribesg
 */

/**
 * Just {@link User#sendNotice(String)} in red.
 *
 * @see User#sendNotice(String)
 */
fun User.sendRedNotice(message: String) {
    this.sendNotice(Codes.RED + message)
}
