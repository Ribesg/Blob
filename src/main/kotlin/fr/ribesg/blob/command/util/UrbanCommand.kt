package fr.ribesg.blob.command.util

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import fr.ribesg.blob.command.Command
import fr.ribesg.blob.extensions.sendRedNotice
import fr.ribesg.blob.util.Log
import fr.ribesg.blob.util.WebUtil
import org.kitteh.irc.client.library.Client
import org.kitteh.irc.client.library.element.Channel
import org.kitteh.irc.client.library.element.User
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/**
 * @author Ribesg
 */

public class UrbanCommand(prefix: Char) : Command(prefix) {

    init {
        this.usage = this.prefix + "urban <query>"
    }

    override fun exec(client: Client, from: User, to: Channel?, primArg: String?, args: Array<String>) {
        if (args.size() == 0) {
            sendUsage(from)
        } else {

            val definitionNumber: Int = try {
                var tmp = Integer.parseInt(primArg!!)
                if (tmp < 1) 1 else tmp
            } catch (e: Exception) {
                1
            }

            val query = args.join(" ")
            val url = try {
                "http://urbanscraper.herokuapp.com/search/" + URLEncoder.encode(query, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                // Should not happen
                Log.error(e.getMessage() ?: "Error", e)
                return
            }

            val response = try {
                WebUtil.get(url)
            } catch (e: IOException) {
                from.sendRedNotice("Failed to get result from UrbanDictionary API!")
                Log.error(e.getMessage() ?: "Error", e)
                return
            }

            val jsonResponse = try {
                JsonParser().parse(response)
            } catch (e: JsonParseException) {
                from.sendRedNotice("Failed to parse response from UrbanDictionary API!")
                Log.error(e.getMessage() ?: "Error", e)
                return
            }

            if (!jsonResponse.isJsonArray()) return

            val array = jsonResponse.getAsJsonArray()
            val size = array.size()
            if (definitionNumber > size) {
                if (size == 0) {
                    (to ?: from).sendMessage(from.getNick() + ", no results")
                } else {
                    (to ?: from).sendMessage(from.getNick() + ", no such definition number")
                }
                return
            }

            val definitionElement = array.get(definitionNumber - 1);
            if (definitionElement.isJsonObject()) {
                val definitionObject = definitionElement.getAsJsonObject();
                if (definitionObject.has("definition")) {
                    var definitionString = definitionObject.getAsJsonPrimitive("definition").getAsString()
                    if (definitionString.length() > 200) {
                        definitionString = definitionString.substring(0, 197) + "..."
                    }
                    if (definitionObject.has("url")) {
                        val definitionUrl = definitionObject.getAsJsonPrimitive("url").getAsString()
                        val shortUrl = try {
                            WebUtil.shortenUrl(definitionUrl)
                        } catch (e: IOException) {
                            Log.error("Failed to shorten URL '$definitionUrl'", e)
                            definitionUrl
                        }
                        (to ?: from).sendMessage(from.getNick() + ", $definitionString ($shortUrl)")
                    } else {
                        (to ?: from).sendMessage(from.getNick() + ", $definitionString")
                    }
                } else {
                    from.sendRedNotice("Malformed response from API!")
                    Log.error("Malformed response from API!")
                }
            }
        }
    }
}
